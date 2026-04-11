package com.taskflow.project;

import com.taskflow.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

public class ProjectIntegrationTest extends BaseIntegrationTest {

    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setup() throws Exception {
        ownerToken = extractToken(mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Owner",
                          "email": "owner@test.com",
                          "password": "password123"
                        }
                        """))
                .andReturn().getResponse().getContentAsString());

        otherToken = extractToken(mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Other",
                          "email": "other@test.com",
                          "password": "password123"
                        }
                        """))
                .andReturn().getResponse().getContentAsString());
    }

    private String extractToken(String responseBody) throws Exception {
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    private String createProject(String token, String name) throws Exception {
        return mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(String.format("{\"name\": \"%s\"}", name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createProject_asOwner_returns201() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        {
                          "name": "My Project",
                          "description": "Test description"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("My Project"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void listProjects_returnsOwnedProjects() throws Exception {
        createProject(ownerToken, "Project A");
        createProject(ownerToken, "Project B");

        mockMvc.perform(get("/projects")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void listProjects_withPagination_returnsCorrectPages() throws Exception {
        createProject(ownerToken, "Proj 1");
        createProject(ownerToken, "Proj 2");
        createProject(ownerToken, "Proj 3");

        // Ask for Page 1, Size 2
        mockMvc.perform(get("/projects?page=1&limit=2")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))) // Should only contain 2 items
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.limit").value(2))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalPages").value(2)); // 3 items / 2 limit = ceil(1.5) = 2 pages

        // Ask for Page 2, Size 2
        mockMvc.perform(get("/projects?page=2&limit=2")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1))) // Should contain the remaining 1 item
                .andExpect(jsonPath("$.page").value(2));
    }

    @Test
    void updateProject_asOwner_returns200() throws Exception {
        String response = createProject(ownerToken, "Original Name");
        String projectId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/projects/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        { "name": "Updated Name" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateProject_asNonOwner_returns403() throws Exception {
        String response = createProject(ownerToken, "Owner Project");
        String projectId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(patch("/projects/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .content("""
                        { "name": "Hacked" }
                        """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Only the project owner can update this project"));
    }

    @Test
    void deleteProject_asOwner_returns204() throws Exception {
        String response = createProject(ownerToken, "To Delete");
        String projectId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/projects/" + projectId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/projects/" + projectId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProject_asNonOwner_returns403() throws Exception {
        String response = createProject(ownerToken, "Protected Project");
        String projectId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/projects/" + projectId)
                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listProjects_pagination_returnsPagedResponse() throws Exception {
        createProject(ownerToken, "Project 1");
        createProject(ownerToken, "Project 2");
        createProject(ownerToken, "Project 3");

        mockMvc.perform(get("/projects?page=1&limit=2")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").isNotEmpty());
    }

    @Test
    void getProject_asOwner_returns200() throws Exception {
        String response = createProject(ownerToken, "Get Me");
        String projectId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/projects/" + projectId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Get Me"));
    }

    @Test
    void createProject_missingName_returns400() throws Exception {
        mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.fields.name").isNotEmpty());
    }

    @Test
    void deleteProject_cascadeDeletesTasks_returns204() throws Exception {
        String projectResponse = createProject(ownerToken, "Project With Tasks");
        String pid = objectMapper.readTree(projectResponse).get("id").asText();

        // create a task in this project
        mockMvc.perform(post("/projects/" + pid + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        { "title": "Task to cascade delete" }
                        """))
                .andExpect(status().isCreated());

        // delete the project
        mockMvc.perform(delete("/projects/" + pid)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        // project should be gone
        mockMvc.perform(get("/projects/" + pid)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());

        // tasks should be gone too — verify via list
        mockMvc.perform(get("/projects/" + pid + "/tasks")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listProjects_doesNotIncludeOtherUsersProjects() throws Exception {
        createProject(ownerToken, "Owner Project");
        createProject(otherToken, "Other Project");

        String response = mockMvc.perform(get("/projects")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // owner should not see other's project in their list
        String dataStr = objectMapper.readTree(response).get("data").toString();
        assert !dataStr.contains("Other Project");
    }
}