import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { AuthService } from '../../../../core/services/auth.service';
import { MyLicenseService } from '../../../../core/services/my-license.service';

const LANG_LABELS: Record<string, string> = { fr: 'FR', en: 'EN', pt: 'PT' };
const EXPIRY_WARNING_DAYS = 30;

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  @Output() toggleSidebar = new EventEmitter<void>();
  currentUser = this.authService.currentUserValue;
  currentLang = 'fr';

  licenseDaysRemaining: number | null = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private translate: TranslateService,
    private myLicenseService: MyLicenseService
  ) {
    this.currentLang = this.translate.currentLang || 'fr';
    this.translate.onLangChange.subscribe(event => {
      this.currentLang = event.lang;
    });
  }

  ngOnInit(): void {
    if (!this.isSuperAdmin) {
      this.myLicenseService.getMine().subscribe({
        next: (mine) => {
          const endDate = mine.current?.endDate;
          if (endDate) {
            const diffMs = new Date(endDate).getTime() - Date.now();
            this.licenseDaysRemaining = Math.max(0, Math.ceil(diffMs / (1000 * 60 * 60 * 24)));
          }
        },
        error: () => {}
      });
    }
  }

  get licenseExpiringSoon(): boolean {
    return this.licenseDaysRemaining !== null && this.licenseDaysRemaining <= EXPIRY_WARNING_DAYS;
  }

  get licenseExpiryMessage(): string {
    const d = this.licenseDaysRemaining;
    if (d === 0) return "Licence expire aujourd'hui";
    if (d === 1) return 'Licence expire demain';
    return `Licence expire dans ${d} jours`;
  }

  get currentLangLabel(): string {
    return LANG_LABELS[this.currentLang] || this.currentLang.toUpperCase();
  }

  get isSuperAdmin(): boolean {
    return this.authService.isSuperAdmin();
  }

  onToggleSidebar(): void {
    this.toggleSidebar.emit();
  }

  changeLanguage(lang: string): void {
    this.translate.use(lang);
    this.currentLang = lang;
  }

  logout(): void {
    if (confirm(this.translate.instant('auth.logout_confirm'))) {
      this.authService.logout();
      this.router.navigate(['/auth/login']);
    }
  }
}
