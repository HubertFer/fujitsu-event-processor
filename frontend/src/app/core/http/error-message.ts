import { HttpErrorResponse } from '@angular/common/http';

export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Unexpected error.';
  }
  if (error.status === 0) {
    return 'API is unavailable. Check that the backend is running.';
  }
  const problem = error.error;
  const fieldErrors = problem?.errors as Record<string, string> | undefined;
  if (fieldErrors) {
    const details = Object.entries(fieldErrors).map(([field, message]) => `${field}: ${message}`);
    return `${problem.detail} (${details.join('; ')})`;
  }
  return problem?.detail ?? `Unexpected error (HTTP ${error.status}).`;
}
