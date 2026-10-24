package com.humberto.events.api;

import com.humberto.events.repository.AuditRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @Test
    void createsEventAndReturnsLocation() throws Exception {
        postEvent("IT-1", "PRODUCTION", "Production line started")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/events/IT-1")))
                .andExpect(jsonPath("$.eventId").value("IT-1"))
                .andExpect(jsonPath("$.eventType").value("PRODUCTION"));
    }

    @Test
    void rejectsDuplicateEventWithConflict() throws Exception {
        postEvent("IT-2", "PRODUCTION", "Production line started").andExpect(status().isCreated());

        postEvent("IT-2", "PRODUCTION", "Production line started")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate event"));
    }

    @Test
    void qualityEventCreatesAuditRecord() throws Exception {
        postEvent("IT-3", "QUALITY", "Inspection failed").andExpect(status().isCreated());

        assertThat(auditRecordRepository.findAll())
                .anyMatch(audit -> audit.getEventId().equals("IT-3")
                        && audit.getAction().equals("QUALITY_EVENT_REGISTERED"));
    }

    @Test
    void productionEventDoesNotCreateAuditRecord() throws Exception {
        postEvent("IT-4", "PRODUCTION", "Production line started").andExpect(status().isCreated());

        assertThat(auditRecordRepository.findAll())
                .noneMatch(audit -> audit.getEventId().equals("IT-4"));
    }

    @Test
    void rejectsBlankDescription() throws Exception {
        postEvent("IT-5", "PRODUCTION", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void rejectsUnknownEventType() throws Exception {
        postEvent("IT-6", "MAINTENANCE", "Unknown type")
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownEvent() throws Exception {
        mockMvc.perform(get("/api/events/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Event not found"));
    }

    private ResultActions postEvent(String eventId, String eventType, String description) throws Exception {
        String body = """
                {"eventId":"%s","eventType":"%s","description":"%s","timestamp":"2026-05-20T10:00:00"}
                """.formatted(eventId, eventType, description);
        return mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}

