import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiBaseUrl.replace(/\/$/, '');
  constructor(private readonly http: HttpClient) {}

  get<T>(endpoint: string, params?: Record<string, string | number | boolean | null | undefined>): Observable<T> {
    return this.http.get<T>(this.url(endpoint), { params: this.toParams(params) });
  }
  post<T>(endpoint: string, body?: unknown): Observable<T> { return this.http.post<T>(this.url(endpoint), body ?? {}); }
  put<T>(endpoint: string, body?: unknown): Observable<T> { return this.http.put<T>(this.url(endpoint), body ?? {}); }
  patch<T>(endpoint: string, body?: unknown): Observable<T> { return this.http.patch<T>(this.url(endpoint), body ?? {}); }
  delete<T>(endpoint: string): Observable<T> { return this.http.delete<T>(this.url(endpoint)); }

  private url(endpoint: string): string { return `${this.baseUrl}${endpoint.startsWith('/') ? endpoint : '/' + endpoint}`; }
  private toParams(params?: Record<string, string | number | boolean | null | undefined>): HttpParams {
    let hp = new HttpParams();
    Object.entries(params ?? {}).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') hp = hp.set(key, String(value));
    });
    return hp;
  }
}
