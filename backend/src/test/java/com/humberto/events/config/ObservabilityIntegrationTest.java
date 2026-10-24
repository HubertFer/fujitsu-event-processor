package com.humberto.events.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
class ObservabilityIntegrationTest {

    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generatesCorrelationIdWhenMissing() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern(UUID_PATTERN)));
    }

    @Test
    void propagatesCorrelationIdSentByClient() throws Exception {
        mockMvc.perform(get("/api/events").header(CorrelationIdFilter.HEADER, "equipment-42"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, "equipment-42"));
    }

    @Test
    void replacesInvalidCorrelationId() throws Exception {
        mockMvc.perform(get("/api/events").header(CorrelationIdFilter.HEADER, "bad id\nforged log line"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern(UUID_PATTERN)));
    }

    @Test
    void exposesProcessedEventsMetric() throws Exception {
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":"OBS-1","eventType":"QUALITY","description":"Inspection failed","timestamp":"2026-05-20T10:00:00"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("events_processed_total{outcome=\"registered\",type=\"QUALITY\"}")));
    }
}
