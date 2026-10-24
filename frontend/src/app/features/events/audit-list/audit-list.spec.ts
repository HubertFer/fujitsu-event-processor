import { TestBed } from '@angular/core/testing';
import { AuditList } from './audit-list';

describe('AuditList', () => {
  it('should render one row per audit record', () => {
    const fixture = TestBed.createComponent(AuditList);
    fixture.componentRef.setInput('audits', [
      { id: 1, eventId: 'EVT-1', action: 'QUALITY_EVENT_REGISTERED', createdAt: '2026-05-20T13:00:00Z' },
      { id: 2, eventId: 'EVT-2', action: 'QUALITY_EVENT_REGISTERED', createdAt: '2026-05-20T13:05:00Z' },
    ]);
    fixture.detectChanges();

    const rows = (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('EVT-1');
  });

  it('should show a message when there are no audit records', () => {
    const fixture = TestBed.createComponent(AuditList);
    fixture.componentRef.setInput('audits', []);
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).querySelector('.empty')).toBeTruthy();
  });
});
