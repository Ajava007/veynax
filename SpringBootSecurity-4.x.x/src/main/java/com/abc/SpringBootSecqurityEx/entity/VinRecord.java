package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vin_record", indexes = {
    @Index(name="idx_vin_wo", columnList="wo_number"),
    @Index(name="idx_vin_model", columnList="model_code"),
    @Index(name="idx_vin_status", columnList="status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VinRecord extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "vin", nullable = false, unique = true, length = 17) private String vin;
    @Column(name = "wo_number", nullable = false, length = 30) private String woNumber;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(length = 60) private String variant;
    @Column(length = 30) private String color;
    @Column(name = "bom_revision", length = 10) private String bomRevision;
    @Column(name = "line_code", length = 30) private String lineCode;
    @Column(name = "build_date") private LocalDate buildDate;
    @Column(name = "completed_at") private LocalDateTime completedAt;
    @Column(name = "quality_hold", nullable = false) private boolean qualityHold = false;
    @Column(name = "hold_reason", length = 500) private String holdReason;
    @Column(nullable = false, length = 20) private String status = "IN_BUILD"; // IN_BUILD, BUILT, QUALITY_HOLD, RELEASED, ALLOCATED, DELIVERED
    @Column(name = "released_by", length = 100) private String releasedBy;
    @Column(name = "released_at") private LocalDateTime releasedAt;
    @Column(name = "allocated_to_dealer", length = 30) private String allocatedToDealer;
    @Column(name = "delivered_at") private LocalDateTime deliveredAt;
    @OneToMany(mappedBy = "vin", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<VinComponent> components = new ArrayList<>();
}
