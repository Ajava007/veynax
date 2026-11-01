export interface PartRecord {
  id: number;
  partCode: string;
  name: string;
  description?: string | null;
  uom: string;
  category?: string | null;
  standardCost?: number | null;
  revision?: string | null;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface PartRequest {
  partCode: string;
  name: string;
  description?: string | null;
  uom: string;
  category?: string | null;
  standardCost?: number | null;
  revision?: string | null;
  status?: string;
}

export interface BomRowRecord {
  id: number;
  parentPartCode: string;
  componentPartCode: string;
  quantity: number;
  scrapFactor: number;
  optional: boolean;
  level: number;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  remark?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface BomRowRequest {
  parentPartCode: string;
  componentPartCode: string;
  quantity: number;
  scrapFactor?: number | null;
  optional?: boolean | null;
  level?: number | null;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  remark?: string | null;
}

export interface RoutingOperationRecord {
  id: number;
  modelCode: string;
  operationNumber: number;
  operationName: string;
  workCenter: string;
  setupTimeSec: number;
  cycleTimeSec: number;
  instructions?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface RoutingOperationRequest {
  modelCode: string;
  operationNumber: number;
  operationName: string;
  workCenter: string;
  setupTimeSec?: number | null;
  cycleTimeSec: number;
  instructions?: string | null;
}
