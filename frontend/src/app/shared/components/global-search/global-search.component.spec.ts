import { Router } from '@angular/router';
import { AppDataService } from '../../../core/services/app-data.service';
import { GlobalSearchComponent } from './global-search.component';

describe('GlobalSearchComponent', () => {
  function createComponent() {
    const data = {
      vins: [], parts: [], workOrders: [], suppliers: [], employees: [],
      purchaseOrders: [{ id: 'PO-24', supplier: 'North Parts', date: '', total: 0, status: 'OPEN', items: 1 }]
    } as unknown as AppDataService;
    const router = { navigateByUrl: jasmine.createSpy('navigateByUrl') } as unknown as Router;
    return { component: new GlobalSearchComponent(data, router), router };
  }

  it('finds purchase orders by number', () => {
    const { component } = createComponent();
    component.query.set('po-24');

    component.onChange();

    expect(component.results()).toEqual([jasmine.objectContaining({
      kind: 'Purchase Order', id: 'PO-24', route: '/supply-chain'
    })]);
  });

  it('opens the purchase order list when a result is selected', () => {
    const { component, router } = createComponent();
    component.go({ kind: 'Purchase Order', id: 'PO-24', label: 'PO-24 · North Parts', route: '/supply-chain' });

    expect(router.navigateByUrl).toHaveBeenCalledWith('/supply-chain');
    expect(component.query()).toBe('');
    expect(component.results()).toEqual([]);
  });
});
