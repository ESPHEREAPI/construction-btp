import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Material } from '../models/material.model';
import { HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class MaterialService {
  private readonly apiUrl = '/materials';

  constructor(private api: ApiService) {}

 

  /**
   * Récupérer tous les matériaux avec pagination
   */
  getAll(page: number = 0, size: number = 1000): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(this.apiUrl, params);
  }

  /**
   * Récupérer un matériau par ID
   */
  getById(id: number): Observable<any> {
    return this.api.get<any>(`${this.apiUrl}/${id}`);
  }

  /**
   * Créer un nouveau matériau
   */
  create(material: any): Observable<any> {
    return this.api.post<any>(this.apiUrl, material);
  }

  /**
   * Mettre à jour un matériau
   */
  update(id: number, material: any): Observable<any> {
    return this.api.put<any>(`${this.apiUrl}/${id}`, material);
  }

  /**
   * Supprimer un matériau
   */
  delete(id: number): Observable<void> {
    return this.api.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Rechercher des matériaux
   */
  search(query: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(`${this.apiUrl}/search`, params);
  }

  /**
   * Récupérer les matériaux par catégorie
   */
  getByCategory(category: string, page: number = 0, size: number = 100): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(`${this.apiUrl}/category/${category}`, params);
  }

  /**
   * Récupérer toutes les catégories de matériaux
   */
  getCategories(): Observable<string[]> {
    return this.api.get<string[]>(`${this.apiUrl}/categories`);
  }

  /**
   * Récupérer tous les fournisseurs de matériaux
   */
  getSuppliers(): Observable<string[]> {
    return this.api.get<string[]>(`${this.apiUrl}/suppliers`);
  }

  /**
   * Masquer un matériau du catalogue partagé (n'affecte que la compagnie courante)
   */
  hide(id: number): Observable<void> {
    return this.api.put<void>(`${this.apiUrl}/${id}/hide`, {});
  }

  /**
   * Réafficher un matériau du catalogue partagé précédemment masqué
   */
  unhide(id: number): Observable<void> {
    return this.api.put<void>(`${this.apiUrl}/${id}/unhide`, {});
  }

  /**
   * Récupérer les matériaux du catalogue partagé masqués par la compagnie courante
   */
  getHidden(): Observable<Material[]> {
    return this.api.get<Material[]>(`${this.apiUrl}/hidden`);
  }

  /**
   * Récupérer les matériaux disponibles (avec stock)
   */
  getAvailable(page: number = 0, size: number = 100): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.api.get<any>(`${this.apiUrl}/available`, params);
  }
}
