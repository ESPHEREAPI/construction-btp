import { Component, EventEmitter, Input, OnChanges, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LicenseService } from '../../../core/services/license.service';
import { Company } from '../../../core/models/company.model';
import {
  ALL_LICENSE_MODULES,
  GenerateLicenseRequest,
  License,
  LicenseGenerationResponse,
  LicenseModule
} from '../../../core/models/license.model';

@Component({
  selector: 'app-company-license-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './company-license-modal.component.html',
  styleUrls: ['./company-license-modal.component.scss']
})
export class CompanyLicenseModalComponent implements OnChanges {
  @Input({ required: true }) company!: Company;
  @Output() closed = new EventEmitter<void>();
  @Output() changed = new EventEmitter<void>();

  readonly allModules = ALL_LICENSE_MODULES;

  history: License[] = [];
  loadingHistory = true;
  error = '';

  form: GenerateLicenseRequest = this.emptyForm();
  generating = false;
  generationResult: LicenseGenerationResponse | null = null;
  copied = false;

  actionBusy = false;

  constructor(private licenseService: LicenseService) {}

  ngOnChanges(): void {
    this.generationResult = null;
    this.error = '';
    this.form = this.emptyForm();
    this.loadHistory();
  }

  private emptyForm(): GenerateLicenseRequest {
    return { type: 'PAYANT', endDate: null, maxUsers: 5, maxProjects: 3, modules: [] };
  }

  loadHistory(): void {
    this.loadingHistory = true;
    this.licenseService.getCompanyLicenseHistory(this.company.id).subscribe({
      next: (data) => {
        this.history = data;
        this.loadingHistory = false;
      },
      error: () => {
        this.history = [];
        this.loadingHistory = false;
      }
    });
  }

  toggleModule(module: LicenseModule): void {
    const index = this.form.modules.indexOf(module);
    if (index >= 0) {
      this.form.modules.splice(index, 1);
    } else {
      this.form.modules.push(module);
    }
  }

  generate(): void {
    if (this.form.modules.length === 0) {
      this.error = 'Sélectionnez au moins un module';
      return;
    }
    this.generating = true;
    this.error = '';

    this.licenseService.generateLicense(this.company.id, this.form).subscribe({
      next: (result) => {
        this.generating = false;
        this.generationResult = result;
        this.changed.emit();
        this.loadHistory();
      },
      error: (err) => {
        this.generating = false;
        this.error = err?.error?.message || 'Erreur lors de la génération';
      }
    });
  }

  copyKey(): void {
    if (!this.generationResult) return;
    navigator.clipboard.writeText(this.generationResult.licenseKey).then(() => {
      this.copied = true;
      setTimeout(() => (this.copied = false), 2000);
    });
  }

  revoke(): void {
    if (!confirm('Révoquer la licence actuelle de cette compagnie ? Cette action est définitive.')) return;
    this.actionBusy = true;
    this.licenseService.revokeLicense(this.company.id).subscribe({
      next: () => { this.actionBusy = false; this.changed.emit(); this.loadHistory(); },
      error: () => { this.actionBusy = false; alert('Erreur lors de la révocation'); }
    });
  }

  suspend(): void {
    this.actionBusy = true;
    this.licenseService.suspendLicense(this.company.id).subscribe({
      next: () => { this.actionBusy = false; this.changed.emit(); this.loadHistory(); },
      error: () => { this.actionBusy = false; alert('Erreur lors de la suspension'); }
    });
  }

  reactivate(): void {
    this.actionBusy = true;
    this.licenseService.reactivateLicense(this.company.id).subscribe({
      next: () => { this.actionBusy = false; this.changed.emit(); this.loadHistory(); },
      error: () => { this.actionBusy = false; alert('Erreur lors de la réactivation'); }
    });
  }

  close(): void {
    this.closed.emit();
  }

  moduleLabel(module: LicenseModule): string {
    return module;
  }

  get latest(): License | null {
    return this.history.length > 0 ? this.history[0] : null;
  }
}
