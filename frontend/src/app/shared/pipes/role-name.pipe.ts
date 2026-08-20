import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Role } from '../../core/models/admin.model';

/**
 * Resolves a role's display name in the active UI language (nameFr/nameEn/namePt),
 * falling back to nameFr then the technical name - roles created before per-company
 * labels existed, or with an EN/PT left blank, always have at least nameFr set.
 */
@Pipe({
  name: 'roleName',
  standalone: true,
  pure: false
})
export class RoleNamePipe implements PipeTransform {
  constructor(private translate: TranslateService) {}

  transform(role: Role | null | undefined): string {
    if (!role) {
      return '';
    }
    const lang = this.translate.currentLang || this.translate.defaultLang;
    if (lang === 'en' && role.nameEn) {
      return role.nameEn;
    }
    if (lang === 'pt' && role.namePt) {
      return role.namePt;
    }
    return role.nameFr || role.name;
  }
}
