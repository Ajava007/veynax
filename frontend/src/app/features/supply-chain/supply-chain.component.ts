import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, finalize, of } from 'rxjs';
import { AppDataService } from '../../core/services/app-data.service';
import { BackendApiService } from '../../core/services/backend-api.service';
import { AuthService } from '../../core/services/auth.service';

type Tab = 'suppliers' | 'pos';

@Component({
  selector: 'vx-supply-chain',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './supply-chain.component.html',
})
export class SupplyChainComponent {
  tab = signal<Tab>('suppliers');
  suppliersLoading = signal(true);
  purchaseOrdersLoading = signal(true);
  inventoryLoading = signal(true);
  partial = signal(false);
  actionError = signal('');
  actionBusy = signal<number | null>(null);
  supplierFormOpen = signal(false);
  editingSupplierId = signal<number | null>(null);
  supplierBusy = signal(false);
  supplierError = signal('');
  supplierForm = this.fb.group({ code: ['', [Validators.required, Validators.maxLength(30)]], company: ['', [Validators.required, Validators.maxLength(150)]], category: ['', Validators.maxLength(60)], country: ['', Validators.maxLength(60)], rating: [0, [Validators.min(0), Validators.max(5)]], contactEmail: ['', [Validators.email, Validators.maxLength(120)]], phone: ['', Validators.maxLength(30)], status: ['ACTIVE', Validators.required] });

  readonly plantStages = [
    { icon: 'bi-bezier2', title: 'Press shop', make: 'Stamp body panels', buy: 'Steel and aluminum coil' },
    { icon: 'bi-tools', title: 'Body shop', make: 'Weld the body-in-white', buy: 'Fixtures and weld consumables' },
    { icon: 'bi-droplet-half', title: 'Paint shop', make: 'Pretreat, prime and paint', buy: 'Paint, sealers and chemicals' },
    { icon: 'bi-car-front', title: 'Final assembly', make: 'Install, connect and test', buy: 'Modules, powertrain and electronics' }
  ];

  readonly sourcingCategories = [
    { group: 'Raw material', examples: 'Steel coil, aluminum, paint', delivery: 'Scheduled bulk', buffer: 'Days to weeks', icon: 'bi-layers' },
    { group: 'Major modules', examples: 'Seats, cockpit, bumpers, glass', delivery: 'Just in sequence', buffer: 'Hours', icon: 'bi-box-seam' },
    { group: 'Powertrain', examples: 'Engines, transmissions, batteries', delivery: 'Spec matched', buffer: 'Hours to days', icon: 'bi-gear-wide-connected' },
    { group: 'Electronics', examples: 'Harnesses, ECUs, sensors', delivery: 'Scheduled; serialize critical parts', buffer: 'Days', icon: 'bi-cpu' },
    { group: 'Chassis', examples: 'Axles, brakes, wheels, tires', delivery: 'Just in time / sequence', buffer: 'Hours to days', icon: 'bi-truck' },
    { group: 'Standard parts', examples: 'Fasteners, clips, seals', delivery: 'Kanban or min/max', buffer: 'Days to weeks', icon: 'bi-nut' },
    { group: 'Indirect supplies', examples: 'Fluids, tools, spares, packaging', delivery: 'Replenish or request', buffer: 'Weeks / as needed', icon: 'bi-wrench-adjustable' }
  ];

  readonly loopStages = [
    { icon: 'bi-graph-up-arrow', label: 'Forecast', detail: 'Explode the build plan through the current BOM.' },
    { icon: 'bi-diagram-3', label: 'Source', detail: 'Approve suppliers, parts, terms and capacity.' },
    { icon: 'bi-file-earmark-text', label: 'Release', detail: 'Send purchase orders and firm call-offs.' },
    { icon: 'bi-broadcast', label: 'Ship notice', detail: 'Supplier sends ASN and shipment details.' },
    { icon: 'bi-box-arrow-in-down', label: 'Receive', detail: 'Count, scan and match the delivery.' },
    { icon: 'bi-shield-check', label: 'Inspect', detail: 'Accept into stock or quarantine the lot.' },
    { icon: 'bi-receipt', label: 'Three-way match', detail: 'Match order, receipt and invoice before payment.' },
    { icon: 'bi-arrow-repeat', label: 'Review', detail: 'Score performance and tune the next plan.' }
  ];

  readonly deliveryMethods = [
    { name: 'MRP', description: 'Explodes BOM demand, stock and open supply into dated requirements.', fit: 'Planned parts', icon: 'bi-calculator' },
    { name: 'Just in time', description: 'Frequent, smaller deliveries arrive close to consumption.', fit: 'Steady-volume parts', icon: 'bi-clock-history' },
    { name: 'Just in sequence', description: 'Supplier ships parts in the exact order of vehicles on the line.', fit: 'Variant-specific modules', icon: 'bi-list-ol' },
    { name: 'Kanban', description: 'A bin or scan signal triggers replenishment when stock is used.', fit: 'Common small parts', icon: 'bi-arrow-down-up' },
    { name: 'Min / max', description: 'Reorder at minimum and replenish to a defined maximum.', fit: 'Consumables and slow movers', icon: 'bi-sliders' },
    { name: 'Consignment', description: 'Supplier owns material at the plant until it is consumed.', fit: 'Fast-moving parts', icon: 'bi-box2-heart' }
  ];

