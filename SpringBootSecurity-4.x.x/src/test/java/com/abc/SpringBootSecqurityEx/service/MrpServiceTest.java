package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.dtos.InventoryDtos;
import com.abc.SpringBootSecqurityEx.entity.InventoryItem;
import com.abc.SpringBootSecqurityEx.entity.MrpRun;
import com.abc.SpringBootSecqurityEx.repository.InventoryItemRepository;
import com.abc.SpringBootSecqurityEx.repository.MrpRunRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MrpServiceTest {
    private final InventoryItemRepository items = mock(InventoryItemRepository.class);
    private final MrpRunRepository runs = mock(MrpRunRepository.class);
    private final MrpService service = new MrpService(items, runs);

    @Test
    void netsReservedStockAndCombinesDuplicateDemandLines() {
        InventoryItem available = InventoryItem.builder()
                .partCode("PART-1").warehouse("WH-1").onHand(new BigDecimal("10"))
                .reserved(new BigDecimal("3")).status("AVAILABLE").build();
        InventoryItem quarantined = InventoryItem.builder()
                .partCode("PART-1").warehouse("WH-1").onHand(new BigDecimal("50"))
                .reserved(BigDecimal.ZERO).status("QUARANTINE").build();
        when(items.findByPartCode("PART-1")).thenReturn(List.of(available, quarantined));
        when(runs.save(any(MrpRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryDtos.MrpRequest request = new InventoryDtos.MrpRequest(LocalDate.of(2026, 10, 30), List.of(
                new InventoryDtos.MrpDemandLine("PART-1", new BigDecimal("6"), BigDecimal.ONE),
                new InventoryDtos.MrpDemandLine("PART-1", new BigDecimal("4"), BigDecimal.ONE)));

        List<InventoryDtos.MrpResultResponse> result = service.calculate(request);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("10"), result.get(0).grossRequirement());
        assertEquals(new BigDecimal("7"), result.get(0).onHand());
        assertEquals(new BigDecimal("2"), result.get(0).scheduledReceipts());
        assertEquals(new BigDecimal("1"), result.get(0).netShortage());
        assertEquals("ORDER", result.get(0).action());
    }
}
