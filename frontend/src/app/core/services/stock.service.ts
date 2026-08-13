import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = '/stocks';

  constructor(private api: ApiService) {}

  getAll(projectId?: number, page: number = 0, size: number = 1000): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (projectId) params = params.set('projectId', projectId.toString());
    
    return this.api.get<any>(this.apiUrl, params);
  }

  getById(id: number): Observable<any> {
    return this.api.get<any>(`${this.apiUrl}/${id}`);
  }

  getByProject(projectId: number, page: number = 0, size: number = 1000): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.api.get<any>(`${this.apiUrl}/project/${projectId}`, params);
  }

  getLowStockAlerts(projectId?: number): Observable<any> {
    let url = `${this.apiUrl}/alerts`;
    if (projectId) {
      url += `/${projectId}`;
    }
    return this.api.get<any>(url);
  }

  addMovement(movement: any): Observable<any> {
    return this.api.post<any>(`${this.apiUrl}/movement`, movement);
  }

  getMovements(stockId: number, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.api.get<any>(`${this.apiUrl}/${stockId}/movements`, params);
  }

  updateThresholds(id: number, data: any): Observable<any> {
    return this.api.put<any>(`${this.apiUrl}/${id}/thresholds`, data);
  }
}
