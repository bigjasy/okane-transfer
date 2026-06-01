import { Language, Role, UserStatus } from './enums';
export interface UserSummaryResponse { id: number; email: string; fullName: string; role: Role; status: UserStatus; agencyName?: string; }
export interface UserProfileResponse { id: number; firstName: string; lastName: string; email: string; phoneNumber: string; role: Role; preferredLanguage: Language; }
export interface UserCreateRequest { firstName: string; lastName: string; email: string; phoneNumber: string; role: Role; agencyId?: number | null; }
export interface UserUpdateRequest { firstName: string; lastName: string; phoneNumber: string; preferredLanguage: Language; }
export interface UserStatusUpdateRequest { status: UserStatus; reason?: string; }
export interface UserRoleUpdateRequest { role: Role; }
