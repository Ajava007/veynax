import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppDataService } from '../../core/services/app-data.service';
import { BackendApiService } from '../../core/services/backend-api.service';
import { ToastService } from '../../core/services/toast.service';
import { forkJoin } from 'rxjs';

type Tab = 'stock' | 'txns' | 'mrp';

@Component({
  selector: 'vx-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent {
  tab = signal<Tab>('stock');
  mrpBusy = signal(false);
  mrpRunNumber = signal('');
  selectedPartCode = '';
  grossRequirement: number | null = null;
  scheduledReceipts = 0;
  planDate = new Date().toISOString().slice(0, 10);

  constructor(public data: AppDataService, private api: BackendApiService, private toast: ToastService) {
    this.api.inventory.items(0, 100).subscribe(res => {
      data.stock = res.data.content.map((item: any) => ({ id: item.id, partCode: item.partCode, name: item.partCode, warehouse: item.warehouse, onHand: item.onHand, reserved: item.reserved, uom: '', value: 0 }));
      const codes = [...new Set(res.data.content.map((item: any) => item.partCode))];
      if (codes.length) forkJoin(codes.map((partCode: string) => this.api.inventory.transactions(partCode))).subscribe(results => {
        data.stockTxns = results.flatMap(result => result.data.map((txn: any) => ({ id: String(txn.id), date: txn.createdAt, type: txn.txnType, partCode: txn.partCode, qty: txn.quantity, warehouse: txn.warehouse, ref: txn.referenceId ?? '' })));
      });
    });
  }

  runMrp() {
    if (!this.selectedPartCode || this.grossRequirement === null || this.grossRequirement <= 0 || this.scheduledReceipts < 0 || !this.planDate) {
      this.toast.error('Choose a part, enter a positive requirement, and check the planned receipt quantity and date.');
      return;
    }
    this.mrpBusy.set(true);
    this.api.inventory.calculateMrp({
      planDate: this.planDate,
      demand: [{ partCode: this.selectedPartCode, grossRequirement: this.grossRequirement, scheduledReceipts: this.scheduledReceipts }]
    }).subscribe({
      next: response => {
        const rows = response.data as any[];
        this.data.mrp = rows.map((row: any) => ({ partCode: row.partCode, name: row.partCode,
          required: row.grossRequirement, onHand: row.onHand, incoming: row.scheduledReceipts, shortage: row.netShortage }));
        this.mrpRunNumber.set(rows[0]?.runNumber ?? '');
        this.mrpBusy.set(false);
        this.toast.success('Material requirements calculated from available stock and scheduled receipts.');
      },
      error: () => this.mrpBusy.set(false)
    });
  }
}
