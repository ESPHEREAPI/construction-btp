import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Company, CreateCompanyRequest } from '../models/company.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private apiUrl = '/admin/companies';

  constructor(private api: ApiService) {}

  getAllCompanies(): Observable<Company[]> {
    return this.api.get<Company[]>(this.apiUrl);
  }

  createCompany(request: CreateCompanyRequest): Observable<Company> {
    return this.api.post<Company>(this.apiUrl, request);
  }

  setStatus(id: number, active: boolean): Observable<Company> {
    return this.api.put<Company>(`${this.apiUrl}/${id}/status?active=${active}`, {});
  }
}
