import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CORRELATION_ID_HEADER, correlationIdInterceptor } from './correlation-id.interceptor';

describe('correlationIdInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([correlationIdInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should send a new correlation ID with each request', () => {
    http.get('/api/events').subscribe();
    http.get('/api/events').subscribe();

    const [first, second] = httpTesting.match('/api/events');
    const firstId = first.request.headers.get(CORRELATION_ID_HEADER);
    const secondId = second.request.headers.get(CORRELATION_ID_HEADER);

    expect(firstId).toMatch(/^[0-9a-f-]{36}$/);
    expect(secondId).toMatch(/^[0-9a-f-]{36}$/);
    expect(firstId).not.toBe(secondId);
    first.flush([]);
    second.flush([]);
  });
});
