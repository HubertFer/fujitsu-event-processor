import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { EventService } from '../../../core/services/event.service';
import { AuditRecord, EventRequest, FactoryEvent } from '../../../core/models/event.model';
import { errorMessage } from '../../../core/http/error-message';
import { Alert, AlertType } from '../../../shared/components/alert/alert';
import { AuditList } from '../audit-list/audit-list';
import { EventForm } from '../event-form/event-form';
import { EventList } from '../event-list/event-list';

@Component({
  selector: 'app-events-page',
  imports: [Alert, AuditList, EventForm, EventList],
  templateUrl: './events-page.html',
  styleUrl: './events-page.css',
})
export class EventsPage implements OnInit {
  private readonly eventService = inject(EventService);
  private readonly eventForm = viewChild.required(EventForm);

  protected readonly events = signal<FactoryEvent[]>([]);
  protected readonly audits = signal<AuditRecord[]>([]);
  protected readonly submitting = signal(false);
  protected readonly alert = signal<{ type: AlertType; message: string } | null>(null);

  ngOnInit(): void {
    this.refresh();
  }

  protected refresh(): void {
    this.eventService.findAll().subscribe({
      next: (events) => this.events.set(events),
      error: (error) => this.alert.set({ type: 'error', message: errorMessage(error) }),
    });
    this.eventService.findAudits().subscribe({
      next: (audits) => this.audits.set(audits),
      error: (error) => this.alert.set({ type: 'error', message: errorMessage(error) }),
    });
  }

  protected register(request: EventRequest): void {
    this.submitting.set(true);
    this.alert.set(null);
    this.eventService.register(request).subscribe({
      next: (event) => {
        this.submitting.set(false);
        this.alert.set({ type: 'success', message: `Event ${event.eventId} registered successfully.` });
        this.eventForm().reset();
        this.refresh();
      },
      error: (error) => {
        this.submitting.set(false);
        this.alert.set({ type: 'error', message: errorMessage(error) });
      },
    });
  }
}