  readonly receivingSteps = [
    'ASN lists shipment contents before the truck reaches the dock.',
    'Dock slot and arrival variance are recorded for delivery performance.',
    'Scan labels and match part, quantity and revision to the order.',
    'Inspect samples or critical lots; quarantine anything suspect.',
    'Put away to a location, line-side supermarket or direct feed.',
    'Deliver bins or sequenced racks to the station that needs them.',
    'Book consumption to the work order and VIN to trigger replenishment.',
    'Track returnable containers and cycle-count stock to correct drift.'
  ];

  readonly riskRows = [
    { risk: 'Single-source failure', impact: 'Line stops for a critical part', response: 'Second source, safety stock and supplier risk review' },
    { risk: 'Late or short shipment', impact: 'Station runs out of material', response: 'Track confirmed dates; escalate shortages early' },
    { risk: 'Wrong BOM revision', impact: 'Part does not fit or pass inspection', response: 'Tie order release to effective engineering revision' },
    { risk: 'Supplier quality escape', impact: 'Rework, warranty or recall exposure', response: 'Incoming checks, lot trace and containment' },
    { risk: 'Stock record drift', impact: 'False shortage or excess inventory', response: 'Scan transactions, cycle counts and reconciliation' },
    { risk: 'Demand or price change', impact: 'Excess stock or margin loss', response: 'Shorter forecast cycles and agreement price terms' }
  ];

  readonly systemNeeds = [
    { title: 'BOM-linked demand', detail: 'Part, quantity, model, variant and effective revision.', icon: 'bi-diagram-2' },
    { title: 'Supplier and agreements', detail: 'Approved sources, lead time, price, capacity and score.', icon: 'bi-building-check' },
    { title: 'Inventory and supply', detail: 'On-hand by location/lot, reservations and dated open orders.', icon: 'bi-boxes' },
    { title: 'Receiving and quality', detail: 'ASN, goods receipt, inspection result and quarantine state.', icon: 'bi-clipboard2-check' },
    { title: 'Consumption trace', detail: 'Work order and VIN links for critical lot or serial numbers.', icon: 'bi-upc-scan' },
    { title: 'Shortage signals', detail: 'Owner, need-by date, confirmed supply and next action.', icon: 'bi-bell' }
  ];

  get pendingPurchaseOrders(): number {
    return this.data.purchaseOrders.filter(order => !['RECEIVED', 'CANCELLED', 'CLOSED'].includes((order.status || '').toUpperCase())).length;
  }
  get purchaseOrderValue(): number { return this.data.purchaseOrders.reduce((sum, order) => sum + (order.total || 0), 0); }
  get zeroAvailableCount(): number { return this.data.stock.filter(item => item.onHand - item.reserved <= 0).length; }

  constructor(public data: AppDataService, private api: BackendApiService, private auth: AuthService, private fb: FormBuilder) {
    api.suppliers.list(0, 100).pipe(catchError(() => { this.partial.set(true); return of(null); }), finalize(() => this.suppliersLoading.set(false))).subscribe(res => {
      if (!res) return;
      data.suppliers = res.data.content.map((s: any) => ({ id: s.id, code: s.code, company: s.company, category: s.category ?? '', country: s.country ?? '', rating: s.rating ?? 0, status: s.status, contactEmail: s.contactEmail ?? '', phone: s.phone ?? '' }));
    });
    api.purchaseOrders.list(0, 100).pipe(catchError(() => { this.partial.set(true); return of(null); }), finalize(() => this.purchaseOrdersLoading.set(false))).subscribe(res => {
      if (!res) return;
      data.purchaseOrders = res.data.content.map((po: any) => ({ id: po.poNumber, backendId: po.id, supplier: po.supplierCode, date: po.orderDate, total: po.totalAmount, status: po.status, items: po.items?.length ?? 0 }));
    });
    api.inventory.items(0, 100).pipe(catchError(() => { this.partial.set(true); return of(null); }), finalize(() => this.inventoryLoading.set(false))).subscribe(res => {
      if (!res) return;
      data.stock = res.data.content.map((item: any) => ({ id: item.id, partCode: item.partCode, name: item.partCode, warehouse: item.warehouse, onHand: item.onHand, reserved: item.reserved, uom: '', value: 0 }));
    });
  }

  canManage(): boolean {
    return (this.auth.currentUser()?.role ?? '').split(',').map(role => role.trim().replace(/^ROLE_/, '').toUpperCase())
      .some(role => ['SUPER_ADMIN', 'FACTORY_ADMIN', 'MANAGER'].includes(role));
  }

