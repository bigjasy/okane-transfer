export interface ApiResponse<T> { success?: boolean; message?: string; data?: T; timestamp?: string; }
export interface PageResponse<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number; }
export interface BackendPage<T> { content: T[]; number?: number; size?: number; totalElements?: number; totalPages?: number; }
export interface ErrorResponse { code: string; message: string; fieldErrors?: Record<string, string>; timestamp?: string; }
export type AnyObject = Record<string, unknown>;
