package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.dtos.SalesDtos.*;
import com.abc.SpringBootSecqurityEx.entity.*;
import com.abc.SpringBootSecqurityEx.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class QuotationService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final QuotationRepository quotationRepo;
    private final DealerPriceRepository priceRepo;
    private final DealerRepository dealerRepo;
    private final SalesOrderRepository salesOrderRepo;

    public QuotationService(QuotationRepository quotationRepo, DealerPriceRepository priceRepo,
            DealerRepository dealerRepo, SalesOrderRepository salesOrderRepo) {
        this.quotationRepo = quotationRepo; this.priceRepo = priceRepo;
        this.dealerRepo = dealerRepo; this.salesOrderRepo = salesOrderRepo;
    }

    public Page<Quotation> findAll(Pageable p) { return quotationRepo.findAll(p); }
    public Page<Quotation> findByDealer(String dealerCode, Pageable p) { return quotationRepo.findByDealerCode(dealerCode, p); }
    public Page<Quotation> findByStatus(String status, Pageable p) { return quotationRepo.findByStatus(status, p); }
    public Quotation findById(Long id) { return quotationRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id)); }
    public Quotation findByNumber(String qn) { return quotationRepo.findByQuotationNumber(qn).orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + qn)); }

    @Transactional
    public Quotation create(QuotationRequest req) {
        // Look up dealer-specific price; fall back to requested unit price
        BigDecimal unitPrice = req.unitPrice();
        priceRepo.findFirstByDealerCodeAndModelCodeAndStatusOrderByCreatedAtDesc(req.dealerCode(), req.modelCode(), "ACTIVE")
            .ifPresent(p -> { /* use dealer price if no explicit price */ });
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.quantity()));
        BigDecimal total = subtotal
            .add(req.accessoriesCost() != null ? req.accessoriesCost() : BigDecimal.ZERO)
            .add(req.transportCost() != null ? req.transportCost() : BigDecimal.ZERO)
            .add(req.taxAmount() != null ? req.taxAmount() : BigDecimal.ZERO);

        String qn = "QTN-" + LocalDate.now().getYear() + "-" + String.format("%05d", SEQ.incrementAndGet());
        Quotation q = Quotation.builder()
            .quotationNumber(qn).dealerCode(req.dealerCode()).modelCode(req.modelCode())
            .variant(req.variant()).color(req.color()).quantity(req.quantity())
            .unitPrice(unitPrice).accessoriesCost(req.accessoriesCost())
            .transportCost(req.transportCost()).taxAmount(req.taxAmount())
            .totalAmount(total).status("DRAFT")
            .validUntil(req.validUntil() != null ? req.validUntil() : LocalDate.now().plusDays(30))
            .remarks(req.remarks()).build();
        return quotationRepo.save(q);
    }

    @Transactional
    public Quotation submitForApproval(Long id) {
        Quotation q = findById(id);
        if (!"DRAFT".equals(q.getStatus())) throw new IllegalStateException("Only DRAFT quotations can be submitted");
        q.setStatus("PENDING_APPROVAL");
        return quotationRepo.save(q);
    }

    @Transactional
    public Quotation approve(Long id, String approvedBy) {
        Quotation q = findById(id);
        if (!"PENDING_APPROVAL".equals(q.getStatus())) throw new IllegalStateException("Only PENDING_APPROVAL can be approved");
        q.setStatus("APPROVED"); q.setApprovedBy(approvedBy); q.setApprovedAt(LocalDate.now());
        return quotationRepo.save(q);
    }

    @Transactional
    public Quotation reject(Long id, String reason) {
        Quotation q = findById(id);
        if (!"PENDING_APPROVAL".equals(q.getStatus())) throw new IllegalStateException("Only PENDING_APPROVAL can be rejected");
        q.setStatus("REJECTED"); q.setRejectReason(reason);
        return quotationRepo.save(q);
    }

    @Transactional
    public SalesOrder convertToOrder(Long quotationId) {
        Quotation q = findById(quotationId);
        if (!"APPROVED".equals(q.getStatus())) throw new IllegalStateException("Only APPROVED quotations can be converted");
        // Calculate advance payment from dealer terms
        Dealer dealer = dealerRepo.findByCode(q.getDealerCode())
            .orElseThrow(() -> new EntityNotFoundException("Dealer not found: " + q.getDealerCode()));
        BigDecimal advancePct = dealer.getAdvancePaymentPct() != null ? dealer.getAdvancePaymentPct() : new BigDecimal("30");
        BigDecimal advanceRequired = q.getTotalAmount().multiply(advancePct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        String soNumber = "SO-" + String.format("%06d", SEQ.incrementAndGet());
        SalesOrder so = new SalesOrder();
        so.setSoNumber(soNumber);
        so.setDealerCode(q.getDealerCode());
        so.setModelCode(q.getModelCode());
        so.setOrderDate(LocalDate.now());
        so.setTotalAmount(q.getTotalAmount());
        so.setStatus("CONFIRMED");
        so.setPaymentStatus("PENDING");
        so.setQuotationId(q.getId());
        so.setAdvancePct(advancePct);
        so.setAdvanceRequired(advanceRequired);
        so.setAdvanceReceived(BigDecimal.ZERO);
        so = salesOrderRepo.save(so);

        q.setStatus("CONVERTED");
        q.setConvertedSoNumber(soNumber);
        quotationRepo.save(q);
        return so;
    }

    @Transactional
    public SalesOrder recordAdvancePayment(String soNumber, BigDecimal amount) {
        SalesOrder so = salesOrderRepo.findBySoNumber(soNumber)
            .orElseThrow(() -> new EntityNotFoundException("Sales order not found: " + soNumber));
        BigDecimal newReceived = so.getAdvanceReceived().add(amount);
        so.setAdvanceReceived(newReceived);
        if (newReceived.compareTo(so.getAdvanceRequired()) >= 0) {
            so.setPaymentStatus("ADVANCE_PAID");
        }
        return salesOrderRepo.save(so);
    }
}
