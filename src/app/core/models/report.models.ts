export interface TransfersReportResponse {
  reportType: string;
  format: string;
  generatedAt: string;
  totalTransfers: number;
  totalVolume: number;
  totalFees: number;
  transfersByStatus: Record<string, number>;
}

export interface AgencyReportResponse {
  agencyId: number;
  agencyName: string;
  agencyCode: string;
  format: string;
  generatedAt: string;
  totalTransfers: number;
  totalVolume: number;
  totalAgencyCommissions: number;
}

export interface CommissionsReportResponse {
  reportType: string;
  format: string;
  agencyId?: number;
  generatedAt: string;
  totalAgencyCommissions: number;
  totalCentralCommissions: number;
}
