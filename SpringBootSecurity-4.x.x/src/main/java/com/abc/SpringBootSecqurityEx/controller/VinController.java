package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.entity.VinComponent;
import com.abc.SpringBootSecqurityEx.entity.VinRecord;
import com.abc.SpringBootSecqurityEx.service.VinService;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vins")
public class VinController {

    private final VinService service;
    public VinController(VinService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VinRecord>>> list(
            @RequestParam(required=false) String woNumber,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String modelCode,
            Pageable pageable) {
        if (woNumber != null) return ResponseEntity.ok(ApiResponse.success(service.findByWo(woNumber, pageable)));
        if (status != null) return ResponseEntity.ok(ApiResponse.success(service.findByStatus(status, pageable)));
        if (modelCode != null) return ResponseEntity.ok(ApiResponse.success(service.findByModel(modelCode, pageable)));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @GetMapping("/{vin}")
    public ResponseEntity<ApiResponse<VinRecord>> get(@PathVariable String vin) {
        return ResponseEntity.ok(ApiResponse.success(service.findByVin(vin)));
    }

    @GetMapping("/{vin}/genealogy")
    public ResponseEntity<ApiResponse<List<VinComponent>>> genealogy(@PathVariable String vin) {
        return ResponseEntity.ok(ApiResponse.success(service.genealogy(vin)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VinRecord>> create(@RequestBody VinRecord v) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.createVin(v)));
    }

    @PostMapping("/{vin}/components")
    public ResponseEntity<ApiResponse<VinComponent>> installComponent(@PathVariable String vin, @RequestBody VinComponent comp) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.installComponent(vin, comp)));
    }

    @PostMapping("/{vin}/quality-hold")
    public ResponseEntity<ApiResponse<VinRecord>> qualityHold(@PathVariable String vin, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success(service.placeQualityHold(vin, reason), "Quality hold placed"));
    }

    @PostMapping("/{vin}/release")
    public ResponseEntity<ApiResponse<VinRecord>> release(@PathVariable String vin, @RequestParam String releasedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.releaseFromHold(vin, releasedBy), "Released from quality hold"));
    }

    @PostMapping("/{vin}/mark-built")
    public ResponseEntity<ApiResponse<VinRecord>> markBuilt(@PathVariable String vin) {
        return ResponseEntity.ok(ApiResponse.success(service.markBuilt(vin), "Vehicle marked built"));
    }

    @PostMapping("/{vin}/allocate")
    public ResponseEntity<ApiResponse<VinRecord>> allocate(@PathVariable String vin, @RequestParam String dealerCode) {
        return ResponseEntity.ok(ApiResponse.success(service.allocateToDealer(vin, dealerCode), "Allocated to dealer"));
    }

    @PostMapping("/{vin}/deliver")
    public ResponseEntity<ApiResponse<VinRecord>> deliver(@PathVariable String vin) {
        return ResponseEntity.ok(ApiResponse.success(service.markDelivered(vin), "Vehicle delivered"));
    }
}
