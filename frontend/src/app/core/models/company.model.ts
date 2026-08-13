import { LicenseModule, LicenseStatus, LicenseType } from './license.model';

export interface Company {
  id: number;
  name: string;
  code: string;
  active: boolean;
  selfRegistered: boolean;
  createdAt: string;

  licenseType?: LicenseType | null;
  licenseStatus?: LicenseStatus | null;
  licenseStartDate?: string | null;
  licenseEndDate?: string | null;
  maxUsers?: number | null;
  maxProjects?: number | null;
  activeModules?: LicenseModule[] | null;
  currentUserCount: number;
  currentProjectCount: number;
  licenseBlocked: boolean;
  /** True when the latest license row exists but is still GENERATED (key not yet activated). */
  licenseKeyPending: boolean;
}

/** No license fields here - the Super Admin generates one separately, after the company exists. */
export interface CreateCompanyRequest {
  companyName: string;
  adminUsername: string;
  adminEmail: string;
  adminPassword: string;
  adminFirstName?: string;
  adminLastName?: string;
  adminPhone?: string;
  preferredLanguage?: string;
}
