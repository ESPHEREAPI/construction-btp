import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { License, LicenseMine } from '../models/license.model';
import { ApiService } from './api.service';

/** Self-service: any authenticated company user can view; activation is COMPANY_ADMIN only (enforced server-side). */
@Injectable({
  providedIn: 'root'
})
export class MyLicenseService {
  private apiUrl = '/licenses/me';

  constructor(private api: ApiService) {}

  getMine(): Observable<LicenseMine> {
    return this.api.get<LicenseMine>(this.apiUrl);
  }

  getHistory(): Observable<License[]> {
    return this.api.get<License[]>(`${this.apiUrl}/history`);
  }

  activate(key: string): Observable<License> {
    return this.api.post<License>(`${this.apiUrl}/activate`, { key });
  }
}
