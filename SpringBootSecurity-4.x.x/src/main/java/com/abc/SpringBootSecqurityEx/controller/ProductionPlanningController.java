package com.abc.SpringBootSecqurityEx.controller;

import com.abc.SpringBootSecqurityEx.entity.Forecast;
import com.abc.SpringBootSecqurityEx.entity.LineTarget;
import com.abc.SpringBootSecqurityEx.service.ForecastService;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/planning")
public class ProductionPlanningController {

    private final ForecastService service;
    public ProductionPlanningController(ForecastService service) { this.service = service; }

    // --- Forecasts ---
    @GetMapping("/forecasts")
    public ResponseEntity<ApiResponse<Page<Forecast>>> forecasts(
            @RequestParam(required=false) String modelCode,
            @RequestParam(required=false) String status,
            Pageable pageable) {
        if (modelCode != null) return ResponseEntity.ok(ApiResponse.success(service.findByModel(modelCode, pageable)));
        if (status != null) return ResponseEntity.ok(ApiResponse.success(service.findByStatus(status, pageable)));
        return ResponseEntity.ok(ApiResponse.success(service.findAll(pageable)));
    }

    @PostMapping("/forecasts")
    public ResponseEntity<ApiResponse<Forecast>> createForecast(@RequestBody Forecast f) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.create(f)));
    }

    @PostMapping("/forecasts/{id}/submit")
    public ResponseEntity<ApiResponse<Forecast>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.submitForApproval(id), "Submitted for approval"));
    }

    @PostMapping("/forecasts/{id}/approve")
    public ResponseEntity<ApiResponse<Forecast>> approve(@PathVariable Long id, @RequestParam String approvedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.approve(id, approvedBy), "Forecast approved"));
    }

    @PostMapping("/forecasts/{id}/adjust")
    public ResponseEntity<ApiResponse<Forecast>> adjust(@PathVariable Long id,
            @RequestParam int adjustment, @RequestParam String reason, @RequestParam String adjustedBy) {
        return ResponseEntity.ok(ApiResponse.success(service.adjust(id, adjustment, reason, adjustedBy), "Forecast adjusted; final forecast recalculated"));
    }

    // --- Line targets (daily production) ---
    @GetMapping("/line-targets")
    public ResponseEntity<ApiResponse<List<LineTarget>>> lineTargets(
            @RequestParam(required=false) String lineCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (lineCode != null) return ResponseEntity.ok(ApiResponse.success(service.lineTargetsByLine(lineCode, from, to)));
        return ResponseEntity.ok(ApiResponse.success(service.lineTargets(from, to)));
    }

    @PostMapping("/line-targets")
    public ResponseEntity<ApiResponse<LineTarget>> setTarget(@RequestBody LineTarget t) {
        return ResponseEntity.status(201).body(ApiResponse.success(service.setTarget(t)));
    }

    @PostMapping("/line-targets/{id}/actual")
    public ResponseEntity<ApiResponse<LineTarget>> recordActual(@PathVariable Long id,
            @RequestParam int actual, @RequestParam(defaultValue="0") int rejected,
            @RequestParam(defaultValue="0") int downtimeMinutes) {
        return ResponseEntity.ok(ApiResponse.success(service.recordActual(id, actual, rejected, downtimeMinutes), "Actual recorded"));
    }
}
