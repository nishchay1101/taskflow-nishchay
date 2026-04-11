package com.taskflow.task;

import com.taskflow.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

public class TaskIntegrationTest extends BaseIntegrationTest {

    private String ownerToken;
    private String otherToken;
    private String projectId;

    @BeforeEach
    void setup() throws Exception {
        ownerToken = extractToken(mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Owner",
                          "email": "taskowner@test.com",
                          "password": "password123"
                        }
                        """))
                .andReturn().getResponse().getContentAsString());

        otherToken = extractToken(mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Other",
                          "email": "taskother@test.com",
                          "password": "password123"
                        }
                        """))
                .andReturn().getResponse().getContentAsString());

        String projectResponse = mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        { "name": "Task Test Project" }
                        """))
                .andReturn().getResponse().getContentAsString();

        projectId = objectMapper.readTree(projectResponse).get("id").asText();
    }

    private String extractToken(String body) throws Exception {
        return objectMapper.readTree(body).get("token").asText();
    }

    private String createTask(String title, String priority) throws Exception {
        String response = mockMvc.perform(post("/projects/" + projectId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content(String.format("""
                        { "title": "%s", "priority": "%s" }
                        """, title, priority)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    @Test
    void createTask_asOwner_returns201WithStatusTodo() throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        {
                          "title": "Fix bug",
                          "priority": "HIGH"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void createTask_asNonOwner_returns403() throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .content("""
                        { "title": "Hacked task" }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createTask_missingTitle_returns400() throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        { "priority": "HIGH" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.fields.title").isNotEmpty());
    }

    @Test
    void listTasks_filterByStatus_returnsCorrectTasks() throws Exception {
        createTask("Todo Task", "LOW");

        mockMvc.perform(get("/projects/" + projectId + "/tasks?status=TODO")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("TODO"))));
    }

    @Test
    void listTasks_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/projects/" + projectId + "/tasks?status=GARBAGE")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }

    @Test
    void updateTask_asOwner_returns200() throws Exception {
        String taskId = createTask("Update me", "LOW");

        mockMvc.perform(patch("/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("""
                        { "status": "IN_PROGRESS" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_emptyBody_returnsUnchanged() throws Exception {
        String taskId = createTask("No change", "HIGH");

        mockMvc.perform(patch("/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + ownerToken)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("No change"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void updateTask_asNonOwnerNonAssignee_returns403() throws Exception {
        String taskId = createTask("Protected", "LOW");

        mockMvc.perform(patch("/tasks/" + taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + otherToken)
                .content("""
                        { "status": "DONE" }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTask_asOwner_returns204() throws Exception {
        String taskId = createTask("Delete me", "LOW");

        mockMvc.perform(delete("/tasks/" + taskId)
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_asNonOwner_returns403() throws Exception {
        String taskId = createTask("Protected task", "LOW");

        mockMvc.perform(delete("/tasks/" + taskId)
                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }
}