import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserManagement } from '../models/admin.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = '/admin';

  constructor(private api: ApiService) {}

  // Users
  getAllUsers(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
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
}
