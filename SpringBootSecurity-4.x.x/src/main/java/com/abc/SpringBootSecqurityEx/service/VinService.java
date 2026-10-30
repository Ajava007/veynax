package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.entity.VinComponent;
import com.abc.SpringBootSecqurityEx.entity.VinRecord;
import com.abc.SpringBootSecqurityEx.repository.VinComponentRepository;
import com.abc.SpringBootSecqurityEx.repository.VinRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VinService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final VinRecordRepository vinRepo;
    private final VinComponentRepository compRepo;

    public VinService(VinRecordRepository vinRepo, VinComponentRepository compRepo) {
        this.vinRepo = vinRepo; this.compRepo = compRepo;
    }

    public Page<VinRecord> findAll(Pageable p) { return vinRepo.findAll(p); }
    public Page<VinRecord> findByWo(String woNumber, Pageable p) { return vinRepo.findByWoNumber(woNumber, p); }
    public Page<VinRecord> findByStatus(String status, Pageable p) { return vinRepo.findByStatus(status, p); }
    public Page<VinRecord> findByModel(String modelCode, Pageable p) { return vinRepo.findByModelCode(modelCode, p); }
    public VinRecord findByVin(String vin) { return vinRepo.findByVin(vin).orElseThrow(() -> new EntityNotFoundException("VIN not found: " + vin)); }
    public List<VinComponent> genealogy(String vin) { findByVin(vin); return compRepo.findByVin(vin); }

    @Transactional
    public VinRecord createVin(VinRecord v) {
        if (v.getVin() == null || v.getVin().length() != 17) {
            throw new IllegalArgumentException("VIN must be exactly 17 characters");
        }
        if (vinRepo.findByVin(v.getVin()).isPresent()) {
            throw new IllegalStateException("VIN already exists: " + v.getVin());
        }
        v.setStatus("IN_BUILD");
        v.setBuildDate(LocalDate.now());
        return vinRepo.save(v);
    }

    @Transactional
    public VinComponent installComponent(String vin, VinComponent comp) {
        VinRecord v = findByVin(vin);
        if ("RELEASED".equals(v.getStatus()) || "DELIVERED".equals(v.getStatus())) {
            throw new IllegalStateException("Cannot install components on a " + v.getStatus() + " vehicle");
        }
        comp.setVin(vin);
        comp.setInstalledAt(LocalDateTime.now());
        return compRepo.save(comp);
    }

    @Transactional
    public VinRecord placeQualityHold(String vin, String reason) {
        VinRecord v = findByVin(vin);
        v.setQualityHold(true);
        v.setHoldReason(reason);
        v.setStatus("QUALITY_HOLD");
        return vinRepo.save(v);
    }

    @Transactional
    public VinRecord releaseFromHold(String vin, String releasedBy) {
        VinRecord v = findByVin(vin);
        if (!v.isQualityHold()) throw new IllegalStateException("VIN is not on quality hold");
        v.setQualityHold(false);
        v.setHoldReason(null);
        v.setStatus("RELEASED");
        v.setReleasedBy(releasedBy);
        v.setReleasedAt(LocalDateTime.now());
        return vinRepo.save(v);
    }

    @Transactional
    public VinRecord markBuilt(String vin) {
        VinRecord v = findByVin(vin);
        if (v.isQualityHold()) throw new IllegalStateException("Cannot mark built - vehicle on quality hold");
        v.setStatus("BUILT");
        v.setCompletedAt(LocalDateTime.now());
        return vinRepo.save(v);
    }

    @Transactional
    public VinRecord allocateToDealer(String vin, String dealerCode) {
        VinRecord v = findByVin(vin);
        if (!"RELEASED".equals(v.getStatus()) && !"BUILT".equals(v.getStatus())) {
            throw new IllegalStateException("Only BUILT or RELEASED vehicles can be allocated");
        }
        if (v.isQualityHold()) throw new IllegalStateException("Vehicle on quality hold - cannot allocate");
        v.setAllocatedToDealer(dealerCode);
        v.setStatus("ALLOCATED");
        return vinRepo.save(v);
    }

    @Transactional
    public VinRecord markDelivered(String vin) {
        VinRecord v = findByVin(vin);
        if (v.isQualityHold()) throw new IllegalStateException("Vehicle on quality hold - cannot deliver");
        if (!"ALLOCATED".equals(v.getStatus())) throw new IllegalStateException("Only ALLOCATED vehicles can be delivered");
        v.setStatus("DELIVERED");
        v.setDeliveredAt(LocalDateTime.now());
        return vinRepo.save(v);
    }
}
