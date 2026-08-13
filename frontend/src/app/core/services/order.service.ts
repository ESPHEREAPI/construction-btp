import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = '/orders';

  constructor(private api: ApiService) {}

  /**
   * Récupérer toutes les commandes avec filtres et pagination
   */
  getAll(projectId?: number, status?: string, page: number = 0, size: number = 10): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (projectId) params = params.set('projectId', projectId.toString());
    if (status) params = params.set('status', status);
    
    return this.api.get<any>(this.apiUrl, params);
  }

  /**
   * Récupérer une commande par ID
   */
  getById(id: number): Observable<Order> {
    return this.api.get<Order>(`${this.apiUrl}/${id}`);
  }

  /**
   * Créer une nouvelle commande
   */
  create(order: any): Observable<Order> {
    return this.api.post<Order>(this.apiUrl, order);
  }

  /**
   * Mettre à jour une commande
   */
  update(id: number, order: any): Observable<Order> {
    return this.api.put<Order>(`${this.apiUrl}/${id}`, order);
  }

  /**
   * Supprimer une commande
   */
  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Approuver une commande
   */
  approve(id: number): Observable<Order> {
    return this.api.post<Order>(`${this.apiUrl}/${id}/approve`, {});
  }

  /**
   * Marquer une commande comme reçue
   */
  receive(id: number): Observable<Order> {
    return this.api.post<Order>(`${this.apiUrl}/${id}/receive`, {});
  }

  /**
   * Annuler une commande
   */
  cancel(id: number): Observable<Order> {
    return this.api.post<Order>(`${this.apiUrl}/${id}/cancel`, {});
  }

  /**
   * Récupérer les commandes d'un projet
   */
  getByProject(projectId: number, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.api.get<any>(`${this.apiUrl}/project/${projectId}`, params);
  }

  /**
   * Récupérer les statistiques des commandes
   */
  getStatistics(): Observable<any> {
    return this.api.get<any>(`${this.apiUrl}/statistics`);
  }

  /**
   * Exporter les commandes en CSV
   */
  exportToCsv(projectId?: number, status?: string): Observable<Blob> {
    let params = new HttpParams();
    if (projectId) params = params.set('projectId', projectId.toString());
    if (status) params = params.set('status', status);
    // responseType: 'blob' as any
    return this.api.get<Blob>(`${this.apiUrl}/export/csv`, params, );
  }
}