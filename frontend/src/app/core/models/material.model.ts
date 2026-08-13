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
