import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FactoryEvent } from '../../../core/models/event.model';

@Component({
  selector: 'app-event-list',
  imports: [DatePipe],
  templateUrl: './event-list.html',
  styleUrl: './event-list.css',
})
export class EventList {
  readonly events = input.required<FactoryEvent[]>();
}
