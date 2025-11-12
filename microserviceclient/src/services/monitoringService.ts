// Monitoring Service - Tích hợp với Prometheus và Spring Boot Actuator
// Prometheus API base URL - sử dụng proxy trong development
const PROMETHEUS_BASE_URL = import.meta.env.VITE_PROMETHEUS_URL || 
  (import.meta.env.DEV ? '/prometheus' : 'http://localhost:9090');
const PROMETHEUS_API = `${PROMETHEUS_BASE_URL}/api/v1`;

// Service configuration - mapping service names to their management ports
// Ports từ application.properties của mỗi service
const SERVICE_CONFIG: Record<string, { port: number; managementPort: number; version?: string }> = {
  'auth-service': { port: 8081, managementPort: 9091, version: '1.2.3' },
  'payment-service': { port: 8086, managementPort: 9096, version: '2.1.0' },
  'product-service': { port: 8082, managementPort: 9102, version: '1.5.2' },
  'order-service': { port: 8083, managementPort: 9103, version: '1.8.1' },
  'inventory-service': { port: 8084, managementPort: 9094, version: '1.0.0' },
  'notification-service': { port: 8087, managementPort: 9098, version: '1.0.0' },
  'cart-service': { port: 8085, managementPort: 9095, version: '1.0.0' },
};

