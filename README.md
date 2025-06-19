# Order Management Service

Sistema de gerenciamento de pedidos desenvolvido em Spring Boot com PostgreSQL.

![image](https://github.com/user-attachments/assets/d55b7fec-9e60-4266-a60a-d16024914362)

## 📋 Pré-requisitos

- [Docker](https://www.docker.com/get-started) (versão 20.10 ou superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2.0 ou superior)
- [Git](https://git-scm.com/) (para clonar o repositório)

> **Nota**: Não é necessário ter Java ou Maven instalados localmente, pois tudo será executado via Docker.

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

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **API Principal** | http://localhost:8080 | Aplicação Spring Boot |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Documentação da API |
| **Health Check** | http://localhost:8080/actuator/health | Status da aplicação |
| **PgAdmin** | http://localhost:8081 | Interface para gerenciar PostgreSQL |
| **PostgreSQL** | localhost:5432 | Banco de dados |

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

## 🏗️ Estrutura do Projeto

```
order-management/
├── src/                          # Código fonte da aplicação
├── docker-compose.yml            # Orquestração principal
├── docker-compose-simple.yml     # Versão alternativa
├── Dockerfile                    # Build da aplicação
├── Dockerfile.simple             # Build alternativo
├── .dockerignore                 # Arquivos ignorados no build
├── application-docker.properties # Configurações para Docker
└── README.md                     # Este arquivo
```

## 🔧 Configurações da Aplicação

### Variáveis de Ambiente

O Docker Compose já configura automaticamente as seguintes variáveis:

| Variável | Valor | Descrição |
|----------|-------|-----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/order_management` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `admin` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `admin123` | Senha do banco |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Modo de criação das tabelas |

### Pool de Conexões

- **Máximo de conexões**: 20
- **Mínimo idle**: 5
- **Timeout de conexão**: 20 segundos

## 🐛 Solução de Problemas

### Problema: Porta já em uso
```bash
# Verificar processos usando as portas
lsof -i :8080
lsof -i :5432
lsof -i :8081

# Parar containers conflitantes
docker-compose down
```

### Problema: Erro de permissão
```bash
# Dar permissão para o Maven Wrapper
chmod +x mvnw
```

### Problema: Aplicação não conecta no banco
```bash
# Verificar se o PostgreSQL está saudável
docker-compose ps

# Ver logs do PostgreSQL
docker-compose logs postgres

# Reiniciar apenas o PostgreSQL
docker-compose restart postgres
```

### Problema: Compilação falha
Se você não tem os arquivos `mvnw` ou `.mvn`, use a versão simplificada:

```bash
# 1. Compile localmente primeiro
mvn clean package -DskipTests

# 2. Use a versão simplificada
docker-compose -f docker-compose-simple.yml up --build
```

## 📊 Monitoramento

### Health Checks
A aplicação possui health checks configurados:

```bash
# Verificar saúde da aplicação
curl http://localhost:8080/actuator/health

# Verificar métricas
curl http://localhost:8080/actuator/metrics
```

### Logs
Os logs são configurados com nível INFO para a aplicação:

```bash
# Ver logs específicos
docker-compose logs --tail=100 order-management-service
```

## 🔄 Desenvolvimento

### Rebuild após Mudanças no Código
```bash
# Rebuild completo
docker-compose up --build

# Rebuild apenas da aplicação
docker-compose build order-management-service
docker-compose up order-management-service
```

### Debug Mode
Para executar em modo debug, adicione ao `docker-compose.yml`:

```yaml
environment:
  - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
ports:
  - "5005:5005"  # Porta de debug
```

## 🧪 Teste da API

### Exemplos de Teste
```bash
# Testar health check
curl -X GET http://localhost:8080/actuator/health

# Testar endpoint da API (substitua pelos seus endpoints)
curl -X GET http://localhost:8080/api/orders

# Testar com dados (exemplo)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"product": "Produto Teste", "quantity": 1}'
```

## 📚 Recursos Adicionais

- **Swagger UI**: Interface interativa para testar a API
- **Actuator**: Endpoints de monitoramento e métricas
- **PgAdmin**: Interface web para gerenciar o PostgreSQL
- **Health Checks**: Verificação automática da saúde dos serviços

## 🆘 Suporte

Se encontrar problemas:

1. **Verifique os logs**: `docker-compose logs -f`
2. **Verifique o status**: `docker-compose ps`
3. **Reinicie os serviços**: `docker-compose restart`
4. **Limpe tudo e recomece**: `docker-compose down -v && docker-compose up --build`

---

## ⚡ Quick Start (TL;DR)

```bash
# Clone o repositório
git clone <seu-repositorio>
cd order-management

# Execute tudo
docker-compose up --build

# Acesse: http://localhost:8080/swagger-ui.html
```

**Pronto! Sua aplicação estará rodando em http://localhost:8080** 🎉


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

### **🔧 Camada de Infraestrutura**
- **Database**: PostgreSQL
- **Connection Pool**: HikariCP
- **Monitoring**: Spring Actuator
- **Logging**: SLF4J + Logback

## 🗄️ Arquitetura de Dados

### **Banco de Dados: PostgreSQL**
```sql
-- Estrutura sugerida baseada no domínio
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   customers  │    │    orders    │    │ order_items  │
├──────────────┤    ├──────────────┤    ├──────────────┤
│ id (PK)      │    │ id (PK)      │    │ id (PK)      │
│ name         │◄───┤ customer_id  │    │ order_id (FK)│
│ email        │    │ order_date   │◄───┤ product_id   │
│ created_at   │    │ status       │    │ quantity     │
└──────────────┘    │ total_amount │    │ unit_price   │
                    └──────────────┘    └──────────────┘
```

### **Pool de Conexões (HikariCP)**
- **Máximo**: 20 conexões
- **Mínimo Idle**: 5 conexões
- **Otimizado** para alta performance

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

## 📊 Arquitetura de Monitoramento

### **Spring Boot Actuator**
```
Endpoints Expostos:
├── /actuator/health     ← Status da aplicação
├── /actuator/info       ← Informações da aplicação  
└── /actuator/metrics    ← Métricas de performance
```

### **Health Checks**
- **Application**: Verifica se a API responde
- **Database**: Verifica conectividade PostgreSQL
- **Disk Space**: Monitora espaço em disco
- **Custom**: Checks específicos do domínio

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

### **Testes (Recomendado)**
```
├── Unit Tests        ← Services, Repositories
├── Integration Tests ← API Endpoints
├── Contract Tests    ← External APIs
└── E2E Tests        ← User Scenarios
```

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

## 🎯 Benefícios da Arquitetura Atual

✅ **Simplicidade**: Fácil desenvolvimento e deploy
✅ **Performance**: Comunicação in-process
✅ **Consistência**: Transações ACID
✅ **Debugging**: Contexto unificado
✅ **Deployment**: Single artifact
✅ **Monitoring**: Centralizado

## ⚡ Próximos Passos Recomendados

1. **Implementar testes automatizados**
2. **Adicionar cache (Redis)**
3. **Configurar CI/CD pipeline**
4. **Implementar observabilidade (metrics, traces)**
5. **Adicionar autenticação/autorização**
6. **Configurar backup automático**
