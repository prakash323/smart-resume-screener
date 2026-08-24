package com.unthinkable.resumescreener.controller;

import com.unthinkable.resumescreener.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResumeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LlmClient llmClient;

    @Test
    void uploadingResumeParsesAndPersistsExtractedData() throws Exception {
        when(llmClient.complete(anyString(), anyString(), anyString())).thenReturn(
                "{\"skills\":[\"Java\",\"Spring Boot\"],\"experience\":[\"Backend Engineer at Acme (2021-2024)\"],\"education\":[\"B.Tech CS\"]}"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain",
                "Jane Doe\nJava and Spring Boot backend engineer".getBytes());

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .param("candidateName", "Jane Doe")
                        .param("email", "jane@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.candidateName").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.extractedData.skills[0]").value("Java"))
                .andExpect(jsonPath("$.extractedData.skills[1]").value("Spring Boot"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void rejectsUploadWithoutCandidateName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "text".getBytes());

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .param("email", "jane@example.com"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void rejectsUploadWithBlankCandidateName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "text".getBytes());

        mockMvc.perform(multipart("/api/resumes")
                        .file(file)
                        .param("candidateName", "   ")
                        .param("email", "jane@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void getUnknownResumeReturns404() throws Exception {
        mockMvc.perform(get("/api/resumes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }
}
