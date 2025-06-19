# Estágio de construção (build)
FROM gradle:8.4-jdk17 AS build

WORKDIR /app

# Copia os arquivos do projeto (incluindo o gradlew)
COPY . .

# Torna o gradlew executável (caso não esteja)
RUN chmod +x gradlew

# Executa o build do Gradle
RUN ./gradlew build --no-daemon -x test

# Estágio de execução
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instalar curl para health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copia o JAR do estágio de construção
COPY --from=build /app/build/libs/*.jar app.jar

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown appuser:appuser app.jar
USER appuser

# Porta que a aplicação Spring Boot usa
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]