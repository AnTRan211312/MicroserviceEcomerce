# 📋 PORT MAPPING - ECOMMERCE MICROSERVICES

## 🌐 Application Services
| Service | Port | Actuator Port | Description |
|---------|------|---------------|-------------|
| API Gateway | 8080 | 9090 | Main entry point |
| Auth Service | 8081 | 9091 | Authentication & Authorization |
| Product Service | 8082 | 9102 | Product management |
| Order Service | 8083 | 9103 | Order processing |
| Inventory Service | 8084 | 9094 | Inventory management |
| Cart Service | 8085 | 9095 | Shopping cart |
| Payment Service | 8086 | 9106 | Payment processing (VNPay) |
| Notification Service | 8087 | 9098 | Notification management |

## 🗄️ Databases
| Database | Port | Description |
|----------|------|-------------|
| Auth DB | 3307 | MySQL - Auth service database |
| Product DB | 3308 | MySQL - Product service database |
| Order DB | 3309 | MySQL - Order service database |
| Inventory DB | 3310 | MySQL - Inventory service database |
| Cart DB | 3311 | MySQL - Cart service database |
| Notification DB | 3312 | MySQL - Notification service database |
| Payment DB | 3313 | MySQL - Payment service database |

## 🔧 Infrastructure Services
| Service | Port | Actuator Port | Description |
|---------|------|---------------|-------------|
| Eureka Server | 8761 | 9099 | Service Discovery |
| Redis | 6379 | Cache & Session storage |
| Kafka | 9092 | Message broker (internal) |
| Zookeeper | 2181 | Kafka coordination |

## 📊 Monitoring & Observability
| Service | Port | Description |
|---------|------|-------------|
| Prometheus | 9097 | Metrics collection |
| Grafana | 3100 | Dashboards & visualization |

## 🚀 Reserved Ports (for future use)
| Port | Reserved For |
|------|--------------|
| 3000 | Frontend Application |

## 📝 Notes
- All services use internal Docker network for communication
- External ports are mapped for host access
- Actuator ports are used for health checks and metrics
- Kafka is used for asynchronous communication between services
- Services: order-service, notification-service, payment-service use Kafka
