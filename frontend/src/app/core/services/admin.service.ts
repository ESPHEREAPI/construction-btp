import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserManagement, Role, RoleRequest, Permission } from '../models/admin.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = '/admin';

  constructor(private api: ApiService) {}

  // Users
  getAllUsers(page: number = 0, size: number = 10, companyId?: number): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (companyId != null) {
      params = params.set('companyId', companyId.toString());
    }
    return this.api.get<any>(`${this.apiUrl}/users`,  params );
  }

  getUserById(id: number): Observable<UserManagement> {
    return this.api.get<UserManagement>(`${this.apiUrl}/users/${id}`);
  }

  createUser(user: any): Observable<UserManagement> {
    return this.api.post<UserManagement>(`${this.apiUrl}/users`, user);
  }

  updateUser(id: number, user: any): Observable<UserManagement> {
    return this.api.put<UserManagement>(`${this.apiUrl}/users/${id}`, user);
  }

  deleteUser(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/users/${id}`);
  }

  toggleUserStatus(id: number): Observable<UserManagement> {
    return this.api.put<UserManagement>(`${this.apiUrl}/users/${id}/toggle-status`, {});
  }

  resetPassword(id: number): Observable<{ tempPassword: string }> {
    return this.api.put<{ tempPassword: string }>(`${this.apiUrl}/users/${id}/reset-password`, {});
  }

  // Roles
  getAssignableRoles(): Observable<Role[]> {
    return this.api.get<Role[]>(`${this.apiUrl}/roles`);
  }

  getAllPermissions(): Observable<Permission[]> {
    return this.api.get<Permission[]>(`${this.apiUrl}/roles/permissions`);
  }

  getRoleById(id: number): Observable<Role> {
    return this.api.get<Role>(`${this.apiUrl}/roles/${id}`);
  }

  createRole(role: RoleRequest): Observable<Role> {
    return this.api.post<Role>(`${this.apiUrl}/roles`, role);
  }

  updateRole(id: number, role: RoleRequest): Observable<Role> {
    return this.api.put<Role>(`${this.apiUrl}/roles/${id}`, role);
  }

  deleteRole(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/roles/${id}`);
  }
}
