# MedBridge-Backend-MicroServices

Concise reference for the code and artifacts that exist in this repository.

## Repository layout (modules at repo root)
- `eureka/` — Eureka server (service discovery)  
- `api-gateway/` — Spring Cloud Gateway (WebFlux)  
  - `src/main/java/.../config/WebClientConfig.java` (exists)  
- `ABHA-Authentication/` — ABHA / authentication microservice  
- `spring-auth/` — JWT auth microservice  
- `terminology-service/` — terminology microservice (FHIR-related)  
- `fhir-service/` — FHIR R4 microservice  
- `docker-compose.yml` — brings up local infra containers  
- `monitoring/` — may contain monitoring configs used by docker-compose

## Runtime infra (defined in docker-compose.yml)
- `redis` (image: `redis:7-alpine`) — mapped to host `6379`  
- `zipkin` (image: `openzipkin/zipkin`) — mapped to host `9411`

## Service default ports (as configured in project)
- Eureka server: `8761`  
- API Gateway: `8080`  
- ABHA-Authentication: `8081`  
- terminology-service: `8082`  
- fhir-service: `8083`  
- spring-auth: `8084`  
- Redis (docker): `6379`  
- Zipkin (docker): `9411`

## How to run locally
1. From repo root, start infra containers declared in `docker-compose.yml`:
   ```bash
   cd /home/suvrat/projects/MedBridge-Backend-MicroServices
   docker-compose up -d
   ```
   This starts Redis and Zipkin (and any other services listed in `docker-compose.yml`).

2. Start the Java services (each in its module directory). Example order:
   ```bash
   cd eureka && ./mvnw spring-boot:run
   cd ../terminology-service && ./mvnw spring-boot:run
   cd ../fhir-service && ./mvnw spring-boot:run
   cd ../ABHA-Authentication && ./mvnw spring-boot:run
   cd ../spring-auth && ./mvnw spring-boot:run
   cd ../api-gateway && ./mvnw spring-boot:run
   ```

3. Verify UIs / endpoints:
- Eureka dashboard: `http://localhost:8761`  
- API Gateway: `http://localhost:8080`  
- Zipkin UI: `http://localhost:9411`  
- Terminology service: `http://localhost:8082`  
- Actuator endpoints (if enabled per service): `/actuator`

## Notable existing files
- `api-gateway/src/main/java/com/example/api_gateway/config/WebClientConfig.java` — provides LoadBalanced `WebClient.Builder` used by gateway.  
- `docker-compose.yml` — defines Redis and Zipkin containers (and any other infra present).  
- Module-level `pom.xml` and `src/main/resources/application*.yml` files — service ports, datasources, tracing and actuator settings.

## Observability
- Zipkin is present in `docker-compose.yml` and reachable at `http://localhost:9411`.  
- Actuator dependencies appear in service poms; actuator endpoints may be enabled per service.

## How to inspect tracing / actuator references
Run these commands from the repo root to locate Zipkin/tracing and actuator references:
```bash
grep -Rni --exclude-dir=target --exclude-dir=.git "zipkin" .
grep -Rni --exclude-dir=target --exclude-dir=.git "actuator" .
```

## Maintenance notes
- This README documents only items present in the repository as-is.  
- To inspect per-service config, open `src/main/resources/application*.yml` in each module.  
- Use the module `pom.xml` files to verify dependencies and actuator/tracing integrations.
