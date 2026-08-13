import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LicenseModule } from '../models/license.model';
import { MyLicenseService } from './my-license.service';

/**
 * Shared, refreshable source of the current company's licensed modules, used
 * by the sidebar (and anywhere else that needs to react to a license change).
 * Deliberately NOT derived from the JWT: license state must take effect
 * without forcing a re-login, unlike companyId/mustChangePassword.
 */
@Injectable({
  providedIn: 'root'
})
export class ActiveModulesService {
  private modulesSubject = new BehaviorSubject<LicenseModule[]>([]);
  public modules$ = this.modulesSubject.asObservable();

  constructor(private myLicenseService: MyLicenseService) {}

  refresh() {
    return this.myLicenseService.getMine().pipe(
      tap(mine => this.modulesSubject.next(mine.current?.activeModules ?? []))
    );
  }

  get current(): LicenseModule[] {
    return this.modulesSubject.value;
  }
}
