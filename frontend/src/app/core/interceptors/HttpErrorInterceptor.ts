import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = '';

        if (error.status === 0) {
          // Erreur côté client ou réseau
          errorMessage = `❌ Impossible de joindre le serveur. Vérifiez que le backend est démarré sur http://localhost:8080`;
          console.error('Erreur réseau:', error);
        } else {
          // Erreur côté serveur
          errorMessage = `Erreur ${error.status}: ${error.error?.message || error.message}`;
          console.error(`Backend returned code ${error.status}`, error.error);
        }

        // Afficher dans la console pour debug
        console.error('📛 Erreur complète:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error,
          url: error.url
        });

        return throwError(() => new Error(errorMessage));
      })
    );
  }
}