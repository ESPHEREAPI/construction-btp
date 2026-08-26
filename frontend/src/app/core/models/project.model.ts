export interface Project {
  id?: number;
  code: string;
  name: string;
  description?: string;
  location?: string;
  client?: string;
  status: ProjectStatus;
  startDate: Date;
  endDate?: Date;
  estimatedEndDate?: Date;
  budget?: number;
  spentAmount?: number;
  pendingAmount?: number;
  pendingOrdersCount?: number;
  staleOrdersCount?: number;
  projectManagerName?: string;
  siteManagerName?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

export enum ProjectStatus {
  PLANNED = 'PLANNED',
  IN_PROGRESS = 'IN_PROGRESS',
  ON_HOLD = 'ON_HOLD',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}
