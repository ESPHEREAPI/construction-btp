import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { LicenseService } from '../../../core/services/license.service';
import { ALL_LICENSE_MODULES, LicenseModule, LicensePlan } from '../../../core/models/license.model';

@Component({
  selector: 'app-license-config',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  templateUrl: './license-config.component.html',
  styleUrls: ['./license-config.component.scss']
})
export class LicenseConfigComponent implements OnInit {
  plans: LicensePlan[] = [];
  loading = true;
  error = '';
  savingType: string | null = null;

  selfRegistrationEnabled = true;
  loadingSetting = true;
  settingError = '';

  readonly allModules = ALL_LICENSE_MODULES;

  constructor(private licenseService: LicenseService) {}

  ngOnInit(): void {
    this.loadPlans();
    this.loadSelfRegistrationSetting();
  }

  loadPlans(): void {
    this.loading = true;
    this.licenseService.getPlans().subscribe({
      next: (data) => {
        this.plans = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des plans de licence';
        this.loading = false;
        console.error(err);
      }
    });
  }

  loadSelfRegistrationSetting(): void {
    this.loadingSetting = true;
    this.settingError = '';
    this.licenseService.getSelfRegistrationEnabled().subscribe({
      next: (data) => {
        this.selfRegistrationEnabled = data.enabled;
        this.loadingSetting = false;
      },
      error: (err) => {
        this.loadingSetting = false;
        this.settingError = err?.error?.message || 'Erreur lors du chargement du paramètre';
      }
    });
  }

  toggleModule(plan: LicensePlan, module: LicenseModule): void {
    const index = plan.modules.indexOf(module);
    if (index >= 0) {
      plan.modules.splice(index, 1);
    } else {
      plan.modules.push(module);
    }
  }

  savePlan(plan: LicensePlan): void {
    this.savingType = plan.type;
    this.licenseService.updatePlan(plan.type, {
      durationDays: plan.durationDays,
      maxUsers: plan.maxUsers,
      maxProjects: plan.maxProjects,
      modules: plan.modules
    }).subscribe({
      next: () => {
        this.savingType = null;
      },
      error: () => {
        this.savingType = null;
        alert('Erreur lors de l\'enregistrement du plan');
      }
    });
  }

  toggleSelfRegistration(): void {
    const newValue = !this.selfRegistrationEnabled;
    this.licenseService.setSelfRegistrationEnabled(newValue).subscribe({
      next: () => {
        this.selfRegistrationEnabled = newValue;
      },
      error: () => alert('Erreur lors de la mise à jour du paramètre')
    });
  }
}
