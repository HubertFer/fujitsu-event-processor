import { Component, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { EVENT_TYPES, EventRequest, EventType } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-form',
  imports: [ReactiveFormsModule],
  templateUrl: './event-form.html',
  styleUrl: './event-form.css',
})
export class EventForm {
  private readonly formBuilder = inject(NonNullableFormBuilder);

  readonly submitting = input(false);
  readonly submitted = output<EventRequest>();

  protected readonly eventTypes = EVENT_TYPES;

  protected readonly form = this.formBuilder.group({
    eventId: ['', [Validators.required, Validators.maxLength(100)]],
    eventType: ['PRODUCTION' as EventType, Validators.required],
    description: ['', [Validators.required, Validators.maxLength(500)]],
    timestamp: [currentDateTime(), Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    this.submitted.emit({
      ...value,
      eventId: value.eventId.trim(),
      description: value.description.trim(),
    });
  }

  reset(): void {
    this.form.reset({ eventType: 'PRODUCTION', timestamp: currentDateTime() });
  }

  protected hasError(field: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }
}

function currentDateTime(): string {
  const now = new Date();
  return new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 19);
}
