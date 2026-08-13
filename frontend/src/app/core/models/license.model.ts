export type LicenseType = 'TRIAL' | 'PAYANT';
export type LicenseStatus = 'GENERATED' | 'ACTIVE' | 'SUSPENDED' | 'EXPIRED' | 'REVOKED';
export type LicenseModule = 'PROJECTS' | 'MATERIALS' | 'ORDERS' | 'USAGE' | 'STOCK' | 'ADMIN';

/** Drives every module-checkbox list in the UI - never hardcode the module names elsewhere. */
export const ALL_LICENSE_MODULES: { value: LicenseModule; labelKey: string }[] = [
  { value: 'PROJECTS', labelKey: 'nav.projects' },
  { value: 'MATERIALS', labelKey: 'nav.materials' },
  { value: 'ORDERS', labelKey: 'nav.orders' },
  { value: 'USAGE', labelKey: 'nav.usage' },
  { value: 'STOCK', labelKey: 'nav.stock' },
  { value: 'ADMIN', labelKey: 'nav.admin' }
];

export interface License {
  id: number;
  companyId: number;
  type: LicenseType;
  status: LicenseStatus;
  startDate: string | null;
  endDate: string | null;
  maxUsers: number;
  maxProjects: number;
  activeModules: LicenseModule[];
  trial: boolean;
  createdBy: string;
  createdAt: string;
  revokedBy?: string | null;
  revokedAt?: string | null;
}

export interface GenerateLicenseRequest {
  type: LicenseType;
  endDate: string | null;
  maxUsers: number;
  maxProjects: number;
  modules: LicenseModule[];
}

export interface LicenseGenerationResponse {
  license: License;
  licenseKey: string;
}

export interface LicenseMine {
  current: License | null;
  pending: License | null;
}

export interface LicensePlan {
  id: number;
  type: LicenseType;
  durationDays: number | null;
  maxUsers: number;
  maxProjects: number;
  modules: LicenseModule[];
  updatedAt: string;
}

export interface LicensePlanUpdateRequest {
  durationDays: number | null;
  maxUsers: number;
  maxProjects: number;
  modules: LicenseModule[];
}
