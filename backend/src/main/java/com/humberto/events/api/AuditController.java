package com.humberto.events.api;

import com.humberto.events.api.dto.AuditRecordResponse;
import com.humberto.events.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditRecordResponse> findAll() {
        return auditService.findAll().stream()
                .map(AuditRecordResponse::from)
                .toList();
    }
}
