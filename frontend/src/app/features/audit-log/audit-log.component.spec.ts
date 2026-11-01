import { TestBed } from '@angular/core/testing';
import { AuditLogComponent } from './audit-log.component';

describe('AuditLogComponent', () => {
  it('clearly says the audit API is missing', async () => {
    await TestBed.configureTestingModule({ imports: [AuditLogComponent] }).compileComponents();
    const fixture = TestBed.createComponent(AuditLogComponent);

    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Audit records are unavailable.');
    expect(fixture.nativeElement.textContent).toContain('Search and filters will be added when real audit records are available.');
  });
});
