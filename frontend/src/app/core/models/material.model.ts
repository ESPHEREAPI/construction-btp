export interface Material {
  id?: number;
  code: string;
  name: string;
  description?: string;
  unit: MaterialUnit;
  category?: string;
  unitPrice?: number;
  supplier?: string;
  active: boolean;
  createdAt?: Date;
  /** Null = shared catalog material (visible to every company, read/hide-only). Set = owned by that company. */
  company?: { id: number; name: string } | null;
}

export enum MaterialUnit {
  KG = 'KG',
  TON = 'TON',
  M3 = 'M3',
  M2 = 'M2',
  PIECE = 'PIECE',
  LITER = 'LITER',
  METER = 'METER',
  BAG = 'BAG',
  BOX = 'BOX',
  ROLL = 'ROLL'
}
