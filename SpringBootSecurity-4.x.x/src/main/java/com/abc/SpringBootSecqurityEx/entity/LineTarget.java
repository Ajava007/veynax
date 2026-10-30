package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "line_target", indexes = {
    @Index(name="idx_line_target_date", columnList="target_date,line_code"),
    @Index(name="idx_line_target_line", columnList="line_code")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LineTarget extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "target_date", nullable = false) private LocalDate targetDate;
    @Column(name = "line_code", nullable = false, length = 30) private String lineCode;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(name = "target_quantity", nullable = false) private Integer targetQuantity = 0;
    @Column(name = "actual_quantity", nullable = false) private Integer actualQuantity = 0;
    @Column(name = "rejected_quantity", nullable = false) private Integer rejectedQuantity = 0;
    @Column(name = "downtime_minutes", nullable = false) private Integer downtimeMinutes = 0;
    @Column(length = 500) private String remarks;
}
