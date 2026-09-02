import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { MyLicenseService } from '../../core/services/my-license.service';
import { ActiveModulesService } from '../../core/services/active-modules.service';
import { AuthService } from '../../core/services/auth.service';
import { License, LicenseMine } from '../../core/models/license.model';

@Component({
  selector: 'app-my-license',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './my-license.component.html',
  styleUrls: ['./my-license.component.scss']
})
export class MyLicenseComponent implements OnInit {
  mine: LicenseMine | null = null;
  history: License[] = [];
  loading = true;
  loadingHistory = true;
  loadError = '';
  historyError = '';

  keyToActivate = '';
  activating = false;
  activateError = '';
  activateSuccess = '';

  constructor(
    private myLicenseService: MyLicenseService,
    private activeModulesService: ActiveModulesService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadHistory();
  }

  get isCompanyAdmin(): boolean {
    return this.authService.isCompanyAdmin();
  }

  daysRemaining(endDate: string | null): number | null {
    if (!endDate) return null;
    const diff = new Date(endDate).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  load(): void {
    this.loading = true;
    this.loadError = '';
    this.myLicenseService.getMine().subscribe({
      next: (data) => {
        this.mine = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.loadError = err?.error?.message || 'Erreur lors du chargement de la licence';
      }
    });
  }

  loadHistory(): void {
    this.loadingHistory = true;
    this.historyError = '';
    this.myLicenseService.getHistory().subscribe({
      next: (data) => {
        this.history = data;
        this.loadingHistory = false;
      },
      error: (err) => {
        this.loadingHistory = false;
        this.historyError = err?.error?.message || "Erreur lors du chargement de l'historique";
      }
    });
  }

  activate(): void {
    if (!this.keyToActivate.trim()) {
      this.activateError = 'Collez une clé de licence';
      return;
    }

    this.activating = true;
    this.activateError = '';
    this.activateSuccess = '';

    this.myLicenseService.activate(this.keyToActivate.trim()).subscribe({
      next: () => {
        this.activating = false;
        this.activateSuccess = 'Licence activée avec succès';
        this.keyToActivate = '';
        this.load();
        this.loadHistory();
        this.activeModulesService.refresh().subscribe();
      },
      error: (err) => {
        this.activating = false;
        this.activateError = err?.error?.message || 'Clé invalide';
      }
    });
  }
}
