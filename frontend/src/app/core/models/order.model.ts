export interface Order {
  id: number;
  code: string;
  projectId: number;
  projectName: string;
  supplier: string;
  status: OrderStatus;
  orderDate: string;
  expectedDeliveryDate: string;
  actualDeliveryDate?: string;
  totalAmount: number;
  notes?: string;
  items: OrderItem[];
  createdBy?: string;
  approvedBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id?: number;
  materialId: number;
  materialName?: string;
  materialCode?: string;
  quantity: number;
  unitPrice: number;
  totalPrice?: number;
}

export type OrderStatus = 'PENDING' | 'APPROVED' | 'RECEIVED' | 'CANCELLED' ;

export const ORDER_STATUS_LABELS: Record<OrderStatus, string> = {
  'PENDING': 'En attente',
  'APPROVED': 'Approuvée',
  'RECEIVED': 'Reçue',
  'CANCELLED': 'Annulée'
};

export const ORDER_STATUS_COLORS: Record<OrderStatus, string> = {
  'PENDING': 'warning',
  'APPROVED': 'info',
  'RECEIVED': 'success',
  'CANCELLED': 'danger'
};
