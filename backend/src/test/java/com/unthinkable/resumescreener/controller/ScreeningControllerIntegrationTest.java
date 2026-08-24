package com.unthinkable.resumescreener.controller;

import com.jayway.jsonpath.JsonPath;
import com.unthinkable.resumescreener.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScreeningControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmClient llmClient;

    @Test
    void fullFlowUploadResumeCreateJdRunScreeningAndShortlist() throws Exception {
        // ExtractionService and ScoringService use distinct, fixed system prompts, so matching
        // on (systemPrompt, userPrompt) together unambiguously distinguishes an extraction call
        // from a scoring call, even though both calls' user prompts embed the same résumé text.
        when(llmClient.complete(contains("résumé parser"), contains("Alice"), anyString()))
                .thenReturn("{\"skills\":[\"Java\",\"Spring Boot\",\"Kubernetes\"],\"experience\":[\"Senior Backend Engineer at Acme (2019-2024)\"],\"education\":[\"B.Tech CS\"]}");
        when(llmClient.complete(contains("résumé parser"), contains("Bob"), anyString()))
                .thenReturn("{\"skills\":[\"Excel\"],\"experience\":[\"Sales Associate at RetailCo\"],\"education\":[\"B.Com\"]}");

        Long aliceId = uploadResume("Alice", "alice@example.com", "Alice - Senior Java Spring Boot Kubernetes engineer");
        Long bobId = uploadResume("Bob", "bob@example.com", "Bob - Retail sales associate with Excel skills");

        Long jdId = createJobDescription("Backend Engineer", "Need a Java + Spring Boot + Kubernetes backend engineer.");

        when(llmClient.complete(contains("technical recruiter"), contains("Alice"), anyString()))
                .thenReturn("{\"score\":9,\"justification\":\"Strong Java, Spring Boot and Kubernetes match.\"}");
        when(llmClient.complete(contains("technical recruiter"), contains("Bob"), anyString()))
                .thenReturn("{\"score\":2,\"justification\":\"No relevant backend engineering experience.\"}");

        runScreening(aliceId, jdId).andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(9));
        runScreening(bobId, jdId).andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(2));

        mockMvc.perform(get("/api/screenings/shortlist").param("jobDescriptionId", jdId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateName").value("Alice"))
                .andExpect(jsonPath("$[0].score").value(9))
                .andExpect(jsonPath("$[1].candidateName").value("Bob"))
                .andExpect(jsonPath("$[1].score").value(2));

        mockMvc.perform(get("/api/screenings/shortlist")
                        .param("jobDescriptionId", jdId.toString())
                        .param("minScore", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].candidateName").value("Alice"));
    }

    @Test
    void screeningWithUnknownResumeReturns404() throws Exception {
        Long jdId = createJobDescription("Backend Engineer", "Java backend role.");

        mockMvc.perform(post("/api/screenings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resumeId\":999999,\"jobDescriptionId\":" + jdId + "}"))
                .andExpect(status().isNotFound());
    }

    private Long uploadResume(String candidateName, String email, String resumeText) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", resumeText.getBytes());

        String body = mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .param("candidateName", candidateName)
                        .param("email", email))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return Long.valueOf(JsonPath.read(body, "$.id").toString());
    }

    private Long createJobDescription(String title, String rawText) throws Exception {
        String requestBody = "{\"title\":\"" + title + "\",\"rawText\":\"" + rawText + "\"}";

        String body = mockMvc.perform(post("/api/job-descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return Long.valueOf(JsonPath.read(body, "$.id").toString());
    }

    private org.springframework.test.web.servlet.ResultActions runScreening(Long resumeId, Long jdId) throws Exception {
        return mockMvc.perform(post("/api/screenings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"resumeId\":" + resumeId + ",\"jobDescriptionId\":" + jdId + "}"));
    }
}
