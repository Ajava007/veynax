import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { VehicleModelService } from '../../../core/services/vehicle-model.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { FieldErrorComponent } from '../../../shared/components/field-error/field-error.component';
import { VehicleModel } from '../../../core/models/master-data';

@Component({
  selector: 'vx-vehicle-model',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FieldErrorComponent],
  templateUrl: './vehicle-model.component.html',
  styleUrls: ['./vehicle-model.component.scss']
})
export class VehicleModelComponent implements OnInit {
  items = signal<VehicleModel[]>([]);
  loading = signal(false);
  loadError = signal('');
  saving = signal(false);
  showForm = signal(false);
  editingId = signal<number | null>(null);

  form = this.fb.group({
    modelCode: ['', [Validators.required, Validators.maxLength(30)]],
    modelName: ['', [Validators.required, Validators.maxLength(150)]],
    brand: ['', [Validators.required, Validators.maxLength(80)]],
    vehicleType: ['', Validators.maxLength(40)],
    segment: ['', Validators.maxLength(40)],
    modelYear: ['', Validators.maxLength(10)],
    fuelType: ['', Validators.maxLength(30)],
    transmission: ['', Validators.maxLength(30)],
    basePrice: [null as number | null],
    status: ['DRAFT']
  });

  constructor(private fb: FormBuilder, private service: VehicleModelService, private toast: ToastService, private confirm: ConfirmDialogService, private auth: AuthService) {}

  canManageModels(): boolean {
    return this.roles().some(role => ['SUPER_ADMIN', 'FACTORY_ADMIN', 'MANAGER'].includes(role));
  }

  canDeleteModels(): boolean { return this.roles().includes('SUPER_ADMIN'); }

  ngOnInit() { this.load(); }

  load() {
    this.loading.set(true);
    this.loadError.set('');
    this.service.list().subscribe({
      next: res => { this.items.set(res.data.content); this.loading.set(false); },
      error: () => { this.loading.set(false); this.loadError.set('Could not load vehicle models. Check the connection and try again.'); }
    });
  }

  toggleForm() { this.showForm.update(v => !v); if (!this.showForm()) { this.form.reset({ status: 'DRAFT' }); this.editingId.set(null); } }

  edit(m: VehicleModel) {
    if (!this.canManageModels()) return;
    this.editingId.set(m.id);
    this.showForm.set(true);
    this.form.patchValue({ modelCode: m.modelCode, modelName: m.modelName, brand: m.brand, vehicleType: m.vehicleType ?? '', segment: m.segment ?? '', modelYear: m.modelYear ?? '', fuelType: m.fuelType ?? '', transmission: m.transmission ?? '', basePrice: m.basePrice ?? null, status: m.status });
  }

  save() {
    if (!this.canManageModels()) { this.toast.error('You do not have permission to change vehicle models.'); return; }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.error('Check the marked fields and try again.');
      return;
    }
    this.saving.set(true);
    const body = this.form.value as Partial<VehicleModel>;
    const req$ = this.editingId() ? this.service.update(this.editingId()!, body) : this.service.create(body);
    req$.subscribe({
      next: () => { this.saving.set(false); this.toast.success('Saved.'); this.toggleForm(); this.load(); },
      error: () => this.saving.set(false)
    });
  }

  async remove(m: VehicleModel) {
    if (!this.canDeleteModels()) { this.toast.error('Only a super admin can delete vehicle models.'); return; }
    if (!(await this.confirm.confirm({ message: `Delete vehicle model "${m.modelName}"?`, danger: true, confirmLabel: 'Delete' }))) return;
    this.service.delete(m.id).subscribe({ next: () => { this.toast.success('Deleted.'); this.load(); }, error: () => {} });
  }

  private roles(): string[] {
    return (this.auth.currentUser()?.role ?? '')
      .split(',')
      .map(role => role.trim().toUpperCase().replace(/^ROLE_/, ''))
      .filter(Boolean);
  }
}
