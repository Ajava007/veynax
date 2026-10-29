package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.dtos.ShipmentDtos.*;
import com.abc.SpringBootSecqurityEx.entity.*;
import com.abc.SpringBootSecqurityEx.repository.*;
import com.abc.SpringBootSecqurityEx.util.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ShipmentService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final ShipmentRepository shipmentRepo;
    private final ContainerRepository containerRepo;
    private final CustomsDeclarationRepository customsRepo;
    private final LandedCostRepository landedCostRepo;
    private final DamageRecordRepository damageRepo;
    private final SupplierClaimRepository claimRepo;
    private final PurchaseOrderRepository poRepo;

    public ShipmentService(ShipmentRepository shipmentRepo, ContainerRepository containerRepo,
            CustomsDeclarationRepository customsRepo, LandedCostRepository landedCostRepo,
            DamageRecordRepository damageRepo, SupplierClaimRepository claimRepo,
            PurchaseOrderRepository poRepo) {
        this.shipmentRepo = shipmentRepo; this.containerRepo = containerRepo;
        this.customsRepo = customsRepo; this.landedCostRepo = landedCostRepo;
        this.damageRepo = damageRepo; this.claimRepo = claimRepo; this.poRepo = poRepo;
    }

    public Page<Shipment> findAll(Pageable pageable) { return shipmentRepo.findAll(pageable); }
    public Shipment findById(Long id) { return shipmentRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Shipment not found: " + id)); }
    public Page<DamageRecord> findDamages(String poNumber, Pageable pageable) { return damageRepo.findByPoNumber(poNumber, pageable); }
    public Page<SupplierClaim> findClaims(String status, Pageable pageable) { return status != null ? claimRepo.findByStatus(status, pageable) : claimRepo.findAll(pageable); }

    @Transactional
    public Shipment create(ShipmentRequest req) {
        String shipmentNumber = "SHP-" + LocalDate.now().getYear() + "-" + String.format("%05d", SEQ.incrementAndGet());
        Shipment s = Shipment.builder()
            .shipmentNumber(shipmentNumber).poNumber(req.poNumber()).supplierCode(req.supplierCode())
            .vesselName(req.vesselName()).voyageNumber(req.voyageNumber())
            .portOfLoading(req.portOfLoading()).portOfDischarge(req.portOfDischarge())
            .etd(req.etd()).eta(req.eta())
            .commercialInvoiceRef(req.commercialInvoiceRef()).packingListRef(req.packingListRef())
            .status(req.status() != null ? req.status() : "PLANNED").build();
        s = shipmentRepo.save(s);
        if (req.containers() != null) {
            for (ContainerRequest cr : req.containers()) {
                Container c = Container.builder().shipment(s).containerNumber(cr.containerNumber())
                    .sealNumber(cr.sealNumber()).containerType(cr.containerType())
                    .grossWeightKg(cr.grossWeightKg()).status(cr.status() != null ? cr.status() : "SEALED").build();
                containerRepo.save(c);
            }
        }
        return s;
    }

    @Transactional
    public Container addContainer(Long shipmentId, ContainerRequest req) {
        Shipment s = findById(shipmentId);
        Container c = Container.builder().shipment(s).containerNumber(req.containerNumber())
            .sealNumber(req.sealNumber()).containerType(req.containerType())
            .grossWeightKg(req.grossWeightKg()).status(req.status() != null ? req.status() : "SEALED").build();
        return containerRepo.save(c);
    }

    @Transactional
    public DamageRecord recordReceiving(ReceivingRequest req) {
        Shipment s = findById(req.shipmentId());
        // Update PO item receiving quantities
        PurchaseOrder po = poRepo.findByPoNumber(s.getPoNumber())
            .orElseThrow(() -> new EntityNotFoundException("PO not found: " + s.getPoNumber()));
        PurchaseOrderItem item = po.getItems().stream()
            .filter(i -> i.getPartCode().equals(req.partCode())).findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Part not on PO: " + req.partCode()));
        item.setReceivedQuantity(req.receivedQuantity());
        item.setRejectedQuantity(req.rejectedQuantity() != null ? req.rejectedQuantity() : 0);
        item.setDamagedQuantity(req.damagedQuantity() != null ? req.damagedQuantity() : 0);
        item.setMissingQuantity(req.missingQuantity() != null ? req.missingQuantity() : 0);
        poRepo.save(po);

        // Auto-create damage record + supplier claim when damage or shortage exists
        int damaged = req.damagedQuantity() != null ? req.damagedQuantity() : 0;
        int missing = req.missingQuantity() != null ? req.missingQuantity() : 0;
        if (damaged > 0 || missing > 0) {
            String damageNumber = "DMG-" + String.format("%06d", SEQ.incrementAndGet());
            DamageRecord dmg = DamageRecord.builder()
                .damageNumber(damageNumber).poNumber(s.getPoNumber()).shipmentId(s.getId())
                .partCode(req.partCode()).damagedQuantity(damaged + missing)
                .damageType(req.damageType() != null ? req.damageType() : (damaged > 0 ? "DAMAGED" : "SHORTAGE"))
                .responsibleParty(req.responsibleParty() != null ? req.responsibleParty() : "SUPPLIER")
                .estimatedLoss(req.estimatedLoss() != null ? req.estimatedLoss() : BigDecimal.ZERO)
                .evidenceUrl(req.evidenceUrl()).status("QUARANTINED").build();
            dmg = damageRepo.save(dmg);

            // Auto-create supplier claim
            String claimNumber = "CLM-" + String.format("%06d", SEQ.incrementAndGet());
            SupplierClaim claim = SupplierClaim.builder()
                .claimNumber(claimNumber).supplierCode(s.getSupplierCode())
                .damageRecordId(dmg.getId()).claimedAmount(dmg.getEstimatedLoss())
                .filedDate(LocalDate.now()).status("OPEN").build();
            claim = claimRepo.save(claim);
            dmg.setClaimId(claim.getId());
            damageRepo.save(dmg);
            return dmg;
        }
        return null; // no damage, nothing to quarantine
    }

    @Transactional
    public CustomsDeclaration recordCustoms(CustomsRequest req) {
        findById(req.shipmentId());
        CustomsDeclaration c = customsRepo.findByShipmentId(req.shipmentId()).orElse(new CustomsDeclaration());
        c.setShipmentId(req.shipmentId());
        c.setDeclarationNumber(req.declarationNumber());
        c.setDeclarationDate(req.declarationDate());
        c.setDutyAmount(req.dutyAmount() != null ? req.dutyAmount() : BigDecimal.ZERO);
        c.setVatAmount(req.vatAmount() != null ? req.vatAmount() : BigDecimal.ZERO);
        c.setOtherCharges(req.otherCharges() != null ? req.otherCharges() : BigDecimal.ZERO);
        c.setStatus(req.status() != null ? req.status() : "SUBMITTED");
        c.setHeldReason(req.heldReason());
        return customsRepo.save(c);
    }

    @Transactional
    public LandedCost recordLandedCost(LandedCostRequest req) {
        findById(req.shipmentId());
        LandedCost lc = landedCostRepo.findByShipmentId(req.shipmentId()).orElse(new LandedCost());
        lc.setShipmentId(req.shipmentId());
        lc.setFreightCost(req.freightCost() != null ? req.freightCost() : BigDecimal.ZERO);
        lc.setInsuranceCost(req.insuranceCost() != null ? req.insuranceCost() : BigDecimal.ZERO);
        lc.setDutyCost(req.dutyCost() != null ? req.dutyCost() : BigDecimal.ZERO);
        lc.setHandlingCost(req.handlingCost() != null ? req.handlingCost() : BigDecimal.ZERO);
        lc.setOtherCost(req.otherCost() != null ? req.otherCost() : BigDecimal.ZERO);
        lc.setCurrency(req.currency() != null ? req.currency() : "USD");
        lc.setAllocationMethod(req.allocationMethod() != null ? req.allocationMethod() : "BY_VALUE");
        lc.setTotalCost(lc.getFreightCost().add(lc.getInsuranceCost()).add(lc.getDutyCost())
            .add(lc.getHandlingCost()).add(lc.getOtherCost()));
        lc.setReconciled(req.reconciled());
        return landedCostRepo.save(lc);
    }
}
