# 🏦 Sistema de Comparación de Tipos de Cambio - 4 Microservicios

Un sistema completo de **4 microservicios Quarkus** que simula un ecosistema real de APIs de tipos de cambio.

## 🎯 Arquitectura del Sistema
```
┌─────────────────┐    HTTP Calls    ┌──────────────────┐
│                 │ ────────────────► │ Simple API :8081 │
│ Exchange Rate   │                   │ (JSON Simple)    │
│ Main Service    │    HTTP Calls    ┌──────────────────┐
│ :8080           │ ────────────────► │ XML API :8082    │
│                 │                   │ (XML Banking)    │
│ (Orquestador)   │    HTTP Calls    ┌──────────────────┐
│                 │ ────────────────► │ Advanced API     │
└─────────────────┘                   │ :8083 (Fintech)  │
                                      └──────────────────┘
```

## 🚀 Inicio Rápido

### 1. Construir todos los proyectos
```bash
# En cada directorio:
cd simple-exchange-api && ./mvnw clean package
cd ../xml-exchange-api && ./mvnw clean package
cd ../advanced-exchange-api && ./mvnw clean package
cd ../exchange-rate-main && ./mvnw clean package
```

### 2. Ejecutar con Docker Compose
```bash
# Desde el directorio raíz
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### 3. Probar el sistema
```bash
curl -X POST http://localhost:8080/api/v1/exchange/best-rate \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCurrency": "USD",
    "targetCurrency": "DOP",
    "amount": 100.00
  }'
```

## 📋 Estructura del Proyecto

| Módulo                   | Puerto | Descripción                        | Formato | Especialidad              |
|--------------------------|--------|------------------------------------|---------|---------------------------|
| `exchange-rate-main`     | 8080   | Servicio principal orquestador      | JSON    | Selección mejor tasa       |
| `simple-exchange-api`    | 8081   | Proveedor de tasas JSON simple      | JSON    | USD/EUR (0.83-0.85)       |
| `xml-exchange-api`       | 8082   | Proveedor de tasas con formato XML  | XML     | USD/MXN (17.5-18.2)       |
| `advanced-exchange-api`  | 8083   | Proveedor de tasas JSON avanzado    | JSON    | USD/DOP (58.2-59.8)       |

## ⚙️ Propiedades Configurables

### Servicio Principal (`exchange-rate-main/application.properties`)

```properties
# URLs de las APIs (pueden cambiarse sin problema)
quarkus.rest-client.simple-exchange-client.url=http://localhost:8081
quarkus.rest-client.xml-exchange-client.url=http://localhost:8082
quarkus.rest-client.advanced-exchange-client.url=http://localhost:8083

# Rutas específicas de cada API (en caso de que cambien en el futuro)
api.simple-exchange.path=/exchange
api.xml-exchange.path=/convert
api.advanced-exchange.path=/rate

# Credenciales de las APIs
api.simple.username=simple-api-user
api.simple.password=simple-api-password
api.xml.username=xml-api-user
api.xml.password=xml-api-password
api.advanced.username=advanced-api-user
api.advanced.password=advanced-api-password

# Configuración de tolerancia a fallos
mp.fault-tolerance.retry.max-retries=2
mp.fault-tolerance.retry.delay=1000
mp.fault-tolerance.circuit-breaker.request-volume-threshold=4
mp.fault-tolerance.circuit-breaker.failure-ratio=0.5
mp.fault-tolerance.circuit-breaker.delay=5000
mp.fault-tolerance.timeout.value=5000

# Configuración de Tasas de Cambio (en cada API provider)
# USD a EUR
exchange.rates.usd.eur.min=0.8300
exchange.rates.usd.eur.max=0.8500

# USD a MXN
exchange.rates.usd.mxn.min=17.5000
exchange.rates.usd.mxn.max=18.2000

# USD a DOP
exchange.rates.usd.dop.min=58.2000
exchange.rates.usd.dop.max=59.8000
```

## 🔐 Credenciales de APIs

Cada servicio API utiliza **autenticación básica**:

- **Simple API (8081)**  
  Usuario: `simple-api-user`  
  Clave: `simple-api-password`

- **XML API (8082)**  
  Usuario: `xml-api-user`  
  Clave: `xml-api-password`

- **Advanced API (8083)**  
  Usuario: `advanced-api-user`  
  Clave: `advanced-api-password`

## 🚀 Orden de Ejecución

1. Construir todos los proyectos
```bash
# En cada directorio:
cd simple-exchange-api && ./mvnw clean package
cd ../xml-exchange-api && ./mvnw clean package
cd ../advanced-exchange-api && ./mvnw clean package
cd ../exchange-rate-main && ./mvnw clean package
```

2. Ejecutar con Docker Compose
```bash
# Desde el directorio raíz
docker-compose up -d

# Ver logs
docker-compose logs -f
```

3. Probar el sistema
```bash
curl -X POST http://localhost:8080/api/v1/exchange/best-rate   -H "Content-Type: application/json"   -d '{
    "sourceCurrency": "USD",
    "targetCurrency": "DOP",
    "amount": 100.00
  }'
```

## 💰 Especialidades por Proveedor

- **Simple API (8081)**: Mejor para USD/EUR (0.83-0.85)  
- **XML API (8082)**: Mejor para USD/MXN (17.5-18.2)  
- **Advanced API (8083)**: Mejor para USD/DOP (58.2-59.8)  

El sistema automáticamente selecciona la mejor oferta entre los 3 proveedores.

## 🛠️ Desarrollo Individual

Cada microservicio puede ejecutarse independientemente:
```bash
# Simple API
cd simple-exchange-api
./mvnw quarkus:dev

# XML API  
cd xml-exchange-api
./mvnw quarkus:dev

# Advanced API
cd advanced-exchange-api
./mvnw quarkus:dev

# Main Service
cd exchange-rate-main
./mvnw quarkus:dev
```

## 📊 Monitoreo

- **Health Checks**: `/q/health` en cada servicio  
- **Swagger UI**: `/q/swagger-ui` en cada servicio  
- **Logs**: Diferenciados por colores para cada API  

-- **author**: Domingo J. Ruiz
