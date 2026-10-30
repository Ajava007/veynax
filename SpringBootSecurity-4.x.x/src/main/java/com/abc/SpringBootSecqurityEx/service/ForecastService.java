package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.entity.Forecast;
import com.abc.SpringBootSecqurityEx.entity.LineTarget;
import com.abc.SpringBootSecqurityEx.repository.ForecastRepository;
import com.abc.SpringBootSecqurityEx.repository.LineTargetRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ForecastService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final ForecastRepository forecastRepo;
    private final LineTargetRepository lineTargetRepo;

    public ForecastService(ForecastRepository forecastRepo, LineTargetRepository lineTargetRepo) {
        this.forecastRepo = forecastRepo; this.lineTargetRepo = lineTargetRepo;
    }

    public Page<Forecast> findAll(Pageable p) { return forecastRepo.findAll(p); }
    public Page<Forecast> findByModel(String modelCode, Pageable p) { return forecastRepo.findByModelCode(modelCode, p); }
    public Page<Forecast> findByStatus(String status, Pageable p) { return forecastRepo.findByStatus(status, p); }
    public Forecast findById(Long id) { return forecastRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Forecast not found: " + id)); }

    @Transactional
    public Forecast create(Forecast f) {
        // Combine: final = historical + dealer + manager adjustment
        int finalForecast = f.getHistoricalDemand() + f.getDealerForecast() + f.getManagerAdjustment();
        f.setFinalForecast(finalForecast);
        f.setForecastNumber("FCST-" + LocalDate.now().getYear() + "-" + String.format("%05d", SEQ.incrementAndGet()));
        f.setStatus("DRAFT");
        return forecastRepo.save(f);
    }

    @Transactional
    public Forecast submitForApproval(Long id) {
        Forecast f = findById(id);
        if (!"DRAFT".equals(f.getStatus())) throw new IllegalStateException("Only DRAFT forecasts can be submitted");
        f.setStatus("PENDING_APPROVAL");
        return forecastRepo.save(f);
    }

    @Transactional
    public Forecast approve(Long id, String approvedBy) {
        Forecast f = findById(id);
        if (!"PENDING_APPROVAL".equals(f.getStatus())) throw new IllegalStateException("Only PENDING_APPROVAL can be approved");
        f.setStatus("APPROVED"); f.setApprovedBy(approvedBy);
        return forecastRepo.save(f);
    }

    @Transactional
    public Forecast adjust(Long id, int adjustment, String reason, String adjustedBy) {
        Forecast f = findById(id);
        if ("APPROVED".equals(f.getStatus())) {
            // Create a new version; supersede old
            f.setStatus("SUPERSEDED");
            forecastRepo.save(f);
            Forecast v2 = Forecast.builder()
                .periodStart(f.getPeriodStart()).periodEnd(f.getPeriodEnd())
                .modelCode(f.getModelCode()).variant(f.getVariant())
                .historicalDemand(f.getHistoricalDemand()).dealerForecast(f.getDealerForecast())
                .managerAdjustment(adjustment).adjustmentReason(reason)
                .finalForecast(f.getHistoricalDemand() + f.getDealerForecast() + adjustment)
                .status("DRAFT").remarks("Adjusted from " + f.getForecastNumber() + " by " + adjustedBy).build();
            return create(v2);
        }
        f.setManagerAdjustment(adjustment);
        f.setAdjustmentReason(reason);
        f.setFinalForecast(f.getHistoricalDemand() + f.getDealerForecast() + adjustment);
        return forecastRepo.save(f);
    }

    // Line targets
    public List<LineTarget> lineTargets(LocalDate from, LocalDate to) {
        return lineTargetRepo.findByTargetDateBetween(from, to);
    }
    public List<LineTarget> lineTargetsByLine(String lineCode, LocalDate from, LocalDate to) {
        return lineTargetRepo.findByLineCodeAndTargetDateBetween(lineCode, from, to);
    }

    @Transactional
    public LineTarget recordActual(Long targetId, int actual, int rejected, int downtimeMinutes) {
        LineTarget t = lineTargetRepo.findById(targetId)
            .orElseThrow(() -> new EntityNotFoundException("Line target not found: " + targetId));
        t.setActualQuantity(actual);
        t.setRejectedQuantity(rejected);
        t.setDowntimeMinutes(downtimeMinutes);
        return lineTargetRepo.save(t);
    }

    @Transactional
    public LineTarget setTarget(LineTarget t) {
        return lineTargetRepo.save(t);
    }
}
