package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.dtos.EngineeringDtos;
import com.abc.SpringBootSecqurityEx.entity.BomRow;
import com.abc.SpringBootSecqurityEx.repository.BomRowRepository;
import com.abc.SpringBootSecqurityEx.repository.PartRepository;
import com.abc.SpringBootSecqurityEx.repository.VehicleModelRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BomServiceTest {
    private final BomRowRepository rows = mock(BomRowRepository.class);
    private final PartRepository parts = mock(PartRepository.class);
    private final VehicleModelRepository models = mock(VehicleModelRepository.class);
    private final BomService service = new BomService(rows, parts, models);

    @Test
    void rejectsMultiLevelCycles() {
        when(parts.existsByPartCode("A")).thenReturn(true);
        when(parts.existsByPartCode("B")).thenReturn(true);
        when(models.existsByModelCode("B")).thenReturn(false);
        when(rows.findByParentPartCode("B")).thenReturn(List.of());
        when(rows.findByParentPartCode("A")).thenReturn(List.of(BomRow.builder()
                .id(1L).parentPartCode("A").componentPartCode("B").build()));

        assertThrows(IllegalStateException.class, () -> service.create(request("B", "A", null, null)));
    }

    @Test
    void rejectsOverlappingEffectivityForTheSameComponent() {
        when(parts.existsByPartCode("A")).thenReturn(true);
        when(parts.existsByPartCode("B")).thenReturn(true);
        when(models.existsByModelCode("B")).thenReturn(false);
        when(rows.findByParentPartCode("A")).thenReturn(List.of(BomRow.builder()
                .id(1L).parentPartCode("A").componentPartCode("B")
                .effectiveFrom(LocalDate.of(2026, 1, 1)).effectiveTo(LocalDate.of(2026, 12, 31)).build()));

        assertThrows(IllegalStateException.class, () -> service.create(request("A", "B",
                LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 1))));
    }

    private EngineeringDtos.BomRowRequest request(String parent, String component, LocalDate from, LocalDate to) {
        return new EngineeringDtos.BomRowRequest(parent, component, BigDecimal.ONE, BigDecimal.ZERO,
                false, 1, from, to, null);
    }
}
