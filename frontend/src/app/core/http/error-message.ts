import { HttpErrorResponse } from '@angular/common/http';
import { CORRELATION_ID_HEADER } from './correlation-id.interceptor';

export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Unexpected error.';
  }
  if (error.status === 0) {
    return 'API is unavailable. Check that the backend is running.';
  }
  const correlationId = error.headers.get(CORRELATION_ID_HEADER);
  const reference = correlationId ? ` [ref: ${correlationId}]` : '';
  return describeProblem(error) + reference;
}

function describeProblem(error: HttpErrorResponse): string {
  const problem = error.error;
  const fieldErrors = problem?.errors as Record<string, string> | undefined;
  if (fieldErrors) {
    const details = Object.entries(fieldErrors).map(([field, message]) => `${field}: ${message}`);
    return `${problem.detail} (${details.join('; ')})`;
  }
  return problem?.detail ?? `Unexpected error (HTTP ${error.status}).`;
}
