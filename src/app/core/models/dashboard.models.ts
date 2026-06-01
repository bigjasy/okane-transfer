export interface DashboardSummaryResponse { totalVolume: number; transferCount: number; totalFees: number; totalCommissions: number; charts: Record<string, unknown>; }
export interface StatItem { label: string; value: string | number; hint?: string; }
export interface ChartPoint { label: string; value: number; }
