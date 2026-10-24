import { Component } from '@angular/core';
import { EventsPage } from './features/events/events-page/events-page';

@Component({
  selector: 'app-root',
  imports: [EventsPage],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
}
