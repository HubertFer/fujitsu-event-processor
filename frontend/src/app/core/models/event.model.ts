export type EventType = 'PRODUCTION' | 'QUALITY';

export const EVENT_TYPES: EventType[] = ['PRODUCTION', 'QUALITY'];

export interface EventRequest {
  eventId: string;
  eventType: EventType;
  description: string;
  timestamp: string;
}

export interface FactoryEvent {
  eventId: string;
  eventType: EventType;
  description: string;
  timestamp: string;
  receivedAt: string;
}

export interface AuditRecord {
  id: number;
  eventId: string;
  action: string;
  createdAt: string;
}
