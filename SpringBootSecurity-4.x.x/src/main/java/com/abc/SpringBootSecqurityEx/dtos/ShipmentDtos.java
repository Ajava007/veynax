package com.abc.SpringBootSecqurityEx.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class ShipmentDtos {
    private ShipmentDtos() {}

    public record ShipmentRequest(
        @NotBlank @Size(max=30) String poNumber,
        @NotBlank @Size(max=30) String supplierCode,
        @Size(max=120) String vesselName,
        @Size(max=40) String voyageNumber,
        @Size(max=80) String portOfLoading,
        @Size(max=80) String portOfDischarge,
        LocalDate etd, LocalDate eta,
        @Size(max=60) String commercialInvoiceRef,
        @Size(max=60) String packingListRef,
        String status,
        List<ContainerRequest> containers) {}

    public record ContainerRequest(
        @NotBlank @Size(max=20) String containerNumber,
        @Size(max=30) String sealNumber,
        @Size(max=20) String containerType,
        BigDecimal grossWeightKg,
        String status) {}

    public record ShipmentResponse(
        Long id, String shipmentNumber, String poNumber, String supplierCode,
        String vesselName, String voyageNumber, String portOfLoading, String portOfDischarge,
        LocalDate etd, LocalDate eta, LocalDate actualDeparture, LocalDate actualArrival,
        String status, String commercialInvoiceRef, String packingListRef, String remarks,
        List<ContainerResponse> containers,
        Instant createdAt, Instant updatedAt) {}

    public record ContainerResponse(
        Long id, String containerNumber, String sealNumber, String containerType,
        BigDecimal grossWeightKg, String status, String remarks,
        Instant createdAt, Instant updatedAt) {}

    public record ReceivingRequest(
        @NotNull Long shipmentId,
        @NotBlank String partCode,
        @NotNull @Min(0) Integer receivedQuantity,
        @Min(0) Integer rejectedQuantity,
        @Min(0) Integer damagedQuantity,
        @Min(0) Integer missingQuantity,
        String damageType,
        String responsibleParty,
        BigDecimal estimatedLoss,
        String evidenceUrl) {}

    public record CustomsRequest(
        @NotNull Long shipmentId,
        @NotBlank @Size(max=60) String declarationNumber,
        LocalDate declarationDate,
        BigDecimal dutyAmount, BigDecimal vatAmount, BigDecimal otherCharges,
        String status, String heldReason) {}

    public record LandedCostRequest(
        @NotNull Long shipmentId,
        BigDecimal freightCost, BigDecimal insuranceCost, BigDecimal dutyCost,
        BigDecimal handlingCost, BigDecimal otherCost,
        String currency, String allocationMethod, boolean reconciled) {}

    public record DamageResponse(
        Long id, String damageNumber, String poNumber, Long shipmentId, String containerNumber,
        String partCode, Integer damagedQuantity, String damageType, String responsibleParty,
        BigDecimal estimatedLoss, String status, Long claimId, String remarks,
        Instant createdAt, Instant updatedAt) {}

    public record ClaimResponse(
        Long id, String claimNumber, String supplierCode, Long damageRecordId,
        BigDecimal claimedAmount, BigDecimal recoveredAmount, String creditNoteRef,
        String status, String remarks,
        Instant createdAt, Instant updatedAt) {}
}
