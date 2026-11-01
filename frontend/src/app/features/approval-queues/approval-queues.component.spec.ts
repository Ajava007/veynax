import { TestBed } from '@angular/core/testing';
import { ApprovalQueuesComponent } from './approval-queues.component';

describe('ApprovalQueuesComponent', () => {
  it('clearly says the approval API is missing', async () => {
    await TestBed.configureTestingModule({ imports: [ApprovalQueuesComponent] }).compileComponents();
    const fixture = TestBed.createComponent(ApprovalQueuesComponent);

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Approval requests are unavailable.');
    expect(fixture.nativeElement.textContent).toContain('No requests or decisions are being shown or saved here.');
  });
});
