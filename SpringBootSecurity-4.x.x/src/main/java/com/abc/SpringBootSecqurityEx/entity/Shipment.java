package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipment", indexes = {@Index(name="idx_shipment_po", columnList="po_number"), @Index(name="idx_shipment_status", columnList="status")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Shipment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shipment_number", nullable = false, unique = true, length = 40) private String shipmentNumber;
    @Column(name = "po_number", nullable = false, length = 30) private String poNumber;
    @Column(name = "supplier_code", nullable = false, length = 30) private String supplierCode;
    @Column(name = "vessel_name", length = 120) private String vesselName;
    @Column(name = "voyage_number", length = 40) private String voyageNumber;
    @Column(name = "port_of_loading", length = 80) private String portOfLoading;
    @Column(name = "port_of_discharge", length = 80) private String portOfDischarge;
    @Column(name = "etd") private LocalDate etd; // estimated time of departure
    @Column(name = "eta") private LocalDate eta; // estimated time of arrival
    @Column(name = "actual_departure") private LocalDate actualDeparture;
    @Column(name = "actual_arrival") private LocalDate actualArrival;
    @Column(nullable = false, length = 20) private String status = "PLANNED"; // PLANNED, IN_TRANSIT, ARRIVED, IN_CUSTOMS, CLEARED, RECEIVED, DELAYED
    @Column(name = "commercial_invoice_ref", length = 60) private String commercialInvoiceRef;
    @Column(name = "packing_list_ref", length = 60) private String packingListRef;
    @Column(length = 500) private String remarks;
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<Container> containers = new ArrayList<>();
}
