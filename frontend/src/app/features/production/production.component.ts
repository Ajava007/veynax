import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppDataService, WorkOrder } from '../../core/services/app-data.service';
import { BackendApiService } from '../../core/services/backend-api.service';
import { StatusBadgeComponent } from '../../shared/components/status-badge/status-badge.component';
import { ToastService } from '../../core/services/toast.service';

type Tab = 'plans' | 'wos' | 'shopfloor';

@Component({
  selector: 'vx-production',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  templateUrl: './production.component.html',
  styleUrls: ['./production.component.scss']
})
export class ProductionComponent {
  tab = signal<Tab>('plans');
  selectedWorkOrder = signal<WorkOrder | null>(null);
  busy = signal(false);
  producedQuantity = 0;
  rejectedQuantity = 0;

  constructor(public data: AppDataService, private api: BackendApiService, private toast: ToastService) {
    this.load();
  }

  load() {
    this.api.production.plans(0, 100).subscribe(res => {
      this.data.productionPlans = res.data.content.map((p: any) => ({ id: p.planNumber, model: p.modelCode, qty: p.quantity, line: p.lineCode ?? '', start: p.plannedStart, end: p.plannedFinish, status: p.status }));
      const lineCode = (res.data.content as any[]).find((p: any) => p.lineCode)?.lineCode;
      if (!lineCode) {
        this.data.shopFloor = { ...this.data.shopFloor, line: '', workOrder: 'No active work order', status: 'NO_ACTIVE_WORK' };
        return;
      }
      this.api.production.activeOnLine(lineCode).subscribe(active => {
        const workOrder = active.data[0] as any;
        if (workOrder) this.selectWorkOrder(this.mapWorkOrder(workOrder));
        else this.data.shopFloor = { ...this.data.shopFloor, line: lineCode, workOrder: 'No active work order', status: 'NO_ACTIVE_WORK' };
      });
    });
    this.api.production.workOrders(0, 100).subscribe(res => {
      this.data.workOrders = res.data.content.map((w: any) => this.mapWorkOrder(w));
      const active = this.data.workOrders.find(w => ['RELEASED', 'IN_PROGRESS', 'PAUSED'].includes(w.status));
      if (active && !this.selectedWorkOrder()) this.selectWorkOrder(active);
    });
  }

  private mapWorkOrder(w: any): WorkOrder {
    return { id: w.woNumber, backendId: w.id, plan: w.planNumber ?? '', model: w.modelCode,
      qty: w.targetQuantity, completed: w.producedQuantity ?? 0, rejected: w.rejectedQuantity ?? 0,
      line: w.lineCode ?? '', status: w.status };
  }

  selectWorkOrder(order: WorkOrder) {
    this.selectedWorkOrder.set(order);
    this.producedQuantity = order.completed;
    this.rejectedQuantity = order.rejected;
    this.data.shopFloor = { ...this.data.shopFloor, line: order.line, workOrder: order.id, model: order.model,
      target: order.qty, actual: order.completed, reject: order.rejected, status: order.status };
  }

  transition(target: string) {
    const order = this.selectedWorkOrder();
    if (!order?.backendId || this.busy()) return;
    if (target === 'COMPLETED' && (this.producedQuantity + this.rejectedQuantity > order.qty || this.producedQuantity + this.rejectedQuantity === 0)) {
      this.toast.error('Produced plus rejected units must be greater than zero and no more than the target.');
      return;
    }
    this.busy.set(true);
    this.api.production.transitionWorkOrder(order.backendId, target,
      target === 'COMPLETED' ? this.producedQuantity : undefined,
      target === 'COMPLETED' ? this.rejectedQuantity : undefined).subscribe({
      next: response => {
        const updated = this.mapWorkOrder(response.data);
        this.data.workOrders = this.data.workOrders.map(row => row.backendId === updated.backendId ? updated : row);
        this.selectWorkOrder(updated);
        this.toast.success(`Work order ${updated.id} moved to ${updated.status}.`);
        this.busy.set(false);
      },
      error: () => this.busy.set(false)
    });
  }

  get shopFloorStatus() { return this.selectedWorkOrder()?.status ?? 'NO_ACTIVE_WORK'; }
  get sf() { return this.data.shopFloor; }
  progress(order: WorkOrder) { return order.qty > 0 ? Math.min(100, Math.max(0, order.completed / order.qty * 100)) : 0; }
}
