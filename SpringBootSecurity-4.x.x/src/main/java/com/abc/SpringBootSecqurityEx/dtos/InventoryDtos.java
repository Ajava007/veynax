package com.abc.SpringBootSecqurityEx.dtos;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class InventoryDtos {
    private InventoryDtos() {}

    public record InventoryItemResponse(
            Long id, String partCode, String warehouse, String location, String lotNumber,
            String serialNumber, BigDecimal onHand, BigDecimal reserved, BigDecimal available,
            String status, Instant createdAt, Instant updatedAt) {}

    public record StockAdjustRequest(
            @NotBlank String partCode,
            @NotBlank String warehouse,
            String lotNumber,
            @NotNull @DecimalMin(value = "0.0001", message = "Quantity must be positive") BigDecimal quantity,
            @NotBlank(message = "Transaction type is required") String txnType, // RECEIPT/ISSUE/SCRAP/ADJUST
            String referenceType,
            String referenceId,
            @Size(max = 300) String remark) {}

    public record StockTransactionResponse(
            Long id, String partCode, String warehouse, String txnType, BigDecimal quantity,
            String referenceType, String referenceId, String remark, Instant createdAt) {}

    public record MrpRequest(
            @NotNull LocalDate planDate,
            @NotEmpty List<MrpDemandLine> demand) {}

    public record MrpDemandLine(
            @NotBlank String partCode,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal grossRequirement,
            @DecimalMin(value = "0") BigDecimal scheduledReceipts) {}

    public record MrpResultResponse(
            String runNumber, LocalDate planDate, String partCode, BigDecimal grossRequirement,
            BigDecimal onHand, BigDecimal scheduledReceipts, BigDecimal netShortage, String action) {}

    public record ReserveRequest(
        @jakarta.validation.constraints.NotBlank String partCode,
        String workOrderNumber,
        @jakarta.validation.constraints.NotBlank String warehouse,
        String lotNumber,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.0001") java.math.BigDecimal quantity,
        java.time.LocalDate requiredBy,
        String reservedBy,
        String remark) {}

    public record ReservationResponse(
        Long id, String reservationNumber, String partCode, String workOrderNumber,
        String warehouse, String lotNumber, java.math.BigDecimal quantity,
        String status, java.time.LocalDate requiredBy, String reservedBy, String remark,
        java.time.Instant createdAt, java.time.Instant updatedAt) {}

    public record ShortageResponse(
        String partCode, String partName, java.math.BigDecimal required,
        java.math.BigDecimal onHand, java.math.BigDecimal reserved,
        java.math.BigDecimal available, java.math.BigDecimal shortage,
        String severity) {} // CRITICAL, WARNING, OK

}
