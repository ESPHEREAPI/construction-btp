export interface Stock {
  id: number;
  projectId: number;
  projectName: string;
  materialId: number;
  materialName: string;
  materialCode: string;
  currentQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minimumQuantity?: number;
  maximumQuantity?: number;
  location?: string;
  lowStockAlert: boolean;
  unit: string;
  createdAt: string;
  updatedAt: string;
}

export interface StockMovement {
  id: number;
  stockId: number;
  movementType: MovementType;
  quantity: number;
  balanceBefore: number;
  balanceAfter: number;
  reference?: string;
  notes?: string;
  performedBy?: string;
  createdAt: string;
}

export type MovementType = 'IN' | 'OUT' | 'TRANSFER' | 'ADJUSTMENT';

export const MOVEMENT_TYPE_LABELS: Record<MovementType, string> = {
  'IN': 'Entrée',
  'OUT': 'Sortie',
  'TRANSFER': 'Transfert',
  'ADJUSTMENT': 'Ajustement'
};

export const MOVEMENT_TYPE_COLORS: Record<MovementType, string> = {
  'IN': 'success',
  'OUT': 'danger',
  'TRANSFER': 'info',
  'ADJUSTMENT': 'warning'
};
