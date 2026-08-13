import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

/** LICENSE_EXPIRED | LICENSE_SUSPENDED | LICENSE_REQUIRED | LICENSE_MODULE_NOT_ACTIVE */
export type LicenseBlockCode = string;

/** Shared code set by ErrorInterceptor when the backend rejects a request with a 402 license error. */
@Injectable({
  providedIn: 'root'
})
export class LicenseStatusService {
  private blockedSubject = new BehaviorSubject<LicenseBlockCode | null>(null);
  public blocked$ = this.blockedSubject.asObservable();

  markBlocked(code: LicenseBlockCode): void {
    this.blockedSubject.next(code);
  }

  clear(): void {
    this.blockedSubject.next(null);
  }
}
