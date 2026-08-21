import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, LoginRequest, LoginResponse, RegisterRequest, User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient) {
    const storedUser = localStorage.getItem(this.USER_KEY);
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem(this.TOKEN_KEY, response.token);
          const user: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            roles: response.roles,
            preferredLanguage: response.preferredLanguage,
            companyId: response.companyId,
            companyName: response.companyName,
            mustChangePassword: response.mustChangePassword,
            active: true
          };
          localStorage.setItem(this.USER_KEY, JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  register(request: RegisterRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/register`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/change-password`, request)
      .pipe(
        tap(() => {
          const user = this.currentUserValue;
          if (user) {
            const updated: User = { ...user, mustChangePassword: false };
            localStorage.setItem(this.USER_KEY, JSON.stringify(updated));
            this.currentUserSubject.next(updated);
          }
        })
      );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(role: string): boolean {
    const user = this.currentUserValue;
    return user ? user.roles.includes(role) : false;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserValue;
    return user ? roles.some(role => user.roles.includes(role)) : false;
  }

  /**
   * user.roles actually holds the full Spring Security authorities list from the JWT -
   * role names AND permission names (e.g. ORDER_APPROVE) mixed together, since a role's
   * permissions are granted as authorities alongside the role itself. This checks a
   * permission name specifically, so action gating reflects a company's own customized
   * role permissions instead of a hardcoded role name.
   */
  hasPermission(permission: string): boolean {
    const user = this.currentUserValue;
    return user ? user.roles.includes(permission) : false;
  }

  hasAnyPermission(permissions: string[]): boolean {
    const user = this.currentUserValue;
    return user ? permissions.some(p => user.roles.includes(p)) : false;
  }

  isSuperAdmin(): boolean {
    return this.hasRole('ROLE_SUPER_ADMIN');
  }

  isCompanyAdmin(): boolean {
    return this.hasRole('ROLE_COMPANY_ADMIN');
  }

  mustChangePassword(): boolean {
    return !!this.currentUserValue?.mustChangePassword;
  }
}
