package com.abc.SpringBootSecqurityEx.service;


import com.abc.SpringBootSecqurityEx.dtos.InventoryDtos;
import com.abc.SpringBootSecqurityEx.entity.InventoryItem;
import com.abc.SpringBootSecqurityEx.entity.StockTransaction;
import com.abc.SpringBootSecqurityEx.repository.InventoryItemRepository;
import com.abc.SpringBootSecqurityEx.repository.StockTransactionRepository;
import com.abc.SpringBootSecqurityEx.repository.MaterialReservationRepository;
import com.abc.SpringBootSecqurityEx.entity.MaterialReservation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class InventoryService {

    private static final Set<String> OUTBOUND_TYPES = Set.of("ISSUE", "SCRAP");
    private static final Set<String> INBOUND_TYPES = Set.of("RECEIPT", "ADJUST");

    private final InventoryItemRepository itemRepository;
    private final StockTransactionRepository txnRepository;
    private final MaterialReservationRepository reservationRepo;
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    public InventoryService(InventoryItemRepository itemRepository, StockTransactionRepository txnRepository, MaterialReservationRepository reservationRepo) {
        this.reservationRepo = reservationRepo;
        this.itemRepository = itemRepository;
        this.txnRepository = txnRepository;
    }

    @Transactional(readOnly = true)
    public Page<InventoryDtos.InventoryItemResponse> findAll(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::toItemResponse);
    }

    @Transactional(readOnly = true)
    public List<InventoryDtos.StockTransactionResponse> findTransactions(String partCode) {
        return txnRepository.findByPartCodeOrderByCreatedAtDesc(partCode).stream().map(this::toTxnResponse).toList();
    }

    @Transactional
    public InventoryDtos.InventoryItemResponse adjust(InventoryDtos.StockAdjustRequest req) {
        String type = req.txnType().toUpperCase();
        if (!OUTBOUND_TYPES.contains(type) && !INBOUND_TYPES.contains(type)) {
            throw new IllegalStateException("Unsupported transaction type: " + type);
        }
        InventoryItem item = itemRepository
                .findByPartCodeAndWarehouseAndLotNumber(req.partCode(), req.warehouse(), req.lotNumber())
                .orElseGet(() -> InventoryItem.builder()
                        .partCode(req.partCode()).warehouse(req.warehouse()).lotNumber(req.lotNumber())
                        .onHand(BigDecimal.ZERO).reserved(BigDecimal.ZERO).status("AVAILABLE").build());

        if ("QUARANTINE".equals(item.getStatus()) || "BLOCKED".equals(item.getStatus())) {
            throw new IllegalStateException("Stock is " + item.getStatus() + " and cannot be moved.");
        }

        BigDecimal signedQty = OUTBOUND_TYPES.contains(type) ? req.quantity().negate() : req.quantity();
        BigDecimal newOnHand = item.getOnHand().add(signedQty);
        if (newOnHand.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient stock: on hand " + item.getOnHand() + ", requested " + req.quantity());
        }
        item.setOnHand(newOnHand);

        // Immutable transaction log — append only
        txnRepository.save(StockTransaction.builder()
                .partCode(req.partCode()).warehouse(req.warehouse()).txnType(type)
                .quantity(signedQty).referenceType(req.referenceType()).referenceId(req.referenceId())
                .remark(req.remark()).build());

        return toItemResponse(itemRepository.save(item));
    }

    private InventoryDtos.InventoryItemResponse toItemResponse(InventoryItem i) {
        return new InventoryDtos.InventoryItemResponse(i.getId(), i.getPartCode(), i.getWarehouse(), i.getLocation(),
                i.getLotNumber(), i.getSerialNumber(), i.getOnHand(), i.getReserved(),
                i.getOnHand().subtract(i.getReserved()), i.getStatus(), i.getCreatedAt(), i.getUpdatedAt());
    }

    private InventoryDtos.StockTransactionResponse toTxnResponse(StockTransaction t) {
        return new InventoryDtos.StockTransactionResponse(t.getId(), t.getPartCode(), t.getWarehouse(), t.getTxnType(),
                t.getQuantity(), t.getReferenceType(), t.getReferenceId(), t.getRemark(), t.getCreatedAt());
    }

    // --- Phase 4: no-negative-stock enforcement ---
    private void assertNoNegativeStock(String partCode, String warehouse, BigDecimal newOnHand) {
        if (newOnHand.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Negative stock prevented: part=" + partCode + " warehouse=" + warehouse +
                " would go to " + newOnHand);
        }
    }

    // --- Phase 4: reserve material for work order ---
    @org.springframework.transaction.annotation.Transactional
    public MaterialReservation reserve(InventoryDtos.ReserveRequest req) {
        InventoryItem item = itemRepository.findByPartCodeAndWarehouse(req.partCode(), req.warehouse())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Stock not found: " + req.partCode() + " @ " + req.warehouse()));
        BigDecimal available = item.getOnHand().subtract(item.getReserved());
        if (available.compareTo(req.quantity()) < 0) {
            throw new IllegalStateException("Insufficient available stock. Available=" + available + " requested=" + req.quantity());
        }
        if (!"AVAILABLE".equals(item.getStatus())) {
            throw new IllegalStateException("Stock is " + item.getStatus() + " - cannot reserve");
        }
        item.setReserved(item.getReserved().add(req.quantity()));
        itemRepository.save(item);

        String resNumber = "RES-" + String.format("%06d", SEQ.incrementAndGet());
        MaterialReservation r = MaterialReservation.builder()
            .reservationNumber(resNumber).partCode(req.partCode())
            .workOrderNumber(req.workOrderNumber()).warehouse(req.warehouse())
            .lotNumber(req.lotNumber()).quantity(req.quantity()).status("RESERVED")
            .requiredBy(req.requiredBy()).reservedBy(req.reservedBy()).remark(req.remark()).build();
        return reservationRepo.save(r);
    }

    // --- Phase 4: issue (kit) reserved material to work order ---
    @org.springframework.transaction.annotation.Transactional
    public MaterialReservation issue(String reservationNumber) {
        MaterialReservation r = reservationRepo.findByReservationNumber(reservationNumber)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Reservation not found: " + reservationNumber));
        if (!"RESERVED".equals(r.getStatus())) {
            throw new IllegalStateException("Reservation is " + r.getStatus() + " - cannot issue");
        }
        InventoryItem item = itemRepository.findByPartCodeAndWarehouse(r.getPartCode(), r.getWarehouse())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Stock not found"));
        BigDecimal newOnHand = item.getOnHand().subtract(r.getQuantity());
        assertNoNegativeStock(r.getPartCode(), r.getWarehouse(), newOnHand);
        item.setOnHand(newOnHand);
        item.setReserved(item.getReserved().subtract(r.getQuantity()));
        itemRepository.save(item);

        StockTransaction txn = new StockTransaction();
        txn.setPartCode(r.getPartCode());
        txn.setWarehouse(r.getWarehouse());
        txn.setTxnType("ISSUE");
        txn.setQuantity(r.getQuantity().negate());
        txn.setReferenceType("WO");
        txn.setReferenceId(r.getWorkOrderNumber());
        txn.setRemark("Kitted to work order via reservation " + r.getReservationNumber());
        txnRepository.save(txn);

        r.setStatus("ISSUED");
        return reservationRepo.save(r);
    }

    // --- Phase 4: cancel reservation ---
    @org.springframework.transaction.annotation.Transactional
    public MaterialReservation cancel(String reservationNumber) {
        MaterialReservation r = reservationRepo.findByReservationNumber(reservationNumber)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Reservation not found: " + reservationNumber));
        if (!"RESERVED".equals(r.getStatus())) {
            throw new IllegalStateException("Reservation is " + r.getStatus() + " - cannot cancel");
        }
        InventoryItem item = itemRepository.findByPartCodeAndWarehouse(r.getPartCode(), r.getWarehouse())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Stock not found"));
        item.setReserved(item.getReserved().subtract(r.getQuantity()));
        itemRepository.save(item);
        r.setStatus("CANCELLED");
        return reservationRepo.save(r);
    }

    // --- Phase 4: inventory valuation ---
    public BigDecimal valuation() {
        return itemRepository.findAll().stream()
            .map(i -> i.getOnHand().multiply(java.math.BigDecimal.ONE)) // would multiply by standard cost from Part
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public org.springframework.data.domain.Page<MaterialReservation> allReservations(org.springframework.data.domain.Pageable p) { return reservationRepo.findAll(p); }
    public org.springframework.data.domain.Page<MaterialReservation> reservationsByStatus(String s, org.springframework.data.domain.Pageable p) { return reservationRepo.findByStatus(s, p); }
    public org.springframework.data.domain.Page<MaterialReservation> reservationsByWorkOrder(String wo, org.springframework.data.domain.Pageable p) { return reservationRepo.findByWorkOrderNumber(wo, p); }

}
