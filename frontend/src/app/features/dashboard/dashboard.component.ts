import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, exhaustMap, filter, finalize, forkJoin, Observable, of, tap, timer } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { AppDataService } from '../../core/services/app-data.service';
import { BackendApiService } from '../../core/services/backend-api.service';

@Component({
  selector: 'vx-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  readonly loading = signal(false);
  readonly partial = signal(false);
  readonly lastUpdated = signal<Date | null>(null);
  readonly ready = {
    production: signal(false), inspections: signal(false), ncrs: signal(false), inventory: signal(false),
    maintenance: signal(false), maintenanceOrders: signal(false), workforce: signal(false), sales: signal(false), purchaseOrders: signal(false)
  };
  readonly apiGaps = [
    { title: 'Machine telemetry & takt', area: 'PRODUCTION', status: 'API MISSING', detail: 'No station heartbeat, cycle-time, downtime event or line-rate endpoint exists yet.', route: 'Needed: /api/v1/production/telemetry' },
    { title: 'VIN search & digital thread', area: 'TRACEABILITY', status: 'PARTIAL API', detail: 'Gate check and PDF genealogy exist; there is no VIN registry/search API for this live station view.', route: 'Available: /api/v1/quality/gate-check/{vin}' },
    { title: 'Safety event register', area: 'EHS', status: 'API MISSING', detail: 'Incident, PPE and training panels have no matching backend endpoints.', route: 'Needed: /api/v1/safety/*' },
    { title: 'Audit activity', area: 'GOVERNANCE', status: 'API MISSING', detail: 'No searchable user-action audit endpoint is exposed.', route: 'Needed: /api/v1/audit/*' },
    { title: 'Approval queue', area: 'GOVERNANCE', status: 'API MISSING', detail: 'The backend does not expose an approval request or decision endpoint.', route: 'Needed: /api/v1/approvals/*' }
  ];

  get recentOrders() { return this.data.workOrders.slice(0, 6); }
  progress(order: { qty: number; completed: number }): number {
    return order.qty > 0 ? Math.min(100, Math.max(0, order.completed / order.qty * 100)) : 0;
  }
  get statusMix() {
    const rows = [
      { label: 'In progress', key: 'IN_PROGRESS', color: '#3289f4' },
      { label: 'Released', key: 'RELEASED', color: '#39c9a1' },
      { label: 'Completed', key: 'COMPLETED', color: '#48b9d3' },
      { label: 'Paused', key: 'PAUSED', color: '#efb94d' }
    ];
    return rows.map(row => ({ ...row, count: this.data.workOrders.filter(order => order.status === row.key).length }));
  }
  get statusDonut(): string {
    const rows = this.statusMix;
    const total = rows.reduce((sum, row) => sum + row.count, 0);
    if (!total) return 'conic-gradient(#243b52 0 100%)';
    let cursor = 0;
    const stops = rows.map(row => {
      const start = cursor;
      cursor += row.count / total * 100;
      return `${row.color} ${start}% ${cursor}%`;
    });
    return `conic-gradient(${stops.join(',')})`;
  }
  get latestRecords() {
    return [
      ...this.data.purchaseOrders.slice(0, 3).map(row => ({ icon: 'bi-bag-check', title: `Purchase order ${row.id}`, detail: `${row.supplier} · ${row.status}`, route: '/supply-chain' })),
      ...this.data.workOrders.slice(0, 3).map(row => ({ icon: 'bi-gear-wide-connected', title: `Work order ${row.id}`, detail: `${row.model} · ${row.status}`, route: '/production' })),
      ...this.data.salesOrders.slice(0, 3).map(row => ({ icon: 'bi-receipt', title: `Sales order ${row.id}`, detail: `${row.model} · ${row.status}`, route: '/sales-payments' }))
    ].slice(0, 5);
  }
  get currentAlerts() {
    const alerts: { icon: string; tone: string; title: string; detail: string; route: string }[] = [];
    if (this.ready.inventory()) {
      const emptyStock = this.data.stock.filter(item => item.onHand - item.reserved <= 0).length;
      if (emptyStock) alerts.push({ icon: 'bi-exclamation-triangle-fill', tone: 'danger', title: 'Parts at zero available stock', detail: `${emptyStock} inventory items need review`, route: '/inventory' });
    }
    if (this.ready.ncrs() && this.openNcrs) alerts.push({ icon: 'bi-exclamation-diamond-fill', tone: 'warning', title: 'Open quality cases', detail: `${this.openNcrs} nonconformance records remain active`, route: '/quality' });
    if (this.ready.maintenanceOrders() && this.pendingServiceOrders) alerts.push({ icon: 'bi-wrench-adjustable', tone: 'info', title: 'Service work in queue', detail: `${this.pendingServiceOrders} maintenance orders are not closed`, route: '/maintenance' });
    return alerts.slice(0, 4);
  }

  constructor(public auth: AuthService, public data: AppDataService, private readonly api: BackendApiService) {
    timer(0, 30_000).pipe(
      filter(() => typeof document === 'undefined' || document.visibilityState === 'visible'),
      exhaustMap(() => this.load()),
      takeUntilDestroyed()
    ).subscribe();
  }

  refresh(): void { this.load().subscribe(); }

  get unitsProduced(): number { return this.data.workOrders.reduce((sum, order) => sum + order.completed, 0); }
  get partsAvailable(): number { return this.data.stock.reduce((sum, item) => sum + Math.max(0, item.onHand - item.reserved), 0); }
  get salesValue(): number { return this.data.salesOrders.reduce((sum, order) => sum + order.amount, 0); }
  get pendingPurchaseOrders(): number { return this.data.purchaseOrders.filter(order => !['RECEIVED', 'CANCELLED', 'CLOSED'].includes((order.status || '').toUpperCase())).length; }
  get pendingServiceOrders(): number { return this.data.maintenanceOrders.filter(order => !['COMPLETED', 'CANCELLED', 'CLOSED'].includes((order.status || '').toUpperCase())).length; }
  get activeWorkOrders(): number { return this.data.workOrders.filter(order => !['COMPLETED', 'CANCELLED'].includes(order.status)).length; }
  get openNcrs(): number { return this.data.ncrs.filter(issue => !['CLOSED', 'RESOLVED', 'VERIFIED'].includes(issue.status)).length; }
  get ordersAwaitingPayment(): number { return this.data.salesOrders.filter(order => !['PAID', 'REFUNDED'].includes(order.status)).length; }
  get inspectionPassRate(): number | null {
    if (!this.data.inspections.length) return null;
    return Math.round(this.data.inspections.filter(row => row.result === 'PASS').length / this.data.inspections.length * 100);
  }

  private load(): Observable<unknown> {
    if (this.loading()) return of(null);
    this.loading.set(true);
    let hadError = false;
    const keepLastGood = <T>(request: Observable<T>): Observable<T | null> => request.pipe(catchError(() => { hadError = true; return of(null); }));

    return forkJoin({
      workOrders: keepLastGood(this.api.production.workOrders(0, 100)),
      inspections: keepLastGood(this.api.quality.inspections(0, 100)),
      ncrs: keepLastGood(this.api.quality.ncrs(0, 100)),
      inventory: keepLastGood(this.api.inventory.items(0, 100)),
      assets: keepLastGood(this.api.maintenance.assets()),
      maintenanceOrders: keepLastGood(this.api.maintenance.workOrders(0, 100)),
      employees: keepLastGood(this.api.workforce.employees(0, 100)),
      salesOrders: keepLastGood(this.api.sales.orders(0, 100)),
      purchaseOrders: keepLastGood(this.api.purchaseOrders.list(0, 100))
    }).pipe(
      tap(result => {
        if (result.workOrders) {
          this.data.workOrders = result.workOrders.data.content.map((w: any) => ({ id: w.woNumber, plan: w.planNumber ?? '', model: w.modelCode, qty: w.targetQuantity, completed: w.producedQuantity ?? 0, rejected: w.rejectedQuantity ?? 0, line: w.lineCode ?? '', status: w.status }));
          this.ready.production.set(true);
        }
        if (result.inspections) {
          this.data.inspections = result.inspections.data.content.map((i: any) => ({ id: i.inspectionNumber, part: i.partCode ?? i.vin ?? '', operation: i.type, result: i.result, inspector: i.inspectorName ?? '', date: i.inspectedAt ?? i.createdAt }));
          this.ready.inspections.set(true);
        }
        if (result.ncrs) {
          this.data.ncrs = result.ncrs.data.content.map((n: any) => ({ id: n.ncrNumber, title: n.containment ?? n.ncrNumber, vin: n.vin ?? '', status: n.status, owner: '', due: n.closedAt ?? '' }));
          this.ready.ncrs.set(true);
        }
        if (result.inventory) {
          this.data.stock = result.inventory.data.content.map((i: any) => ({ id: i.id, partCode: i.partCode, name: i.partCode, warehouse: i.warehouse, onHand: i.onHand, reserved: i.reserved, uom: '', value: 0 }));
          this.ready.inventory.set(true);
        }
        if (result.assets) {
          this.data.machines = result.assets.data.map((a: any) => ({ id: a.assetCode, name: a.name, workCenter: a.lineCode ?? '', type: a.level ?? '', status: a.status, nextMaintenance: '—' }));
          this.ready.maintenance.set(true);
        }
        if (result.maintenanceOrders) {
          this.data.maintenanceOrders = result.maintenanceOrders.data.content.map((w: any) => ({ id: w.mwoNumber ?? String(w.id), machine: w.assetCode ?? '—', type: w.type ?? '', priority: w.priority ?? '—', status: w.status, due: w.scheduledStart ?? '—' }));
          this.ready.maintenanceOrders.set(true);
        }
        if (result.employees) {
          this.data.employees = result.employees.data.content.map((e: any) => ({ id: e.id, name: e.fullName, role: e.department ?? '', factory: e.factoryCode ?? '', shift: '—', skill: '—', status: e.status }));
          this.ready.workforce.set(true);
        }
        if (result.salesOrders) {
          this.data.salesOrders = result.salesOrders.data.content.map((o: any) => ({ id: o.soNumber, customer: String(o.customerId), model: o.modelCode, qty: 1, amount: o.totalAmount ?? 0, status: o.paymentStatus }));
          this.ready.sales.set(true);
        }
        if (result.purchaseOrders) {
          this.data.purchaseOrders = result.purchaseOrders.data.content.map((po: any) => ({ id: po.poNumber, backendId: po.id, supplier: po.supplierCode, date: po.orderDate, total: po.totalAmount ?? 0, status: po.status, items: po.items?.length ?? 0 }));
          this.ready.purchaseOrders.set(true);
        }
        this.partial.set(hadError);
        if (Object.values(result).some(value => value !== null)) this.lastUpdated.set(new Date());
      }),
      finalize(() => this.loading.set(false))
    );
  }
}
