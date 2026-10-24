import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './error-message';

describe('errorMessage', () => {
  it('should report the API as unavailable when there is no response', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(errorMessage(error)).toContain('API is unavailable');
  });

  it('should use the problem detail returned by the API', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: { title: 'Duplicate event', detail: 'Event EVT-1 has already been processed.' },
    });

    expect(errorMessage(error)).toBe('Event EVT-1 has already been processed.');
  });

  it('should list field errors returned by the API', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { detail: 'One or more fields are invalid', errors: { description: 'must not be blank' } },
    });

    expect(errorMessage(error)).toBe('One or more fields are invalid (description: must not be blank)');
  });
});
