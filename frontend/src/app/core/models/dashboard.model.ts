export interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  totalMaterials: number;
  totalConsumption: number;
  totalBudget: number;
  alertsCount: number;
  projectStats: ProjectStats[];
  materialUsageStats: MaterialUsageStats[];
}

export interface ProjectStats {
  projectName: string;
  budgetUsed: number;
  materialsCount: number;
}

export interface MaterialUsageStats {
  materialName: string;
  quantityUsed: number;
  unit: string;
}
