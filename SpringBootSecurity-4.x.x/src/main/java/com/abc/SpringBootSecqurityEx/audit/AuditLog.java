package com.abc.SpringBootSecqurityEx.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
    @Index(name = "idx_audit_actor", columnList = "actor"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String actor;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Instant timestamp;

    @Column(nullable = false, updatable = false)
    private String entityType;

    @Column(updatable = false)
    private String entityId;

    @Column(nullable = false, updatable = false)
    private String action;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String beforeState;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String afterState;

    @Column(updatable = false)
    private String ipAddress;
}
