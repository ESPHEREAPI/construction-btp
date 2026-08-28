import { Pipe, PipeTransform, Inject, LOCALE_ID } from '@angular/core';
import { formatNumber } from '@angular/common';
import { CurrencyService } from '../../core/services/currency.service';

/**
 * Formats a number the same way `| number` does, then appends the current company's
 * currency label (e.g. "3,762,000 FCFA") - replaces every hardcoded "FCFA" suffix in
 * templates so the label reflects each company's own configured currency.
 */
@Pipe({
  name: 'appCurrency',
  standalone: true,
  pure: false
})
export class AppCurrencyPipe implements PipeTransform {
  constructor(private currencyService: CurrencyService, @Inject(LOCALE_ID) private locale: string) {}

  transform(value: number | string | null | undefined, digitsInfo = '1.0-0'): string {
    if (value == null || value === '') {
      return '';
    }
    const numericValue = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(numericValue)) {
      return '';
    }
    return `${formatNumber(numericValue, this.locale, digitsInfo)} ${this.currencyService.current}`;
  }
}
