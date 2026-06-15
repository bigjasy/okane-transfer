function resolveApiBaseUrl(): string {
  if (typeof window !== 'undefined' && window.location?.origin) {
    return `${window.location.origin}/okane_transfer_war/api/v1`;
  }
  return '/okane_transfer_war/api/v1';
}

export const environment = {
  production: true,
  apiBaseUrl: resolveApiBaseUrl(),
  useMockApi: false,
  allowMockFallback: false
};
