// Monitoring Types - Prometheus Integration
export interface PrometheusQueryResponse {
  status: string;
  data: {
    resultType: string;
    result: PrometheusMetric[];
  };
}

export interface PrometheusMetric {
  metric: {
    [key: string]: string | undefined; // labels
    application?: string;
    service?: string;
    instance?: string;
  };
  value?: [number, string]; // [timestamp, value]
  values?: [number, string][]; // for range queries
}

export interface ServiceMetrics {
  name: string;
  status: "healthy" | "degraded" | "down";
  uptime: number; // percentage
  responseTime: number; // average in ms
  requests: number; // total requests
  errors: number; // total errors (5xx)
  lastCheck: string; // ISO timestamp
  port: number;
  version?: string;
}

export interface ServiceHealth {
  name: string;
  status: "UP" | "DOWN" | "UNKNOWN";
  details?: {
    [key: string]: any;
  };
}

