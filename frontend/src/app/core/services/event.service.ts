import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuditRecord, EventRequest, FactoryEvent } from '../models/event.model';

@Injectable({ providedIn: 'root' })
export class EventService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  register(request: EventRequest): Observable<FactoryEvent> {
    return this.http.post<FactoryEvent>(`${this.baseUrl}/events`, request);
  }

  findAll(): Observable<FactoryEvent[]> {
    return this.http.get<FactoryEvent[]>(`${this.baseUrl}/events`);
  }

  findAudits(): Observable<AuditRecord[]> {
    return this.http.get<AuditRecord[]>(`${this.baseUrl}/audits`);
  }
}
