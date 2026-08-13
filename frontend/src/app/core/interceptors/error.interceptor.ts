import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { LicenseStatusService } from '../services/license-status.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(
    private authService: AuthService,
    private router: Router,
    private licenseStatusService: LicenseStatusService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = '';

        console.group('🔴 HTTP ERROR INTERCEPTED');
        console.error('📍 URL:', error.url);
        console.error('📍 Method:', request.method);
        console.error('📍 Status:', error.status);
        console.error('📍 Status Text:', error.statusText);
        console.error('📍 Message:', error.message);

        if (error.status === 0) {
          errorMessage = '🔌 Impossible de joindre le serveur backend';
          console.error('');
          console.error('💡 CAUSES POSSIBLES:');
          console.error('   ❌ Le serveur Spring Boot n\'est pas démarré');
          console.error('   ❌ Le serveur n\'écoute pas sur http://localhost:8080');
          console.error('   ❌ Problème CORS (vérifiez @CrossOrigin dans le contrôleur)');
          console.error('   ❌ Firewall ou antivirus bloque la connexion');
          console.error('');
          console.error('🔧 SOLUTIONS:');
          console.error('   1. Démarrez votre application Spring Boot');
          console.error('   2. Vérifiez que le serveur écoute sur le bon port');
          console.error('   3. Testez: curl http://localhost:8080/api/orders');
          console.error('   4. Vérifiez les logs du backend Spring Boot');
          
        } else if (error.status === 401) {
          errorMessage = '🔐 Session expirée ou non autorisé';
          console.error('💡 Token JWT manquant, invalide ou expiré');
          console.error('🔧 Redirection vers la page de connexion...');
          
          this.authService.logout();
          this.router.navigate(['/login'], {
            queryParams: { returnUrl: this.router.url }
          });
          
        } else if (error.status === 403) {
          errorMessage = '🚫 Accès interdit - Permissions insuffisantes';
          console.error('💡 Vous n\'avez pas les droits pour accéder à cette ressource');

        } else if (error.status === 402) {
          errorMessage = error.error?.message || '🔒 Licence requise';
          console.error('💡 Code:', error.error?.code);
          if (error.error?.code) {
            this.licenseStatusService.markBlocked(error.error.code);
          }

        } else if (error.status === 404) {
          errorMessage = `❌ Ressource non trouvée`;
          console.error('💡 L\'endpoint n\'existe pas ou est mal configuré dans le backend');
          
        } else if (error.status === 400) {
          errorMessage = '⚠️ Données de la requête invalides';
          console.error('💡 Erreur de validation:', error.error);
          
        } else if (error.status === 500) {
          errorMessage = '💥 Erreur interne du serveur';
          console.error('💡 Erreur côté backend:', error.error);
          console.error('💡 Message du serveur:', error.error?.message);
          
        } else if (error.status === 503) {
          errorMessage = '🔧 Service temporairement indisponible';
          console.error('💡 Le serveur est en maintenance ou surchargé');
          
        } else {
          errorMessage = `Erreur ${error.status}: ${error.statusText || 'Erreur inconnue'}`;
          console.error('💡 Erreur HTTP non gérée explicitement');
        }

        console.error('');
        console.error('📦 Response Body:', error.error);
        console.error('📦 Full Error Object:', error);
        console.groupEnd();

        return throwError(() => ({
          status: error.status,
          statusText: error.statusText,
          message: errorMessage,
          url: error.url,
          timestamp: new Date(),
          error: error.error,
          originalError: error
        }));
      })
    );
  }
}