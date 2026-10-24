import { Component, input } from '@angular/core';

export type AlertType = 'success' | 'error';

@Component({
  selector: 'app-alert',
  imports: [],
  templateUrl: './alert.html',
  styleUrl: './alert.css',
})
export class Alert {
  readonly type = input.required<AlertType>();
  readonly message = input.required<string>();
}

