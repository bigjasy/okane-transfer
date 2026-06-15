export const AUTH_SESSION_KEYS = {
  twoFactorToken: 'okane_2fa_token',
  twoFactorEmail: 'okane_2fa_email',
  twoFactorHint: 'okane_2fa_hint'
} as const;

export function clearTwoFactorSession(): void {
  sessionStorage.removeItem(AUTH_SESSION_KEYS.twoFactorToken);
  sessionStorage.removeItem(AUTH_SESSION_KEYS.twoFactorEmail);
  sessionStorage.removeItem(AUTH_SESSION_KEYS.twoFactorHint);
}

export function saveTwoFactorSession(token: string, email: string, hint?: string): void {
  sessionStorage.setItem(AUTH_SESSION_KEYS.twoFactorToken, token);
  sessionStorage.setItem(AUTH_SESSION_KEYS.twoFactorEmail, email);
  if (hint) {
    sessionStorage.setItem(AUTH_SESSION_KEYS.twoFactorHint, hint);
  } else {
    sessionStorage.removeItem(AUTH_SESSION_KEYS.twoFactorHint);
  }
}
