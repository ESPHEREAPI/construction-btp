import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Project } from '../models/project.model';
import { environment } from '../../../environments/environment';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly apiUrl = '/projects';

  constructor(private api: ApiService) {}


  /**
   * Récupérer tous les projets avec pagination
   */
  getAll(page: number = 0, size: number = 1000): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(this.apiUrl, params);
  }

  /**
   * Récupérer un projet par ID
   */
  getById(id: number): Observable<any> {
    return this.api.get<any>(`${this.apiUrl}/${id}`);
  }

  /**
   * Créer un nouveau projet
   */
  create(project: any): Observable<any> {
    return this.api.post<any>(this.apiUrl, project);
  }

  /**
   * Mettre à jour un projet
   */
  update(id: number, project: any): Observable<any> {
    return this.api.put<any>(`${this.apiUrl}/${id}`, project);
  }

  /**
   * Supprimer un projet
   */
  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Rechercher des projets
   */
  search(query: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(`${this.apiUrl}/search`, params);
  }

  /**
   * Récupérer les projets actifs
   */
  getActive(page: number = 0, size: number = 100): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(`${this.apiUrl}/active`, params);
  }

  /**
   * Récupérer les statistiques d'un projet
   */
  getStatistics(id: number): Observable<any> {
    return this.api.get<any>(`${this.apiUrl}/${id}/statistics`);
  }
}
