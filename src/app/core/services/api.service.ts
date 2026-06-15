import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { TokenService } from './token.service';

type QueryParams = Record<string, string | number | boolean | null | undefined>;
type HeaderOptions = HttpHeaders | Record<string, string | string[]>;

interface RequestOptions {
  headers?: HeaderOptions;
  params?: HttpParams;
}

interface BlobRequestOptions extends RequestOptions {
  responseType: 'blob';
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiBaseUrl.replace(/\/$/, '');
  private readonly publicEndpoints = ['/auth/login', '/auth/register-client', '/auth/verify-otp', '/auth/refresh', '/otp/request', '/otp/verify'];

  constructor(private readonly http: HttpClient, private readonly tokens: TokenService) {}

  get<T>(endpoint: string, params?: QueryParams): Observable<T> {
    return this.http.get<T>(this.url(endpoint), this.withAuthOptions(endpoint, {
      params: this.toParams(params)
    }));
  }

  post<T>(endpoint: string, body?: unknown): Observable<T> {
    return this.http.post<T>(this.url(endpoint), body ?? {}, this.withAuthOptions(endpoint, {}));
  }

  put<T>(endpoint: string, body?: unknown): Observable<T> {
    return this.http.put<T>(this.url(endpoint), body ?? {}, this.withAuthOptions(endpoint, {}));
  }

  patch<T>(endpoint: string, body?: unknown, params?: QueryParams): Observable<T> {
    return this.http.patch<T>(this.url(endpoint), body ?? {}, this.withAuthOptions(endpoint, {
      params: this.toParams(params)
    }));
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(this.url(endpoint), this.withAuthOptions(endpoint, {}));
  }

  getBlob(endpoint: string, params?: QueryParams): Observable<Blob> {
    return this.http.get(this.url(endpoint), this.withAuthOptions<BlobRequestOptions>(endpoint, {
      params: this.toParams(params),
      responseType: 'blob' as const
    }));
  }

  private url(endpoint: string): string {
    return `${this.baseUrl}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`;
  }

  private withAuthOptions<T extends RequestOptions>(endpoint: string, options: T): T {
    if (this.publicEndpoints.some(publicEndpoint => endpoint.includes(publicEndpoint))) return options;

    const token = this.tokens.getAccessToken();
    if (!token) return options;

    const headers = options.headers instanceof HttpHeaders
      ? options.headers
      : new HttpHeaders(options.headers ?? {});
    const authHeaders = headers.has('Authorization')
      ? headers
      : headers.set('Authorization', `Bearer ${token}`);

    return {
      ...options,
      headers: authHeaders
    } as T;
  }

  private toParams(params?: QueryParams): HttpParams {
    let hp = new HttpParams();
    Object.entries(params ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') hp = hp.set(key, String(value));
    });
    return hp;
  }
}
