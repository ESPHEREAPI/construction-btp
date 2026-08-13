import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Usage } from '../models/usage.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class UsageService {
  private apiUrl = '/usages';

  constructor(private api: ApiService) {}

  getAll(projectId?: number, materialId?: number, page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (projectId) params = params.set('projectId', projectId.toString());
    if (materialId) params = params.set('materialId', materialId.toString());
    
    return this.api.get<any>(this.apiUrl, params );
  }

  getById(id: number): Observable<Usage> {
    return this.api.get<Usage>(`${this.apiUrl}/${id}`);
  }

  create(usage: any): Observable<Usage> {
    return this.api.post<Usage>(this.apiUrl, usage);
  }

  update(id: number, usage: any): Observable<Usage> {
    return this.api.put<Usage>(`${this.apiUrl}/${id}`, usage);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByProject(projectId: number, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.api.get<any>(`${this.apiUrl}/project/${projectId}`,  params );
  }
}
