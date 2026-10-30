package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vin_component", indexes = {
    @Index(name="idx_vincomp_vin", columnList="vin"),
    @Index(name="idx_vincomp_part", columnList="part_code"),
    @Index(name="idx_vincomp_serial", columnList="serial_number")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VinComponent extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "vin", nullable = false, length = 17) private String vin;
    @Column(name = "part_code", nullable = false, length = 40) private String partCode;
    @Column(name = "part_name", length = 150) private String partName;
    @Column(name = "serial_number", length = 60) private String serialNumber; // for critical serialized components
    @Column(name = "batch_number", length = 40) private String batchNumber;
    @Column(name = "supplier_code", length = 30) private String supplierCode;
    @Column(name = "po_number", length = 30) private String poNumber;
    @Column(name = "shipment_number", length = 40) private String shipmentNumber;
    @Column(name = "container_number", length = 20) private String containerNumber;
    @Column(name = "installed_at_workstation", length = 60) private String installedAtWorkstation;
    @Column(name = "installed_by", length = 100) private String installedBy;
    @Column(name = "installed_at") private java.time.LocalDateTime installedAt;
}