export const monitoringService = {
  /**
   * Query Prometheus API
   */
  async queryPrometheus(query: string): Promise<any> {
    try {
      // Prometheus API không cần authentication qua API Gateway
      const response = await fetch(`${PROMETHEUS_API}/query?query=${encodeURIComponent(query)}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(`Prometheus query failed: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error querying Prometheus:', error);
      throw error;
    }
  },

  /**
   * Get service health from Spring Boot Actuator
   */
  async getServiceHealth(serviceName: string): Promise<any> {
    const config = SERVICE_CONFIG[serviceName];
    if (!config) {
      throw new Error(`Unknown service: ${serviceName}`);
    }

    try {
      // Query through API Gateway or directly to management port
      const healthUrl = `http://localhost:${config.managementPort}/actuator/health`;
      const response = await fetch(healthUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        return { status: 'DOWN' };
      }

      return await response.json();
    } catch (error) {
      console.error(`Error fetching health for ${serviceName}:`, error);
      return { status: 'DOWN' };
    }
  },

  /**
   * Get total requests for a service from Prometheus
   * Sử dụng label 'application' hoặc 'service' tùy theo cách Prometheus scrape
   */
  async getTotalRequests(serviceName: string, timeRange: string = '1h'): Promise<number> {
    try {
      // Thử query với label 'application' trước (Spring Boot Micrometer default)
      let query = `sum(increase(http_server_requests_seconds_count{application="${serviceName}"}[${timeRange}]))`;
      let result = await this.queryPrometheus(query);
      
      // Nếu không có kết quả, thử với label 'service' (từ Prometheus scrape config)
      if (!result?.data?.result?.[0]?.value) {
        query = `sum(increase(http_server_requests_seconds_count{service="${serviceName}"}[${timeRange}]))`;
        result = await this.queryPrometheus(query);
      }
      
      if (result?.data?.result?.[0]?.value) {
        return parseFloat(result.data.result[0].value[1]) || 0;
      }
      return 0;
    } catch (error) {
      console.error(`Error getting total requests for ${serviceName}:`, error);
      return 0;
    }
  },

  /**
   * Get error count (5xx) for a service
   */
  async getErrorCount(serviceName: string, timeRange: string = '1h'): Promise<number> {
    try {
      // Thử với label 'application' trước
      let query = `sum(increase(http_server_requests_seconds_count{application="${serviceName}",status=~"5.."}[${timeRange}]))`;
      let result = await this.queryPrometheus(query);
      
      // Nếu không có kết quả, thử với label 'service'
      if (!result?.data?.result?.[0]?.value) {
        query = `sum(increase(http_server_requests_seconds_count{service="${serviceName}",status=~"5.."}[${timeRange}]))`;
        result = await this.queryPrometheus(query);
      }
      
      if (result?.data?.result?.[0]?.value) {
        return parseFloat(result.data.result[0].value[1]) || 0;
      }
      return 0;
    } catch (error) {
      console.error(`Error getting error count for ${serviceName}:`, error);
      return 0;
    }
  },

  /**
   * Get average response time for a service
   */
  async getAverageResponseTime(serviceName: string, timeRange: string = '1h'): Promise<number> {
    try {
      // Thử với label 'application' trước
      let query = `avg(rate(http_server_requests_seconds_sum{application="${serviceName}"}[${timeRange}]) / rate(http_server_requests_seconds_count{application="${serviceName}"}[${timeRange}])) * 1000`;
      let result = await this.queryPrometheus(query);
      
      // Nếu không có kết quả, thử với label 'service'
      if (!result?.data?.result?.[0]?.value) {
        query = `avg(rate(http_server_requests_seconds_sum{service="${serviceName}"}[${timeRange}]) / rate(http_server_requests_seconds_count{service="${serviceName}"}[${timeRange}])) * 1000`;
        result = await this.queryPrometheus(query);
      }
      
      if (result?.data?.result?.[0]?.value) {
        return parseFloat(result.data.result[0].value[1]) || 0;
      }
      return 0;
    } catch (error) {
      console.error(`Error getting average response time for ${serviceName}:`, error);
      return 0;
    }
  },

  /**
   * Get uptime percentage for a service
   * Tính dựa trên tỷ lệ requests thành công hoặc process uptime
   */
  async getUptimePercentage(serviceName: string, timeRange: string = '24h'): Promise<number> {
    try {
      // Thử query process_uptime_seconds trước (chính xác hơn)
      let uptimeQuery = `process_uptime_seconds{application="${serviceName}"}`;
      let uptimeResult = await this.queryPrometheus(uptimeQuery);
      
      if (!uptimeResult?.data?.result?.[0]?.value) {
        uptimeQuery = `process_uptime_seconds{service="${serviceName}"}`;
        uptimeResult = await this.queryPrometheus(uptimeQuery);
      }

      // Nếu có process uptime, tính phần trăm dựa trên timeRange
      if (uptimeResult?.data?.result?.[0]?.value) {
        const uptimeSeconds = parseFloat(uptimeResult.data.result[0].value[1]);
        const rangeSeconds = this.parseTimeRange(timeRange);
        const percentage = Math.min((uptimeSeconds / rangeSeconds) * 100, 100);
        return Math.round(percentage * 10) / 10;
      }

      // Fallback: Calculate uptime based on successful requests vs total requests
      const totalQuery = `sum(increase(http_server_requests_seconds_count{application="${serviceName}"}[${timeRange}]))`;
      const successQuery = `sum(increase(http_server_requests_seconds_count{application="${serviceName}",status!~"5.."}[${timeRange}]))`;
      
      const [totalResult, successResult] = await Promise.all([
        this.queryPrometheus(totalQuery).catch(() => ({ data: { result: [] } })),
        this.queryPrometheus(successQuery).catch(() => ({ data: { result: [] } })),
      ]);

      const total = totalResult?.data?.result?.[0]?.value 
        ? parseFloat(totalResult.data.result[0].value[1]) 
        : 0;
      const success = successResult?.data?.result?.[0]?.value 
        ? parseFloat(successResult.data.result[0].value[1]) 
        : 0;

      if (total === 0) {
        // Check if service is running via health endpoint
        const health = await this.getServiceHealth(serviceName);
        return health.status === 'UP' ? 100 : 0;
      }

      return Math.round((success / total) * 100 * 10) / 10;
    } catch (error) {
      console.error(`Error getting uptime for ${serviceName}:`, error);
      // Fallback to health check
      const health = await this.getServiceHealth(serviceName);
      return health.status === 'UP' ? 100 : 0;
    }
  },

  /**
   * Parse time range string to seconds
   */
  parseTimeRange(timeRange: string): number {
    const match = timeRange.match(/(\d+)([smhd])/);
    if (!match) return 3600; // default 1 hour
    
    const value = parseInt(match[1]);
    const unit = match[2];
    
    switch (unit) {
      case 's': return value;
      case 'm': return value * 60;
      case 'h': return value * 3600;
      case 'd': return value * 86400;
      default: return 3600;
    }
  },

  /**
   * Get all service metrics
   */
  async getAllServiceMetrics(): Promise<any[]> {
    const serviceNames = Object.keys(SERVICE_CONFIG);
    
    const metricsPromises = serviceNames.map(async (serviceName) => {
      const config = SERVICE_CONFIG[serviceName];
      
      try {
        const [health, requests, errors, responseTime, uptime] = await Promise.all([
          this.getServiceHealth(serviceName),
          this.getTotalRequests(serviceName),
          this.getErrorCount(serviceName),
          this.getAverageResponseTime(serviceName),
          this.getUptimePercentage(serviceName),
        ]);

        // Determine status based on health, uptime, error rate, and response time
        let status: "healthy" | "degraded" | "down" = "down";
        if (health.status === 'UP') {
          const errorRate = requests > 0 ? (errors / requests) * 100 : 0;
          if (uptime >= 99 && errorRate < 1 && responseTime < 200) {
            status = "healthy";
          } else if (uptime >= 95 && errorRate < 5) {
            status = "degraded";
          } else {
            status = "down";
          }
        } else {
          status = "down";
        }

        return {
          id: serviceName,
          name: this.formatServiceName(serviceName),
          status,
          uptime: Math.round(uptime * 10) / 10,
          responseTime: Math.round(responseTime),
          requests: Math.round(requests),
          errors: Math.round(errors),
          lastCheck: new Date().toISOString(),
          port: config.port,
          version: config.version,
        };
      } catch (error) {
        console.error(`Error fetching metrics for ${serviceName}:`, error);
        return {
          id: serviceName,
          name: this.formatServiceName(serviceName),
          status: "down" as const,
          uptime: 0,
          responseTime: 0,
          requests: 0,
          errors: 0,
          lastCheck: new Date().toISOString(),
          port: config.port,
          version: config.version,
        };
      }
    });

    return Promise.all(metricsPromises);
  },

  /**
   * Format service name for display
   */
  formatServiceName(serviceName: string): string {
    return serviceName
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  },
};

