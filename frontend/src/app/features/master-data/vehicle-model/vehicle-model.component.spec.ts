import { FormBuilder } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';
import { ToastService } from '../../../core/services/toast.service';
import { VehicleModelService } from '../../../core/services/vehicle-model.service';
import { VehicleModelComponent } from './vehicle-model.component';

describe('VehicleModelComponent access', () => {
  function createComponent(role: string) {
    const auth = { currentUser: () => ({ role }) } as unknown as AuthService;
    return new VehicleModelComponent(
      new FormBuilder(),
      {} as VehicleModelService,
      {} as ToastService,
      {} as ConfirmDialogService,
      auth
    );
  }

  it('lets a factory admin create and edit models but not delete them', () => {
    const component = createComponent('ROLE_FACTORY_ADMIN');

    expect(component.canManageModels()).toBeTrue();
    expect(component.canDeleteModels()).toBeFalse();
  });

  it('lets a super admin delete models', () => {
    const component = createComponent('ROLE_SUPER_ADMIN');

    expect(component.canManageModels()).toBeTrue();
    expect(component.canDeleteModels()).toBeTrue();
  });

  it('keeps model actions hidden for view-only users', () => {
    const component = createComponent('ROLE_VIEWER');

    expect(component.canManageModels()).toBeFalse();
    expect(component.canDeleteModels()).toBeFalse();
  });
});
