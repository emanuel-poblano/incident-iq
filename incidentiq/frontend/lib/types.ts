// Mirrors the backend enums/DTOs. Expanded in Phase 3+ as the real
// Incident/Investigation endpoints are implemented.

export type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type Environment = "DEVELOPMENT" | "TEST" | "STAGING" | "PRODUCTION";
export type IncidentStatus = "OPEN" | "INVESTIGATING" | "RESOLVED";

export interface Incident {
  id: number;
  title: string;
  description: string;
  service: string;
  environment: Environment;
  severity: Severity;
  status: IncidentStatus;
  rawLogs: string;
  createdAt: string;
  updatedAt: string;
}
