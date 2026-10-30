package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "material_reservation", indexes = {
    @Index(name="idx_reservation_part", columnList="part_code"),
    @Index(name="idx_reservation_wo", columnList="work_order_number"),
    @Index(name="idx_reservation_status", columnList="status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialReservation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "reservation_number", nullable = false, unique = true, length = 40) private String reservationNumber;
    @Column(name = "part_code", nullable = false, length = 40) private String partCode;
    @Column(name = "work_order_number", length = 30) private String workOrderNumber;
    @Column(nullable = false, length = 60) private String warehouse;
    @Column(name = "lot_number", length = 40) private String lotNumber;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity = BigDecimal.ZERO;
    @Column(nullable = false, length = 20) private String status = "RESERVED"; // RESERVED, ISSUED, CANCELLED, EXPIRED
    @Column(name = "required_by") private LocalDate requiredBy;
    @Column(name = "reserved_by", length = 100) private String reservedBy;
    @Column(length = 300) private String remark;
}
