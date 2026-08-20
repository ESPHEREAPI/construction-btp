export interface UserManagement {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  active: boolean;
  roles: Role[];
  permissions: string[];
  preferredLanguage: string;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
  assignedProjectId?: number;
  assignedProjectName?: string;
}

export interface Role {
  id: number;
  name: string;
  description: string;
  nameFr?: string;
  nameEn?: string;
  namePt?: string;
  systemRole?: boolean;
  custom?: boolean;
  permissions: Permission[];
}

export interface RoleRequest {
  nameFr: string;
  nameEn?: string;
  namePt?: string;
  permissionIds: number[];
}

export interface Permission {
  id: number;
  name: string;
  description: string;
  category: string;
}
