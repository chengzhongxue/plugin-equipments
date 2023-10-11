export interface Metadata {
  name: string;
  generateName?: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface EquipmentGroupSpec {
  displayName: string;
  priority?: number;
}

export interface PostGroupStatus {
  equipmentCount: number;
}

export interface EquipmentSpec {
  displayName: string;
  description?: string;
  url: string;
  cover?: string;
  priority?: number;
  groupName: string;
}

export interface Equipment {
  spec: EquipmentSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface EquipmentGroup {
  spec: EquipmentGroupSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  status: PostGroupStatus;
}

export interface EquipmentList {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: Array<Equipment>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface EquipmentGroupList {
  page: number;
  size: number;
  total: number;
  items: Array<EquipmentGroup>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
