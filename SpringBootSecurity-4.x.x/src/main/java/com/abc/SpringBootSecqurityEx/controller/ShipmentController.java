package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.dtos.ShipmentDtos.*;
import com.abc.SpringBootSecqurityEx.entity.*;
import com.abc.SpringBootSecqurityEx.service.ShipmentService;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService service;
    public ShipmentController(ShipmentService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Shipment>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Shipment>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Shipment>> create(@Valid @RequestBody ShipmentRequest req) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.create(req)));
    }

    @PostMapping("/{id}/containers")
    public ResponseEntity<ApiResponse<Container>> addContainer(@PathVariable Long id, @Valid @RequestBody ContainerRequest req) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.addContainer(id, req)));
    }

    @PostMapping("/receive")
    public ResponseEntity<ApiResponse<DamageRecord>> receive(@Valid @RequestBody ReceivingRequest req) {
        DamageRecord dmg = service.recordReceiving(req);
        return ResponseEntity.ok(ApiResponse.success(dmg, dmg != null ? "Receiving recorded; damage quarantined + claim auto-created" : "Receiving recorded; no damage"));
    }

    @PostMapping("/customs")
    public ResponseEntity<ApiResponse<CustomsDeclaration>> customs(@Valid @RequestBody CustomsRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.recordCustoms(req)));
    }

    @PostMapping("/landed-cost")
    public ResponseEntity<ApiResponse<LandedCost>> landedCost(@Valid @RequestBody LandedCostRequest req) {
        return ResponseEntity.ok(ApiResponse.success(service.recordLandedCost(req)));
    }

    @GetMapping("/damages")
    public ResponseEntity<ApiResponse<Page<DamageRecord>>> damages(@RequestParam String poNumber, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.findDamages(poNumber, pageable)));
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<Page<SupplierClaim>>> claims(@RequestParam(required=false) String status, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.findClaims(status, pageable)));
    }
}
