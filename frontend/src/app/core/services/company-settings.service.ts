import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CompanySettings } from '../models/company-settings.model';
import { ApiService } from './api.service';

/** Self-service: any authenticated company user can view; update is Company Admin/Admin only (enforced server-side). */
@Injectable({
  providedIn: 'root'
})
export class CompanySettingsService {
  private apiUrl = '/companies/me/settings';

  constructor(private api: ApiService) {}

  getMine(): Observable<CompanySettings> {
    return this.api.get<CompanySettings>(this.apiUrl);
  }

  updateMine(currency: string): Observable<CompanySettings> {
    return this.api.put<CompanySettings>(this.apiUrl, { currency });
  }
}
