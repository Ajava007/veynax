package com.abc.SpringBootSecqurityEx.service;


import com.abc.SpringBootSecqurityEx.dtos.EngineeringDtos;
import com.abc.SpringBootSecqurityEx.entity.BomRow;
import com.abc.SpringBootSecqurityEx.repository.BomRowRepository;
import com.abc.SpringBootSecqurityEx.repository.PartRepository;
import com.abc.SpringBootSecqurityEx.repository.VehicleModelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
public class BomService {
    private final BomRowRepository repository;
    private final PartRepository parts;
    private final VehicleModelRepository models;

    public BomService(BomRowRepository repository, PartRepository parts, VehicleModelRepository models) {
        this.repository = repository;
        this.parts = parts;
        this.models = models;
    }

    @Transactional(readOnly = true)
    public List<EngineeringDtos.BomRowResponse> findByParent(String parentPartCode) {
        return repository.findByParentPartCodeOrderByLevelAscComponentPartCodeAsc(parentPartCode)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EngineeringDtos.BomRowResponse findById(Long id) { return toResponse(getOrThrow(id)); }

    @Transactional
    public EngineeringDtos.BomRowResponse create(EngineeringDtos.BomRowRequest req) {
        validate(req, null);
        BomRow row = BomRow.builder()
                .parentPartCode(req.parentPartCode().trim()).componentPartCode(req.componentPartCode().trim())
                .quantity(req.quantity()).scrapFactor(req.scrapFactor() != null ? req.scrapFactor() : BigDecimal.ZERO)
                .optional(req.optional() != null ? req.optional() : false)
                .level(req.level() != null ? req.level() : 1)
                .effectiveFrom(req.effectiveFrom()).effectiveTo(req.effectiveTo()).remark(req.remark())
                .build();
        return toResponse(repository.save(row));
    }

    @Transactional
    public EngineeringDtos.BomRowResponse update(Long id, EngineeringDtos.BomRowRequest req) {
        BomRow row = getOrThrow(id);
        validate(req, id);
        row.setParentPartCode(req.parentPartCode().trim());
        row.setComponentPartCode(req.componentPartCode().trim());
        row.setQuantity(req.quantity());
        if (req.scrapFactor() != null) row.setScrapFactor(req.scrapFactor());
        if (req.optional() != null) row.setOptional(req.optional());
        if (req.level() != null) row.setLevel(req.level());
        row.setEffectiveFrom(req.effectiveFrom());
        row.setEffectiveTo(req.effectiveTo());
        row.setRemark(req.remark());
        return toResponse(repository.save(row));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new EntityNotFoundException("BOM row not found: " + id);
        repository.deleteById(id);
    }

    private BomRow getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("BOM row not found: " + id));
    }

    private void validate(EngineeringDtos.BomRowRequest req, Long excludedId) {
        String parent = req.parentPartCode().trim();
        String component = req.componentPartCode().trim();
        if (req.effectiveFrom() != null && req.effectiveTo() != null
                && req.effectiveTo().isBefore(req.effectiveFrom())) {
            throw new IllegalArgumentException("BOM effectiveTo must be on or after effectiveFrom");
        }
        if (!parts.existsByPartCode(parent) && !models.existsByModelCode(parent)) {
            throw new EntityNotFoundException("BOM parent part or vehicle model not found: " + parent);
        }
        if (!parts.existsByPartCode(component)) throw new EntityNotFoundException("Component part not found: " + component);
        if (parent.equalsIgnoreCase(component)) throw new IllegalStateException("Circular BOM not allowed: parent cannot equal component.");

        for (BomRow existing : repository.findByParentPartCode(parent)) {
            if (!existing.getComponentPartCode().equalsIgnoreCase(component)
                    || (excludedId != null && excludedId.equals(existing.getId()))) continue;
            if (rangesOverlap(req.effectiveFrom(), req.effectiveTo(), existing.getEffectiveFrom(), existing.getEffectiveTo())) {
                throw new IllegalStateException("BOM component already has an overlapping effectivity range: " + parent + " / " + component);
            }
        }
        if (wouldCreateCycle(parent, component, excludedId, new HashSet<>())) {
            throw new IllegalStateException("Circular BOM not allowed: adding " + parent + " / " + component + " creates a cycle.");
        }
    }

    private boolean rangesOverlap(LocalDate fromA, LocalDate toA, LocalDate fromB, LocalDate toB) {
        LocalDate startA = fromA == null ? LocalDate.MIN : fromA;
        LocalDate endA = toA == null ? LocalDate.MAX : toA;
        LocalDate startB = fromB == null ? LocalDate.MIN : fromB;
        LocalDate endB = toB == null ? LocalDate.MAX : toB;
        return !startA.isAfter(endB) && !startB.isAfter(endA);
    }

    private boolean wouldCreateCycle(String target, String current, Long excludedId, Set<String> visited) {
        if (target.equalsIgnoreCase(current)) return true;
        String key = current.toUpperCase(java.util.Locale.ROOT);
        if (!visited.add(key)) return false;
        for (BomRow row : repository.findByParentPartCode(current)) {
            if (excludedId != null && excludedId.equals(row.getId())) continue;
            if (wouldCreateCycle(target, row.getComponentPartCode(), excludedId, visited)) return true;
        }
        return false;
    }

    private EngineeringDtos.BomRowResponse toResponse(BomRow r) {
        return new EngineeringDtos.BomRowResponse(r.getId(), r.getParentPartCode(), r.getComponentPartCode(), r.getQuantity(),
                r.getScrapFactor(), r.isOptional(), r.getLevel(), r.getEffectiveFrom(), r.getEffectiveTo(),
                r.getRemark(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
