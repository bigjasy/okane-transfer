import { NotificationChannel, OtpPurpose } from './enums';
import { UserSummaryResponse } from './user.models';
export interface LoginRequest { email: string; password: string; }
export interface JwtResponse { accessToken: string; refreshToken: string; tokenType: string; expiresIn: number; }
export interface LoginResponse { twoFactorRequired: boolean; temporaryToken?: string; tokens?: JwtResponse; user?: UserSummaryResponse; }
export interface OtpVerifyRequest { temporaryToken: string; otpCode: string; purpose: OtpPurpose; }
export interface OtpRequest { purpose: OtpPurpose; channel: NotificationChannel; temporaryToken?: string; }
export interface OtpChallengeResponse { otpId: number; expiresInSeconds: number; simulatedCode?: string; }
export interface OtpVerificationResponse { verified: boolean; }
export interface RefreshTokenRequest { refreshToken: string; }
export interface LogoutRequest { refreshToken: string; }
export interface ClientRegisterRequest { firstName: string; lastName: string; email: string; phoneNumber: string; password: string; countryId: number; }
