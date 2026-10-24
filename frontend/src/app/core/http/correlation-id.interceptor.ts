import { HttpInterceptorFn } from '@angular/common/http';

export const CORRELATION_ID_HEADER = 'X-Correlation-Id';

export const correlationIdInterceptor: HttpInterceptorFn = (request, next) => {
  const correlationId = globalThis.crypto?.randomUUID?.();
  if (!correlationId) {
    return next(request);
  }
  return next(request.clone({ setHeaders: { [CORRELATION_ID_HEADER]: correlationId } }));
};
