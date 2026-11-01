package com.abc.SpringBootSecqurityEx.service;


import com.abc.SpringBootSecqurityEx.dtos.InventoryDtos;
import com.abc.SpringBootSecqurityEx.entity.InventoryItem;
import com.abc.SpringBootSecqurityEx.entity.MrpRun;
import com.abc.SpringBootSecqurityEx.repository.InventoryItemRepository;
import com.abc.SpringBootSecqurityEx.repository.MrpRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MrpService {

    private final InventoryItemRepository itemRepository;
    private final MrpRunRepository mrpRunRepository;

    public MrpService(InventoryItemRepository itemRepository, MrpRunRepository mrpRunRepository) {
        this.itemRepository = itemRepository;
        this.mrpRunRepository = mrpRunRepository;
    }

    @Transactional
    public List<InventoryDtos.MrpResultResponse> calculate(InventoryDtos.MrpRequest req) {
        String runNumber = "MRP-" + req.planDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.currentTimeMillis() % 10000;
        Map<String, InventoryDtos.MrpDemandLine> demandByPart = new LinkedHashMap<>();
        for (InventoryDtos.MrpDemandLine line : req.demand()) {
            demandByPart.merge(line.partCode().trim(), line, (left, right) -> new InventoryDtos.MrpDemandLine(
                    left.partCode().trim(), left.grossRequirement().add(right.grossRequirement()),
                    (left.scheduledReceipts() == null ? BigDecimal.ZERO : left.scheduledReceipts())
                            .add(right.scheduledReceipts() == null ? BigDecimal.ZERO : right.scheduledReceipts())));
        }
        List<InventoryDtos.MrpResultResponse> results = demandByPart.values().stream().map(line -> {
            BigDecimal usableStock = totalUsableStock(line.partCode());
            BigDecimal receipts = line.scheduledReceipts() != null ? line.scheduledReceipts() : BigDecimal.ZERO;
            // Exclude reservations and unusable stock; combine duplicate demand lines before netting.
            BigDecimal net = line.grossRequirement().subtract(usableStock).subtract(receipts);
            String action = net.compareTo(BigDecimal.ZERO) > 0
                    ? (net.compareTo(line.grossRequirement().multiply(new BigDecimal("0.5"))) > 0 ? "EXPEDITE" : "ORDER")
                    : "NONE";
            MrpRun run = MrpRun.builder()
                    .runNumber(runNumber).planDate(req.planDate()).partCode(line.partCode())
                    .grossRequirement(line.grossRequirement()).onHand(usableStock).scheduledReceipts(receipts)
                    .netShortage(net.max(BigDecimal.ZERO)).action(action).build();
            mrpRunRepository.save(run);
            return new InventoryDtos.MrpResultResponse(runNumber, req.planDate(), line.partCode(), line.grossRequirement(),
                    usableStock, receipts, net, action);
        }).toList();
        return results;
    }

    @Transactional(readOnly = true)
    public List<InventoryDtos.MrpResultResponse> findByRun(String runNumber) {
        return mrpRunRepository.findByRunNumberOrderByNetShortageDesc(runNumber).stream()
                .map(r -> new InventoryDtos.MrpResultResponse(r.getRunNumber(), r.getPlanDate(), r.getPartCode(),
                        r.getGrossRequirement(), r.getOnHand(), r.getScheduledReceipts(), r.getNetShortage(), r.getAction()))
                .toList();
    }

    private BigDecimal totalUsableStock(String partCode) {
        return itemRepository.findByPartCode(partCode).stream()
                .filter(i -> "AVAILABLE".equals(i.getStatus()))
                .map(i -> i.getOnHand().subtract(i.getReserved()).max(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
