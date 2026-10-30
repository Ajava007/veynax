package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.dtos.SalesDtos.*;
import com.abc.SpringBootSecqurityEx.entity.Quotation;
import com.abc.SpringBootSecqurityEx.entity.SalesOrder;
import com.abc.SpringBootSecqurityEx.service.QuotationService;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/quotations")
public class QuotationController {
    private final QuotationService service;
    public QuotationController(QuotationService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Quotation>>> list(
            @RequestParam(required=false) String dealerCode,
            @RequestParam(required=false) String status,
            Pageable pageable) {
        if (dealerCode != null) return ResponseEntity.ok(ApiResponse.success(service.findByDealer(dealerCode, pageable)));
        if (status != null) return ResponseEntity.ok(ApiResponse.success(service.findByStatus(status, pageable)));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Quotation>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Quotation>> create(@Valid @RequestBody QuotationRequest req) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.create(req)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Quotation>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.submitForApproval(id), "Submitted for approval"));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Quotation>> approve(@PathVariable Long id, @RequestParam String approvedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id, approvedBy), "Quotation approved"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Quotation>> reject(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(service.reject(id, reason), "Quotation rejected"));
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<SalesOrder>> convert(@PathVariable Long id) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.convertToOrder(id), "Converted to sales order"));
    }

    @PostMapping("/orders/{soNumber}/advance-payment")
    public ResponseEntity<ApiResponse<SalesOrder>> advancePayment(@PathVariable String soNumber, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success(service.recordAdvancePayment(soNumber, amount), "Advance payment recorded"));
    }
}
