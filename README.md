# Order Management Service

Sistema de gerenciamento de pedidos desenvolvido em Spring Boot com PostgreSQL.

![image](https://github.com/user-attachments/assets/d55b7fec-9e60-4266-a60a-d16024914362)

## 📋 Pré-requisitos

- [Docker](https://www.docker.com/get-started) (versão 20.10 ou superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2.0 ou superior)
- [Git](https://git-scm.com/) (para clonar o repositório)

> **Nota**: Não é necessário ter Java ou Maven instalados localmente, pois tudo será executado via Docker.

## ⚡ Quick Start (TL;DR)

```bash
# Clone o repositório
git clone git@github.com:velrino/order-management.git
cd order-management

# Execute tudo
docker-compose up --build

# Acesse: http://localhost:8080/swagger-ui.html
```

**Pronto! Sua aplicação estará rodando em http://localhost:8080** 🎉

## 🚀 Execução Rápida

Para executar o projeto completo com **um único comando**:

```bash
docker-compose up --build
```

Aguarde alguns minutos para que:

1. O PostgreSQL seja inicializado
2. A aplicação seja compilada
3. Os serviços sejam iniciados

## 🔗 Acesso aos Serviços

Após a execução, os seguintes serviços estarão disponíveis:

| Serviço           | URL                                   | Descrição                           |
| ----------------- | ------------------------------------- | ----------------------------------- |
| **API Principal** | http://localhost:8080                 | Aplicação Spring Boot               |
| **Swagger UI**    | http://localhost:8080/swagger-ui.html | Documentação da API                 |
| **Health Check**  | http://localhost:8080/actuator/health | Status da aplicação                 |
| **PgAdmin**       | http://localhost:8081                 | Interface para gerenciar PostgreSQL |
| **PostgreSQL**    | localhost:5432                        | Banco de dados                      |

### 📊 Credenciais de Acesso

**PostgreSQL:**

- Host: `localhost`
- Porta: `5432`
- Database: `order_management`
- Usuário: `admin`
- Senha: `admin123`

**PgAdmin:**

- Email: `admin@admin.com`
- Senha: `admin123`

## 🛠️ Comandos Úteis

### Executar em Background

```bash
docker-compose up --build -d
```

### Ver Logs em Tempo Real

```bash
# Todos os serviços
docker-compose logs -f

# Apenas a aplicação
docker-compose logs -f order-management-service

# Apenas o PostgreSQL
docker-compose logs -f postgres
```

### Verificar Status dos Serviços

```bash
docker-compose ps
```

### Parar os Serviços

```bash
docker-compose down
```

### Parar e Remover Dados do Banco

```bash
docker-compose down -v
```

### Rebuild Apenas da Aplicação

```bash
docker-compose up --build order-management-service
```

### Acessar Container da Aplicação

```bash
docker-compose exec order-management-service sh
```

### Acessar PostgreSQL via CLI

```bash
docker-compose exec postgres psql -U admin -d order_management
```

## 🔧 Configurações da Aplicação

### Variáveis de Ambiente

O Docker Compose já configura automaticamente as seguintes variáveis:

| Variável                        | Valor                                              | Descrição                   |
| ------------------------------- | -------------------------------------------------- | --------------------------- |
| `SPRING_DATASOURCE_URL`         | `jdbc:postgresql://postgres:5432/order_management` | URL do banco                |
| `SPRING_DATASOURCE_USERNAME`    | `admin`                                            | Usuário do banco            |
| `SPRING_DATASOURCE_PASSWORD`    | `admin123`                                         | Senha do banco              |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update`                                           | Modo de criação das tabelas |

### Pool de Conexões

- **Máximo de conexões**: 20
- **Mínimo idle**: 5
- **Timeout de conexão**: 20 segundos

## 📊 Monitoramento

### Health Checks

A aplicação possui health checks configurados:

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health

# Verificar métricas
curl http://localhost:8080/actuator/metrics
```

## 📚 Recursos Adicionais

- **Swagger UI**: Interface interativa para testar a API
- **Actuator**: Endpoints de monitoramento e métricas
- **PgAdmin**: Interface web para gerenciar o PostgreSQL
- **Health Checks**: Verificação automática da saúde dos serviços

# 🏗️ Arquitetura do Order Management Service

## 📋 Visão Geral

O **Order Management Service** é uma aplicação **Spring Boot** que segue uma arquitetura **monolítica modular** com características de **microserviço**, preparada para escalar e ser distribuída.

## 🎯 Padrões Arquiteturais Utilizados

### 1. **Arquitetura em Camadas (Layered Architecture)**

```
┌─────────────────────────────────────┐
│           Presentation Layer        │  ← Controllers, DTOs, APIs REST
├─────────────────────────────────────┤
│            Business Layer           │  ← Services, Business Logic
├─────────────────────────────────────┤
│           Persistence Layer         │  ← Repositories, Entities, JPA
├─────────────────────────────────────┤
│           Infrastructure Layer      │  ← Database, External APIs
└─────────────────────────────────────┘
```

## 🏛️ Componentes da Arquitetura

### **🌐 Camada de Apresentação**

- **Controllers REST**: Endpoints para APIs
- **DTOs**: Objetos de transferência de dados
- **Validation**: Validação de entrada
- **Exception Handlers**: Tratamento de erros
- **Swagger/OpenAPI**: Documentação automática

### **⚙️ Camada de Negócio**

- **Services**: Lógica de negócio
- **Business Rules**: Regras de domínio
- **Use Cases**: Casos de uso específicos
- **Domain Events**: Eventos de domínio

### **💾 Camada de Persistência**

- **Repositories**: Abstração de acesso a dados
- **Entities**: Mapeamento objeto-relacional
- **JPA/Hibernate**: ORM para PostgreSQL
- **Migrations**: Controle de versão do banco

## 🚀 Arquitetura de Deploy

### **Containerização (Docker)**

```
┌─────────────────────────────────────────────────────┐
│                    Docker Host                      │
│  ┌─────────────────┐  ┌─────────────────┐          │
│  │  Spring Boot    │  │   PostgreSQL    │          │
│  │  Application    │  │   Database      │          │
│  │  (Port 8080)    │  │  (Port 5432)    │          │
│  └─────────────────┘  └─────────────────┘          │
│  ┌─────────────────┐                               │
│  │    PgAdmin      │                               │
│  │   (Port 8081)   │                               │
│  └─────────────────┘                               │
└─────────────────────────────────────────────────────┘
```

### **Rede e Comunicação**

- **Bridge Network**: Comunicação entre containers
- **Service Discovery**: DNS interno do Docker
- **Health Checks**: Monitoramento automático

## 🔐 Arquitetura de Segurança

### **Configurações Atuais**

- **Container Security**: Usuário não-root
- **Database**: Credenciais configuráveis
- **Network**: Rede isolada entre containers

### **Recomendações para Produção**

```
┌─────────────────────────────────────┐
│             Load Balancer           │
├─────────────────────────────────────┤
│               API Gateway           │  ← Rate Limiting, Auth
├─────────────────────────────────────┤
│            Spring Security          │  ← JWT, OAuth2
├─────────────────────────────────────┤
│           Order Service             │
├─────────────────────────────────────┤
│            Database                 │  ← SSL, Encryption
└─────────────────────────────────────┘
```

## 📈 Escalabilidade

### **Horizontal Scaling**

- **Stateless**: Aplicação sem estado
- **Database Connection Pool**: Suporte a múltiplas instâncias
- **Load Balancer Ready**: Preparado para distribuição

### **Vertical Scaling**

- **JVM Tuning**: Configurações otimizadas
- **Connection Pool**: Ajustável conforme carga
- **Resource Limits**: Configuráveis via Docker

### **Variáveis de Ambiente**

- **Database**: URL, credenciais dinâmicas
- **JVM**: Memory, GC settings
- **Logging**: Níveis configuráveis
- **Pool**: Tamanhos ajustáveis

## 🚦 Quality Gates

### **Código**

- **Clean Architecture**: Separação de responsabilidades
- **SOLID Principles**: Design orientado a objetos
- **DRY**: Reutilização de código

## 🔮 Evolução Arquitetural

### **Roadmap Sugerido**

1. **Phase 1**: Monolito modular ✅ (Atual)
2. **Phase 2**: Event-driven architecture
3. **Phase 3**: Microservices decomposition
4. **Phase 4**: CQRS + Event Sourcing

### **Preparação para Microservices**

- **Domain Boundaries**: Bem definidos
- **API First**: Contratos claros
- **Database per Service**: Preparado
- **Event-driven**: Comunicação assíncrona

## ⚡ Próximos Passos Recomendados

1. **Implementar testes automatizados**
2. **Adicionar cache (Redis)**
3. **Configurar CI/CD pipeline**
4. **Implementar observabilidade (metrics, traces)**
5. **Adicionar autenticação/autorização**
6. **Configurar backup automático**
