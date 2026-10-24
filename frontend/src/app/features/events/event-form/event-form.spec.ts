import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EventForm } from './event-form';
import { EventRequest } from '../../../core/models/event.model';

describe('EventForm', () => {
  let fixture: ComponentFixture<EventForm>;
  let element: HTMLElement;
  let emitted: EventRequest[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventForm],
    }).compileComponents();

    fixture = TestBed.createComponent(EventForm);
    element = fixture.nativeElement;
    emitted = [];
    fixture.componentInstance.submitted.subscribe((request) => emitted.push(request));
    fixture.detectChanges();
  });

  function type(selector: string, value: string): void {
    const field = element.querySelector(selector) as HTMLInputElement;
    field.value = value;
    field.dispatchEvent(new Event('input'));
  }

  function submit(): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
  }

  it('should not emit and should show errors when the form is empty', () => {
    submit();

    expect(emitted.length).toBe(0);
    expect(element.querySelectorAll('small').length).toBe(2);
  });

  it('should emit the trimmed request when the form is valid', () => {
    type('input[formControlName="eventId"]', '  EVT-1001  ');
    type('textarea[formControlName="description"]', '  Production line started  ');
    type('input[formControlName="timestamp"]', '2026-05-20T10:00:30');

    submit();

    expect(emitted).toEqual([
      {
        eventId: 'EVT-1001',
        eventType: 'PRODUCTION',
        description: 'Production line started',
        timestamp: '2026-05-20T10:00:30',
      },
    ]);
  });
});
