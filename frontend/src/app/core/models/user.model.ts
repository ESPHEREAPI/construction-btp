export interface User {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  active: boolean;
  preferredLanguage: string;
  roles: string[];
  companyId?: number | null;
  companyName?: string | null;
  mustChangePassword?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
  preferredLanguage: string;
  companyId?: number | null;
  companyName?: string | null;
  mustChangePassword: boolean;
}

export interface RegisterRequest {
  companyName: string;
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  preferredLanguage?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
