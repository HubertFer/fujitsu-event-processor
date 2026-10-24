import { Component, OnInit, inject, signal } from '@angular/core';
import { EventService } from '../../../core/services/event.service';
import { FactoryEvent } from '../../../core/models/event.model';
import { EventList } from '../event-list/event-list';

@Component({
  selector: 'app-events-page',
  imports: [EventList],
  templateUrl: './events-page.html',
  styleUrl: './events-page.css',
})
export class EventsPage implements OnInit {
  private readonly eventService = inject(EventService);

  protected readonly events = signal<FactoryEvent[]>([]);

  ngOnInit(): void {
    this.loadEvents();
  }

  protected loadEvents(): void {
    this.eventService.findAll().subscribe((events) => this.events.set(events));
  }
}