  canDeleteSupplier(): boolean {
    return (this.auth.currentUser()?.role ?? '').split(',').map(role => role.trim().replace(/^ROLE_/, '').toUpperCase()).includes('SUPER_ADMIN');
  }

  openSupplierForm(supplier?: { id: number; code: string; company: string; category: string; country: string; rating: number; status: string; contactEmail?: string; phone?: string }): void {
    this.editingSupplierId.set(supplier?.id ?? null);
    this.supplierError.set('');
    this.supplierForm.reset({ code: supplier?.code ?? '', company: supplier?.company ?? '', category: supplier?.category ?? '', country: supplier?.country ?? '', rating: supplier?.rating ?? 0, contactEmail: supplier?.contactEmail ?? '', phone: supplier?.phone ?? '', status: supplier?.status ?? 'ACTIVE' });
    this.supplierFormOpen.set(true);
  }

  saveSupplier(): void {
    if (this.supplierForm.invalid) { this.supplierForm.markAllAsTouched(); return; }
    const value = this.supplierForm.getRawValue();
    const body = { ...value, code: value.code!.trim(), company: value.company!.trim(), category: value.category?.trim() || null, country: value.country?.trim() || null, contactEmail: value.contactEmail?.trim() || null, phone: value.phone?.trim() || null };
    const id = this.editingSupplierId(); this.supplierBusy.set(true); this.supplierError.set('');
    (id ? this.api.suppliers.update(id, body) : this.api.suppliers.create(body)).pipe(finalize(() => this.supplierBusy.set(false))).subscribe({
      next: () => { this.supplierFormOpen.set(false); this.loadSuppliers(); },
      error: error => this.supplierError.set(error?.error?.message || 'Could not save this supplier.')
    });
  }

  deleteSupplier(supplier: { id: number; company: string }): void {
    if (!this.canDeleteSupplier() || !window.confirm(`Delete supplier ${supplier.company}?`)) return;
    this.api.suppliers.delete(supplier.id).subscribe({ next: () => this.loadSuppliers(), error: error => this.supplierError.set(error?.error?.message || 'Could not delete this supplier.') });
  }

  private loadSuppliers(): void {
    this.suppliersLoading.set(true);
    this.api.suppliers.list(0, 100).pipe(finalize(() => this.suppliersLoading.set(false))).subscribe({
      next: res => { this.data.suppliers = res.data.content.map((s: any) => ({ id: s.id, code: s.code, company: s.company, category: s.category ?? '', country: s.country ?? '', rating: s.rating ?? 0, status: s.status, contactEmail: s.contactEmail ?? '', phone: s.phone ?? '' })); },
      error: error => { this.partial.set(true); this.supplierError.set(error?.error?.message || 'Could not refresh supplier records.'); }
    });
  }

  advanceOrder(order: { backendId: number }, targetStatus: string): void {
    this.actionError.set(''); this.actionBusy.set(order.backendId);
    this.api.purchaseOrders.transition(order.backendId, targetStatus).pipe(finalize(() => this.actionBusy.set(null))).subscribe({
      next: () => this.reloadOrders(), error: error => this.actionError.set(error?.error?.message || 'Could not update this purchase order.')
    });
  }

  receiveOrder(order: { backendId: number }): void {
    const warehouse = window.prompt('Enter the warehouse code for this delivery:');
    if (!warehouse?.trim()) return;
    this.actionError.set(''); this.actionBusy.set(order.backendId);
    this.api.purchaseOrders.receive(order.backendId, warehouse.trim()).pipe(finalize(() => this.actionBusy.set(null))).subscribe({
      next: () => this.reloadOrders(), error: error => this.actionError.set(error?.error?.message || 'Could not receive this purchase order.')
    });
  }

  private reloadOrders(): void {
    this.purchaseOrdersLoading.set(true);
    this.api.purchaseOrders.list(0, 100).pipe(finalize(() => this.purchaseOrdersLoading.set(false))).subscribe({
      next: res => { this.data.purchaseOrders = res.data.content.map((po: any) => ({ id: po.poNumber, backendId: po.id, supplier: po.supplierCode, date: po.orderDate, total: po.totalAmount, status: po.status, items: po.items?.length ?? 0 })); },
      error: error => this.actionError.set(error?.error?.message || 'The order changed, but the list could not refresh.')
    });
  }

  statusColor(status: string) {
    const normalized = (status || '').toUpperCase();
    if (['RECEIVED', 'APPROVED', 'ACTIVE', 'DELIVERED'].includes(normalized)) return { background: '#103b37', color: '#64ddb2', border: '1px solid #236957' };
    if (['OPEN', 'DRAFT', 'SUBMITTED', 'IN_TRANSIT'].includes(normalized)) return { background: '#102c45', color: '#85caff', border: '1px solid #28577d' };
    if (['REJECTED', 'CANCELLED', 'CLOSED'].includes(normalized)) return { background: '#47232d', color: '#ff9aa0', border: '1px solid #7d3b49' };
    return { background: '#40321c', color: '#f4c56c', border: '1px solid #70562b' };
  }
}
