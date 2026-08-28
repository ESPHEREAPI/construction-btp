import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { CompanySettingsService } from '../../../core/services/company-settings.service';
import { CurrencyService } from '../../../core/services/currency.service';
import { CURRENCY_PRESETS, DEFAULT_CURRENCY } from '../../../core/models/company-settings.model';

const CUSTOM_OPTION = '__custom__';

@Component({
  selector: 'app-company-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './company-settings.component.html',
  styleUrls: ['./company-settings.component.scss']
})
export class CompanySettingsComponent implements OnInit {
  readonly presets = CURRENCY_PRESETS;

  loading = true;
  saving = false;
  error = '';
  success = '';

  selectedPreset = DEFAULT_CURRENCY;
  customCurrency = '';

  constructor(
    private companySettingsService: CompanySettingsService,
    private currencyService: CurrencyService
  ) {}

  get isCustom(): boolean {
    return this.selectedPreset === CUSTOM_OPTION;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.companySettingsService.getMine().subscribe({
      next: (data) => {
        const currency = data.currency || DEFAULT_CURRENCY;
        const preset = this.presets.find(p => p.value === currency);
        if (preset) {
          this.selectedPreset = preset.value;
        } else {
          this.selectedPreset = CUSTOM_OPTION;
          this.customCurrency = currency;
        }
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Erreur lors du chargement des paramètres';
      }
    });
  }

  save(): void {
    const currency = this.isCustom ? this.customCurrency.trim() : this.selectedPreset;
    if (!currency) {
      this.error = 'Veuillez indiquer une devise';
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';
    this.companySettingsService.updateMine(currency).subscribe({
      next: () => {
        this.saving = false;
        this.success = 'Paramètres enregistrés avec succès';
        this.currencyService.refresh().subscribe();
      },
      error: (err) => {
        this.saving = false;
        this.error = err?.error?.message || "Erreur lors de l'enregistrement";
      }
    });
  }
}
