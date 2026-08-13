import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  GenerateLicenseRequest,
  License,
  LicenseGenerationResponse,
  LicensePlan,
  LicensePlanUpdateRequest,
  LicenseType
} from '../models/license.model';
import { ApiService } from './api.service';

/** Super Admin only: license plan configuration and per-company license lifecycle. */
@Injectable({
  providedIn: 'root'
})
export class LicenseService {
  private apiUrl = '/admin';

  constructor(private api: ApiService) {}

  getPlans(): Observable<LicensePlan[]> {
    return this.api.get<LicensePlan[]>(`${this.apiUrl}/license-plans`);
  }

  updatePlan(type: LicenseType, request: LicensePlanUpdateRequest): Observable<LicensePlan> {
    return this.api.put<LicensePlan>(`${this.apiUrl}/license-plans/${type}`, request);
  }

  generateLicense(companyId: number, request: GenerateLicenseRequest): Observable<LicenseGenerationResponse> {
    return this.api.post<LicenseGenerationResponse>(`${this.apiUrl}/companies/${companyId}/license/generate`, request);
  }

  revokeLicense(companyId: number): Observable<License> {
    return this.api.post<License>(`${this.apiUrl}/companies/${companyId}/license/revoke`, {});
  }

  suspendLicense(companyId: number): Observable<License> {
    return this.api.post<License>(`${this.apiUrl}/companies/${companyId}/license/suspend`, {});
  }

  reactivateLicense(companyId: number): Observable<License> {
    return this.api.post<License>(`${this.apiUrl}/companies/${companyId}/license/reactivate`, {});
  }

  getCompanyLicenseHistory(companyId: number): Observable<License[]> {
    return this.api.get<License[]>(`${this.apiUrl}/companies/${companyId}/license/history`);
  }

  getSelfRegistrationEnabled(): Observable<{ enabled: boolean }> {
    return this.api.get<{ enabled: boolean }>(`${this.apiUrl}/settings/self-registration`);
  }

  setSelfRegistrationEnabled(enabled: boolean): Observable<{ message: string }> {
    return this.api.put<{ message: string }>(`${this.apiUrl}/settings/self-registration?enabled=${enabled}`, {});
  }
}
