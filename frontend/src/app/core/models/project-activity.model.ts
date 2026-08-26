export type ActivityAction = 'APPROVE' | 'RECEIVE' | 'CANCEL' | 'UPDATE';

export interface ProjectActivity {
  id: number;
  entityType: string;
  action: ActivityAction;
  entityId: number;
  description: string;
  performedBy?: string;
  timestamp: string;
}
