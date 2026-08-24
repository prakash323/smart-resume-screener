package com.unthinkable.resumescreener.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobDescriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsAndFetchesJobDescription() throws Exception {
        String requestBody = """
                {"title":"Backend Engineer","rawText":"Looking for a Java + Spring Boot engineer with 3+ years experience."}
                """;

        String responseBody = mockMvc.perform(post("/api/job-descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Backend Engineer"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();

        int id = JsonPath.read(responseBody, "$.id");

        mockMvc.perform(get("/api/job-descriptions/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Backend Engineer"));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        String requestBody = """
                {"title":"","rawText":"some text"}
                """;

        mockMvc.perform(post("/api/job-descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    void listAllReturnsCreatedJobDescriptions() throws Exception {
        String requestBody = """
                {"title":"Data Engineer","rawText":"SQL and ETL pipelines."}
                """;
        mockMvc.perform(post("/api/job-descriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/job-descriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", hasItem("Data Engineer")));
    }
}
