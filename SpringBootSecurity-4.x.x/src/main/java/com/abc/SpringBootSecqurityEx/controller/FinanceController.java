package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.entity.*;
import com.abc.SpringBootSecqurityEx.service.FinanceService;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/finance")
public class FinanceController {
    private final FinanceService service;
    public FinanceController(FinanceService service) { this.service = service; }

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<Page<Invoice>>> invoices(
            @RequestParam(required=false) String dealerCode, Pageable pageable) {
        if (dealerCode != null) return ResponseEntity.ok(ApiResponse.success(service.invoicesByDealer(dealerCode, pageable)));
        return ResponseEntity.ok(ApiResponse.success(service.invoices(pageable)));
    }

    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<ApiResponse<Invoice>> invoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(ApiResponse.success(service.invoiceByNumber(invoiceNumber)));
    }

    @PostMapping("/invoices/generate/{salesOrderId}")
    public ResponseEntity<ApiResponse<Invoice>> generate(@PathVariable Long salesOrderId) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.generateInvoice(salesOrderId), "Invoice generated, due in 90 days"));
    }

    @PostMapping("/invoices/{invoiceNumber}/payments")
    public ResponseEntity<ApiResponse<Receipt>> applyPayment(@PathVariable String invoiceNumber,
            @RequestParam BigDecimal amount, @RequestParam String method, @RequestParam(required=false) String reference) {
        return ResponseEntity.ok(ApiResponse.success(service.applyPayment(invoiceNumber, amount, method, reference), "Payment applied"));
    }

    @PostMapping("/invoices/{invoiceNumber}/credit-notes")
    public ResponseEntity<ApiResponse<CreditNote>> applyCredit(@PathVariable String invoiceNumber,
            @RequestParam BigDecimal amount, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(service.applyCreditNote(invoiceNumber, amount, reason), "Credit note applied"));
    }

    @PostMapping("/refunds")
    public ResponseEntity<ApiResponse<Refund>> refund(@RequestParam String dealerCode,
            @RequestParam BigDecimal amount, @RequestParam String reason) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.issueRefund(dealerCode, amount, reason), "Refund issued (pending)"));
    }

    @GetMapping("/warranty-claims")
    public ResponseEntity<ApiResponse<Page<WarrantyClaim>>> warrantyClaims(
            @RequestParam(required=false) String vin, Pageable pageable) {
        if (vin != null) return ResponseEntity.ok(ApiResponse.success(service.warrantyByVin(vin, pageable)));
        return ResponseEntity.ok(ApiResponse.success(service.warrantyClaims(pageable)));
    }

    @PostMapping("/warranty-claims")
    public ResponseEntity<ApiResponse<WarrantyClaim>> fileClaim(@RequestParam String vin,
            @RequestParam(required=false) String partCode, @RequestParam String defect,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.fileWarrantyClaim(vin, partCode, defect, amount), "Warranty claim filed"));
    }

    @PostMapping("/warranty-claims/{id}/approve")
    public ResponseEntity<ApiResponse<WarrantyClaim>> approveClaim(@PathVariable Long id,
            @RequestParam BigDecimal approvedAmount) {
        return ResponseEntity.ok(ApiResponse.success(service.approveWarrantyClaim(id, approvedAmount), "Warranty claim approved"));
    }
}
