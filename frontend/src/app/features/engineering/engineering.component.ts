import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { AppDataService } from '../../core/services/app-data.service';
import { BackendApiService } from '../../core/services/backend-api.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmDialogService } from '../../shared/services/confirm-dialog.service';
import { FieldErrorComponent } from '../../shared/components/field-error/field-error.component';
import { VehicleModel } from '../../core/models/master-data';
import { BomRowRecord, BomRowRequest, PartRecord, PartRequest, RoutingOperationRecord, RoutingOperationRequest } from '../../core/models/engineering';

type Tab = 'parts' | 'bom' | 'routing';
type BomViewRow = BomRowRecord & { displayLevel: number };

@Component({
  selector: 'vx-engineering',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, FieldErrorComponent],
  templateUrl: './engineering.component.html',
  styleUrls: ['./engineering.component.scss']
})
export class EngineeringComponent {
  tab = signal<Tab>('parts');
  models = signal<VehicleModel[]>([]);
  partRecords = signal<PartRecord[]>([]);
  bomRecords = signal<BomViewRow[]>([]);
  routingRecords = signal<RoutingOperationRecord[]>([]);
  selectedModelCode = signal('');
  modelsLoading = signal(true);
  partsLoading = signal(true);
  bomLoading = signal(false);
  routingLoading = signal(false);
  modelsError = signal('');
  partsError = signal('');
  bomError = signal('');
  routingError = signal('');
  partFormOpen = signal(false);
  partSaving = signal(false);
  editingPartId = signal<number | null>(null);
  bomFormOpen = signal(false);
  bomSaving = signal(false);
  editingBomId = signal<number | null>(null);
  routingFormOpen = signal(false);
  routingSaving = signal(false);
  editingRoutingId = signal<number | null>(null);
  private selectionVersion = 0;
  partForm = this.fb.group({
    partCode: ['', [Validators.required, Validators.maxLength(40)]],
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(500)],
    uom: ['', [Validators.required, Validators.maxLength(10)]],
    category: ['PURCHASED', Validators.maxLength(30)],
    standardCost: [null as number | null, Validators.min(0)],
    revision: ['', Validators.maxLength(10)],
    status: ['DRAFT']
  });
  bomForm = this.fb.group({
    parentPartCode: ['', [Validators.required, Validators.maxLength(40)]],
    componentPartCode: ['', [Validators.required, Validators.maxLength(40)]],
    quantity: [1, [Validators.required, Validators.min(0.0001)]],
    scrapFactor: [0, [Validators.min(0), Validators.max(1)]],
    optional: [false],
    level: [1, [Validators.required, Validators.min(1)]],
    effectiveFrom: [''],
    effectiveTo: [''],
    remark: ['', Validators.maxLength(500)]
  });
  routingForm = this.fb.group({
    operationNumber: [10, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    operationName: ['', [Validators.required, Validators.maxLength(150)]],
    workCenter: ['', [Validators.required, Validators.maxLength(60)]],
    setupTimeSec: [0, [Validators.min(0), Validators.pattern(/^\d+$/)]],
    cycleTimeSec: [1, [Validators.required, Validators.min(1), Validators.pattern(/^\d+$/)]],
    instructions: ['', Validators.maxLength(1000)]
  });
  tabs: { id: Tab; label: string }[] = [
    { id: 'parts', label: 'Part Master' },
    { id: 'bom', label: 'BOM (multi-level)' },
    { id: 'routing', label: 'Manufacturing Routing' }
  ];
  constructor(
    public data: AppDataService,
    private api: BackendApiService,
    private fb: FormBuilder,
    private auth: AuthService,
    private toast: ToastService,
    private confirm: ConfirmDialogService
  ) {
    this.loadParts();
    this.loadModels();
  }

  selectedModel(): VehicleModel | undefined {
    return this.models().find(model => model.modelCode === this.selectedModelCode());
  }

  partName(partCode: string): string {
    return this.partRecords().find(part => part.partCode === partCode)?.name ?? partCode;
  }

  secondsToMinutes(seconds: number): string { return (seconds / 60).toFixed(2); }

  canManageEngineering(): boolean {
    return this.roles().some(role => ['SUPER_ADMIN', 'FACTORY_ADMIN', 'MANAGER'].includes(role));
  }

  canDeleteEngineering(): boolean { return this.roles().includes('SUPER_ADMIN'); }

  openNewPart() {
    if (!this.canManageEngineering()) return;
    this.editingPartId.set(null);
    this.partForm.reset({ partCode: '', name: '', description: '', uom: '', category: 'PURCHASED', standardCost: null, revision: '', status: 'DRAFT' });
    this.partFormOpen.set(true);
  }

  editPart(part: PartRecord) {
    if (!this.canManageEngineering()) return;
    this.editingPartId.set(part.id);
    this.partForm.reset({
      partCode: part.partCode, name: part.name, description: part.description ?? '', uom: part.uom,
      category: part.category ?? 'PURCHASED', standardCost: part.standardCost ?? null,
      revision: part.revision ?? '', status: part.status
    });
    this.partFormOpen.set(true);
  }

  cancelPartEdit() { this.partFormOpen.set(false); this.editingPartId.set(null); }

  savePart() {
    if (!this.canManageEngineering()) { this.toast.error('You do not have permission to change parts.'); return; }
    if (this.partForm.invalid) { this.partForm.markAllAsTouched(); this.toast.error('Check the marked fields and try again.'); return; }
    const values = this.partForm.getRawValue();
    const body: PartRequest = {
      partCode: values.partCode?.trim() ?? '', name: values.name?.trim() ?? '', description: values.description?.trim() || null,
      uom: values.uom?.trim() ?? '', category: values.category?.trim() || null,
      standardCost: values.standardCost ?? null, revision: values.revision?.trim() || null, status: values.status ?? 'DRAFT'
    };
    this.partSaving.set(true);
    const request = this.editingPartId() === null
      ? this.api.parts.create(body)
      : this.api.parts.update(this.editingPartId()!, body);
    request.subscribe({
      next: () => {
        this.partSaving.set(false);
        this.toast.success(this.editingPartId() === null ? 'Part added.' : 'Part updated.');
        this.cancelPartEdit();
        this.loadParts();
      },
      error: () => this.partSaving.set(false)
    });
  }

  async deletePart(part: PartRecord) {
    if (!this.canDeleteEngineering()) { this.toast.error('Only a super admin can delete parts.'); return; }
    if (!(await this.confirm.confirm({ message: `Delete part "${part.partCode}"?`, danger: true, confirmLabel: 'Delete' }))) return;
    this.api.parts.delete(part.id).subscribe({
      next: () => { this.toast.success('Part deleted.'); this.loadParts(); },
      error: () => undefined
    });
  }

  private roles(): string[] {
    return (this.auth.currentUser()?.role ?? '').split(',')
      .map(role => role.trim().toUpperCase().replace(/^ROLE_/, '')).filter(Boolean);
  }

  openNewBomRow() {
    if (!this.canManageEngineering() || !this.selectedModelCode()) return;
    this.editingBomId.set(null);
    this.bomForm.reset({
      parentPartCode: this.selectedModelCode(), componentPartCode: '', quantity: 1,
      scrapFactor: 0, optional: false, level: 1, effectiveFrom: '', effectiveTo: '', remark: ''
    });
    this.bomFormOpen.set(true);
  }

  editBomRow(row: BomViewRow) {
    if (!this.canManageEngineering()) return;
    this.editingBomId.set(row.id);
    this.bomForm.reset({
      parentPartCode: row.parentPartCode, componentPartCode: row.componentPartCode,
      quantity: row.quantity, scrapFactor: row.scrapFactor, optional: row.optional,
      level: row.level, effectiveFrom: row.effectiveFrom ?? '', effectiveTo: row.effectiveTo ?? '', remark: row.remark ?? ''
    });
    this.bomFormOpen.set(true);
  }

  cancelBomEdit() { this.bomFormOpen.set(false); this.editingBomId.set(null); }

  updateBomParentLevel(parentCode: string) {
    const parentRow = this.bomRecords().find(row => row.componentPartCode === parentCode);
    this.bomForm.patchValue({ level: parentRow ? parentRow.displayLevel + 1 : 1 });
  }

  bomEffectivityInvalid(): boolean {
    const from = this.bomForm.controls.effectiveFrom.value;
    const to = this.bomForm.controls.effectiveTo.value;
    return Boolean(from && to && to < from);
  }

  saveBomRow() {
    if (!this.canManageEngineering()) { this.toast.error('You do not have permission to change the bill of materials.'); return; }
    if (this.bomForm.invalid || this.bomEffectivityInvalid()) {
      this.bomForm.markAllAsTouched();
      this.toast.error(this.bomEffectivityInvalid() ? 'The end date must be the same as or later than the start date.' : 'Check the marked fields and try again.');
      return;
    }
    const values = this.bomForm.getRawValue();
    const body: BomRowRequest = {
      parentPartCode: values.parentPartCode?.trim() ?? '', componentPartCode: values.componentPartCode?.trim() ?? '',
      quantity: values.quantity ?? 1, scrapFactor: values.scrapFactor ?? 0, optional: values.optional ?? false,
      level: values.level ?? 1, effectiveFrom: values.effectiveFrom || null,
      effectiveTo: values.effectiveTo || null, remark: values.remark?.trim() || null
    };
    this.bomSaving.set(true);
    const request = this.editingBomId() === null
      ? this.api.bom.create(body)
      : this.api.bom.update(this.editingBomId()!, body);
    request.subscribe({
      next: () => {
        this.bomSaving.set(false);
        this.toast.success(this.editingBomId() === null ? 'BOM row added.' : 'BOM row updated.');
        this.cancelBomEdit();
        this.selectModel(this.selectedModelCode());
      },
      error: () => this.bomSaving.set(false)
    });
  }

  async deleteBomRow(row: BomViewRow) {
    if (!this.canDeleteEngineering()) { this.toast.error('Only a super admin can delete a BOM row.'); return; }
    if (!(await this.confirm.confirm({ message: `Remove ${row.componentPartCode} from ${row.parentPartCode}?`, danger: true, confirmLabel: 'Remove' }))) return;
    this.api.bom.delete(row.id).subscribe({
      next: () => { this.toast.success('BOM row removed.'); this.selectModel(this.selectedModelCode()); },
      error: () => undefined
    });
  }

  openNewRoutingOperation() {
    if (!this.canManageEngineering() || !this.selectedModelCode()) return;
    this.editingRoutingId.set(null);
    const nextNumber = this.routingRecords().reduce((max, operation) => Math.max(max, operation.operationNumber), 0) + 10;
    this.routingForm.reset({ operationNumber: nextNumber, operationName: '', workCenter: '', setupTimeSec: 0, cycleTimeSec: 1, instructions: '' });
    this.routingFormOpen.set(true);
  }

  editRoutingOperation(operation: RoutingOperationRecord) {
    if (!this.canManageEngineering()) return;
    this.editingRoutingId.set(operation.id);
    this.routingForm.reset({
      operationNumber: operation.operationNumber, operationName: operation.operationName,
      workCenter: operation.workCenter, setupTimeSec: operation.setupTimeSec,
      cycleTimeSec: operation.cycleTimeSec, instructions: operation.instructions ?? ''
    });
    this.routingFormOpen.set(true);
  }

  cancelRoutingEdit() { this.routingFormOpen.set(false); this.editingRoutingId.set(null); }

  saveRoutingOperation() {
    if (!this.canManageEngineering()) { this.toast.error('You do not have permission to change manufacturing steps.'); return; }
    if (this.routingForm.invalid) { this.routingForm.markAllAsTouched(); this.toast.error('Check the marked fields and try again.'); return; }
    const values = this.routingForm.getRawValue();
    const body: RoutingOperationRequest = {
      modelCode: this.selectedModelCode(), operationNumber: values.operationNumber ?? 1,
      operationName: values.operationName?.trim() ?? '', workCenter: values.workCenter?.trim() ?? '',
      setupTimeSec: values.setupTimeSec ?? 0, cycleTimeSec: values.cycleTimeSec ?? 1,
      instructions: values.instructions?.trim() || null
    };
    this.routingSaving.set(true);
    const request = this.editingRoutingId() === null
      ? this.api.routing.create(body)
      : this.api.routing.update(this.editingRoutingId()!, body);
    request.subscribe({
      next: () => {
        this.routingSaving.set(false);
        this.toast.success(this.editingRoutingId() === null ? 'Work step added.' : 'Work step updated.');
        this.cancelRoutingEdit();
        this.selectModel(this.selectedModelCode());
      },
      error: () => this.routingSaving.set(false)
    });
  }

  async deleteRoutingOperation(operation: RoutingOperationRecord) {
    if (!this.canDeleteEngineering()) { this.toast.error('Only a super admin can delete a work step.'); return; }
    if (!(await this.confirm.confirm({ message: `Delete operation ${operation.operationNumber}: ${operation.operationName}?`, danger: true, confirmLabel: 'Delete' }))) return;
    this.api.routing.delete(operation.id).subscribe({
      next: () => { this.toast.success('Work step deleted.'); this.selectModel(this.selectedModelCode()); },
      error: () => undefined
    });
  }

  loadParts() {
    this.partsLoading.set(true);
    this.partsError.set('');
    this.api.parts.list(0, 100).subscribe({
      next: res => {
        this.partRecords.set(res.data.content);
        this.data.parts = res.data.content.map(part => ({
          id: part.id, code: part.partCode, name: part.name, category: part.category ?? '',
          uom: part.uom, cost: part.standardCost ?? 0, status: part.status
        }));
        this.partsLoading.set(false);
      },
      error: () => {
        this.partsLoading.set(false);
        this.partsError.set('Could not load parts. Check the connection and try again.');
      }
    });
  }

  loadModels() {
    this.modelsLoading.set(true);
    this.modelsError.set('');
    this.api.vehicleModels.list(0, 100).subscribe({
      next: res => {
        const models = res.data.content;
        this.models.set(models);
        this.modelsLoading.set(false);
        const selected = models.some(model => model.modelCode === this.selectedModelCode())
          ? this.selectedModelCode()
          : models[0]?.modelCode ?? '';
        this.selectModel(selected);
      },
      error: () => {
        this.modelsLoading.set(false);
        this.modelsError.set('Could not load vehicle models. Check the connection and try again.');
      }
    });
  }

  selectModel(modelCode: string) {
    this.selectedModelCode.set(modelCode);
    this.bomRecords.set([]);
    this.routingRecords.set([]);
    const requestVersion = ++this.selectionVersion;
    this.bomError.set('');
    this.routingError.set('');
    if (!modelCode) {
      this.bomLoading.set(false);
      this.routingLoading.set(false);
      return;
    }

    this.loadBomRows(modelCode, requestVersion);
    this.loadRoutingRows(modelCode, requestVersion);
  }

  private loadBomRows(modelCode: string, requestVersion: number) {
    this.bomLoading.set(true);
    const rows: BomViewRow[] = [];
    const visit = async (parentCode: string, depth: number, branch: Set<string>): Promise<void> => {
      const key = parentCode.toUpperCase();
      if (depth >= 12 || branch.has(key)) return;
      const nextBranch = new Set(branch).add(key);
      const response = await firstValueFrom(this.api.bom.byParent(parentCode));
      for (const row of response.data) {
        rows.push({ ...row, displayLevel: depth + 1 });
        await visit(row.componentPartCode, depth + 1, nextBranch);
      }
    };

    visit(modelCode, 0, new Set()).then(() => {
      if (requestVersion !== this.selectionVersion) return;
      this.bomRecords.set(rows);
      this.bomLoading.set(false);
    }).catch(() => {
      if (requestVersion !== this.selectionVersion) return;
      this.bomLoading.set(false);
      this.bomError.set('Could not load the bill of materials. Try again.');
    });
  }

  private loadRoutingRows(modelCode: string, requestVersion: number) {
    this.routingLoading.set(true);
    this.api.routing.byModel(modelCode).subscribe({
      next: result => {
        if (requestVersion !== this.selectionVersion) return;
        this.routingRecords.set(result.data);
        this.routingLoading.set(false);
      },
      error: () => {
        if (requestVersion !== this.selectionVersion) return;
        this.routingLoading.set(false);
        this.routingError.set('Could not load manufacturing steps. Try again.');
      }
    });
  }
}
