import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AuditRecord } from '../../../core/models/event.model';

@Component({
  selector: 'app-audit-list',
  imports: [DatePipe],
  templateUrl: './audit-list.html',
})
export class AuditList {
  readonly audits = input.required<AuditRecord[]>();
}
