import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EventService } from './event.service';
import { EventRequest, FactoryEvent } from '../models/event.model';

describe('EventService', () => {
  let service: EventService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EventService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should post the event to the API', () => {
    const request: EventRequest = {
      eventId: 'EVT-1001',
      eventType: 'PRODUCTION',
      description: 'Production line started',
      timestamp: '2026-05-20T10:00:00',
    };
    let response: FactoryEvent | undefined;

    service.register(request).subscribe((event) => (response = event));

    const req = httpTesting.expectOne('/api/events');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ ...request, receivedAt: '2026-05-20T13:00:00Z' });
    expect(response?.eventId).toBe('EVT-1001');
  });

  it('should fetch registered events', () => {
    let events: FactoryEvent[] = [];

    service.findAll().subscribe((result) => (events = result));

    const req = httpTesting.expectOne('/api/events');
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        eventId: 'EVT-1',
        eventType: 'QUALITY',
        description: 'Inspection failed',
        timestamp: '2026-05-20T10:00:00',
        receivedAt: '2026-05-20T13:00:00Z',
      },
    ]);
    expect(events.length).toBe(1);
  });
});
