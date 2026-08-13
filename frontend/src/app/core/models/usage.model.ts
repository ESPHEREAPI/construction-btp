export interface Usage {
  id: number;
  projectId: number;
  projectName: string;
  materialId: number;
  materialName: string;
  materialCode: string;
  quantity: number;
  unit: string;
  usageDate: string;
  notes?: string;
  usedBy?: string;
  recordedBy?: string;
  createdAt: string;
  updatedAt: string;
}
