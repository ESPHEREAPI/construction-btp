import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { DEFAULT_CURRENCY } from '../models/company-settings.model';
import { CompanySettingsService } from './company-settings.service';

/**
 * Shared, refreshable source of the current company's currency label, used by
 * AppCurrencyPipe everywhere a monetary amount is displayed. Deliberately NOT
 * derived from the JWT: a currency change must take effect without forcing a
 * re-login, same reasoning as ActiveModulesService for license modules.
 */
@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private currencySubject = new BehaviorSubject<string>(DEFAULT_CURRENCY);
  public currency$ = this.currencySubject.asObservable();

  constructor(private companySettingsService: CompanySettingsService) {}

  refresh() {
    return this.companySettingsService.getMine().pipe(
      tap(settings => this.currencySubject.next(settings.currency || DEFAULT_CURRENCY))
    );
  }

  get current(): string {
    return this.currencySubject.value;
  }
}
