package com.humberto.events.service;

import com.humberto.events.domain.AuditRecord;
import com.humberto.events.repository.AuditRecordRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditRecordRepository auditRecordRepository;

    public AuditService(AuditRecordRepository auditRecordRepository) {
        this.auditRecordRepository = auditRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> findAll() {
        return auditRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
