# Arquitetura System-To-Be - Sistema de Gestão de Biblioteca (LMS)

- [Arquitetura System-To-Be - Sistema de Gestão de Biblioteca (LMS)](#arquitetura-system-to-be---sistema-de-gestão-de-biblioteca-lms)
  - [1. Introdução](#1-introdução)
    - [1.1. Contexto](#11-contexto)
    - [1.2. Objetivo](#12-objetivo)
    - [1.3. Âmbito](#13-âmbito)
  - [2. Análise Comparativa: System-As-Is vs System-To-Be](#2-análise-comparativa-system-as-is-vs-system-to-be)
    - [2.1. System-As-Is (Monolítico Modular)](#21-system-as-is-monolítico-modular)
    - [2.2. System-To-Be (Micro-serviços)](#22-system-to-be-micro-serviços)
  - [3. Arquitetura de Micro-serviços](#3-arquitetura-de-micro-serviços)
    - [3.1. Visão Geral](#31-visão-geral)
    - [3.2. Descrição dos Micro-serviços](#32-descrição-dos-micro-serviços)
      - [3.2.1. lms-authnusers](#321-lms-authnusers)
        - [3.2.1.1. Responsabilidades](#3211-responsabilidades)
        - [3.2.1.2. Tecnologias](#3212-tecnologias)
        - [3.2.1.3. Endpoints Principais](#3213-endpoints-principais)
      - [3.2.2. lms-books](#322-lms-books)
        - [3.2.2.1. Responsabilidades](#3221-responsabilidades)
        - [3.2.2.2. Tecnologias](#3222-tecnologias)
        - [3.2.2.3. Eventos Publicados](#3223-eventos-publicados)
        - [3.2.2.4. Endpoints Principais](#3224-endpoints-principais)
      - [3.2.3. lms-genres](#323-lms-genres)
        - [3.2.3.1. Responsabilidades](#3231-responsabilidades)
        - [3.2.3.2. Tecnologias](#3232-tecnologias)
        - [3.2.3.3. Eventos Publicados](#3233-eventos-publicados)
        - [3.2.3.4. Endpoints Principais](#3234-endpoints-principais)
      - [3.2.4. lms-readers](#324-lms-readers)
        - [3.2.4.1. Responsabilidades](#3241-responsabilidades)
        - [3.2.4.2. Tecnologias](#3242-tecnologias)
        - [3.2.4.3. Eventos Publicados](#3243-eventos-publicados)
        - [3.2.4.4. Endpoints Principais](#3244-endpoints-principais)
      - [3.2.5. lms-lendings](#325-lms-lendings)
        - [3.2.5.1. Responsabilidades](#3251-responsabilidades)
        - [3.2.5.2. Tecnologias](#3252-tecnologias)
        - [3.2.5.3. Eventos Publicados](#3253-eventos-publicados)
        - [3.2.5.4. Endpoints Principais](#3254-endpoints-principais)
    - [3.3. Padrões e Decisões Arquiteturais](#33-padrões-e-decisões-arquiteturais)
  - [4. Comunicação entre Micro-serviços](#4-comunicação-entre-micro-serviços)
    - [4.1. RabbitMQ - Message Broker](#41-rabbitmq---message-broker)
    - [4.2. Padrão de Comunicação](#42-padrão-de-comunicação)
      - [4.2.1. Direct Exchange Pattern](#421-direct-exchange-pattern)
      - [4.2.2. Event Types](#422-event-types)
    - [4.3. Consistência Eventual](#43-consistência-eventual)
  - [5. Persistência de Dados](#5-persistência-de-dados)
    - [5.1. Database per Service Pattern](#51-database-per-service-pattern)
      - [5.1.1. Vantagens](#511-vantagens)
      - [5.1.2. Desafios](#512-desafios)
  - [6. Segurança](#6-segurança)
    - [6.1. Autenticação (JWT)](#61-autenticação-jwt)
      - [6.1.1. Flow](#611-flow)
      - [6.1.2. Validação JWT Distribuída](#612-validação-jwt-distribuída)
    - [6.2. Autorização (RBAC)](#62-autorização-rbac)
  - [7. Qualidade e Testes](#7-qualidade-e-testes)
    - [7.1. Contract Testing (CDC)](#71-contract-testing-cdc)
      - [7.1.1. Exemplo (lms-books)](#711-exemplo-lms-books)
    - [7.2. Integration Tests](#72-integration-tests)
    - [7.3. Performance Testing](#73-performance-testing)
  - [8. Deployment e Orquestração](#8-deployment-e-orquestração)
    - [8.1. Docker Swarm](#81-docker-swarm)
  - [9. Próximos Passos e Melhorias](#9-próximos-passos-e-melhorias)
    - [9.1. API Gateway](#91-api-gateway)
    - [9.2. Service Discovery](#92-service-discovery)
    - [9.3. Distributed Tracing](#93-distributed-tracing)
    - [9.4. Circuit Breaker](#94-circuit-breaker)
    - [9.5. Event Sourcing](#95-event-sourcing)
  - [10. Conclusão](#10-conclusão)
  - [O sistema está preparado para crescimento e evolução contínua, mantendo os princípios de Domain-Driven Design e Event-Driven Architecture.](#o-sistema-está-preparado-para-crescimento-e-evolução-contínua-mantendo-os-princípios-de-domain-driven-design-e-event-driven-architecture)
  - [Referências Cruzadas](#referências-cruzadas)

## 1. Introdução

### 1.1. Contexto

Este documento descreve a arquitetura do sistema de gestão de biblioteca (Library Management System - LMS) numa abordagem de micro-serviços, que representa a evolução do sistema monolítico original (system-as-is) para uma arquitetura distribuída e escalável (system-to-be).

### 1.2. Objetivo

O objetivo principal desta transformação é decompor o sistema monolítico em micro-serviços independentes que comunicam através de messaging assíncrona (RabbitMQ), permitindo:

- **Escalabilidade independente** de cada componente
- **Isolamento de falhas** entre serviços (resiliência)
- **Desenvolvimento independente** por equipas diferentes
- **Flexibilidade tecnológica** para cada serviço
- **Deploy independente** de cada micro-serviço

### 1.3. Âmbito

O sistema suporta a gestão de uma biblioteca, incluindo:

- Gestão de livros e autores (books & authors)
- Gestão de leitores (readers)
- Gestão de empréstimos (lendings)
- Gestão de géneros literários (genres)
- Autenticação e autorização de utilizadores

---

## 2. Análise Comparativa: System-As-Is vs System-To-Be

### 2.1. System-As-Is (Monolítico Modular)

**Características:**

- Aplicação monolítica baseada em Spring Boot
- Base de dados única partilhada por todos os módulos
- Comunicação síncrona entre componentes
- Deploy único de toda a aplicação
- Módulos principais:
  - `authormanagement` - Gestão de autores
  - `bookmanagement` - Gestão de livros
  - `readermanagement` - Gestão de leitores
  - `lendingmanagement` - Gestão de empréstimos
  - `genremanagement` - Gestão de géneros
  - `usermanagement` - Gestão de utilizadores

**Limitações:**

- Escalabilidade limitada (escala toda a aplicação)
- Acoplamento forte entre módulos
- Alterações requerem deploy completo
- Falhas podem afetar todo o sistema

### 2.2. System-To-Be (Micro-serviços)

**Características:**

- Arquitetura de micro-serviços distribuídos
- Cada serviço com base de dados independente
- Comunicação assíncrona via RabbitMQ (event-driven)
- Deploy independente por serviço
- Container orchestration ready (Docker/Docker Swarm)

**Micro-serviços:**

1. **lms-authnusers** - Autenticação e gestão de utilizadores
2. **lms-books** - Gestão de livros e autores
3. **lms-genres** - Gestão de géneros literários
4. **lms-readers** - Gestão de leitores
5. **lms-lendings** - Gestão de empréstimos de livros

---

## 3. Arquitetura de Micro-serviços

### 3.1. Visão Geral

```plaintext
      ┌─────────────────────────────────────────────────────────────┐
      │                  API Gateway / Load Balancer                │
      └─────────────────────────────────────────────────────────────┘
                                      │
        ┌─────────────────────────────┼───────────────────────────┐
        │                             │                           │
┌───────▼────────┐          ┌─────────▼─────────┐        ┌────────▼────────┐
│ lms-authnusers │          │    lms-books      │        │   lms-readers   │
│  (Auth & User) │          │ (Books & Authors) │        │    (Readers)    │
└───────┬────────┘          └─────────┬─────────┘        └────────┬────────┘
        │                             │                           │
        └───────────────────┬─────────┴───────────┬───────────────┘
                            │                     │
                    ┌───────▼──────┐   ┌──────────▼────────┐
                    │  lms-genres  │   │    lms-lendings   │
                    │   (Genres)   │   │     (Lendings)    │
                    └───────┬──────┘   └──────────┬────────┘
                            │                     │
               ┌────────────┴─────────────────────┴───────────┐
               │                                              │
       ┌───────▼──────────────────────────────────────────────▼─────┐
       │                  RabbitMQ Message Broker                   │
       │                (Event-Driven Communication)                │
       └────────────────────────────────────────────────────────────┘
```

### 3.2. Descrição dos Micro-serviços

#### 3.2.1. lms-authnusers

##### 3.2.1.1. Responsabilidades

- Autenticação de utilizadores
- Gestão de credenciais e roles
- Autorização e controlo de acessos (RBAC)

##### 3.2.1.2. Tecnologias

- Spring Boot 3.2.5
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- RabbitMQ (AMQP) para consumo de eventos `USER_CREATED`

##### 3.2.1.3. Endpoints Principais

- `POST /api/login` - Autenticação
- `POST /api/register` - Registo de utilizadores
- `GET /api/users` - Listagem de utilizadores (admin)

#### 3.2.2. lms-books

##### 3.2.2.1. Responsabilidades

- Gestão do catálogo de livros
- Gestão de autores
- Gestão de géneros associados aos livros
- Publicação de eventos de alterações em livros

##### 3.2.2.2. Tecnologias

- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- RabbitMQ (AMQP) para publicar eventos `BOOK_*` e consumir géneros

##### 3.2.2.3. Eventos Publicados

- `BOOK_CREATED` - Livro criado → **Consumido por**: lms-genres, lms-lendings, lms-readers
- `BOOK_UPDATED` - Livro atualizado → **Consumido por**: lms-genres, lms-lendings, lms-readers
- `BOOK_DELETED` - Livro eliminado → **Consumido por**: lms-genres, lms-lendings, lms-readers

_Ver [Event-Driven-Architecture.md](Event-Driven-Architecture.md) para fluxos completos e detalhes dos consumers._

##### 3.2.2.4. Endpoints Principais

- `GET /api/books` - Listagem de livros
- `POST /api/books` - Criação de livro
- `PATCH /api/books/{isbn}` - Atualização de livro
- `GET /api/authors` - Listagem de autores

#### 3.2.3. lms-genres

##### 3.2.3.1. Responsabilidades

- Gestão de géneros literários
- Categorização e taxonomia
- Publicação de eventos de alterações em géneros

##### 3.2.3.2. Tecnologias

- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- RabbitMQ (AMQP) para consumir `BOOK_*` e publicar `GENRE_*`

##### 3.2.3.3. Eventos Publicados

- `GENRE_CREATED` - Género criado → **Consumido por**: lms-readers
- `GENRE_UPDATED` - Género atualizado → **Consumido por**: lms-readers
- `GENRE_DELETED` - Género eliminado → **Consumido por**: lms-readers

_Ver [Event-Driven-Architecture.md](Event-Driven-Architecture.md) para fluxos completos e detalhes dos consumers._

##### 3.2.3.4. Endpoints Principais

- `GET /api/genres` - Listagem de géneros
- `POST /api/genres` - Criação de género
- `PATCH /api/genres/{id}` - Atualização de género

#### 3.2.4. lms-readers

##### 3.2.4.1. Responsabilidades

- Gestão de leitores (readers)
- Perfis e informações dos leitores
- Histórico e estatísticas de leitores
- Publicação de eventos de alterações em leitores

##### 3.2.4.2. Tecnologias

- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- RabbitMQ (AMQP) para publicar `USER_CREATED` e consumir `GENRE_*`

##### 3.2.4.3. Eventos Publicados

- `READER_CREATED` - Leitor criado (não consumido por outros serviços)
- `READER_UPDATED` - Leitor atualizado (não consumido por outros serviços)
- `READER_DELETED` - Leitor removido (não consumido por outros serviços)
- `USER_CREATED` - Utilizador criado → **Consumido por**: lms-authnusers

_Ver [Event-Driven-Architecture.md](Event-Driven-Architecture.md) para fluxos completos._

##### 3.2.4.4. Endpoints Principais

- `GET /api/readers` - Listagem de leitores
- `POST /api/readers` - Criação de leitor
- `GET /api/readers/{id}` - Detalhes do leitor

#### 3.2.5. lms-lendings

##### 3.2.5.1. Responsabilidades

- Gestão de empréstimos de livros
- Controlo de prazos e devoluções
- Cálculo de multas por atraso
- Estatísticas de empréstimos
- Publicação de eventos de alterações em empréstimos

##### 3.2.5.2. Tecnologias

- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- RabbitMQ (AMQP) para consumir `BOOK_*` e publicar `LENDING_*`

##### 3.2.5.3. Eventos Publicados

- `LENDING_CREATED` - Empréstimo criado (não consumido por outros serviços)
- `LENDING_UPDATED` - Empréstimo atualizado (devolução) (não consumido por outros serviços)
- `LENDING_DELETED` - Empréstimo cancelado (não consumido por outros serviços)

_Ver [Event-Driven-Architecture.md](Event-Driven-Architecture.md) para arquitectura completa de eventos._

##### 3.2.5.4. Endpoints Principais

- `POST /api/lendings` - Criar empréstimo
- `PATCH /api/lendings/{id}/return` - Registar devolução
- `GET /api/lendings` - Listagem de empréstimos
- `GET /api/lendings/average` - Estatísticas

---

### 3.3. Padrões e Decisões Arquiteturais

**Implementado**

- **Strangler Fig**: migração faseada do monólito para micro-serviços; novos requisitos (RF1/RF2) já vivem apenas nos serviços Books/Readers/AuthnUsers.
- **Event-Driven + Message Broker (RabbitMQ)**: comunicação assíncrona entre serviços, evitando chamadas REST cross-service e permitindo escalabilidade independente.
- **Database-per-Service**: cada serviço com a sua base de dados; isolamento de falhas e esquemas.
- **Polyglot Persistence**: lms-genres usa **MySQL**, restantes serviços usam **PostgreSQL** (refletido nos diagramas e compose files).
- **Idempotência nos consumers**: verificação de existência antes de criar (ex.: AuthnUsers para `USER_CREATED`, Genres para géneros em `BOOK_CREATED`).

**Não implementado (e porquê)**

- **Outbox Pattern**: não implementado por falta de tempo; publicação continua direta no mesmo processo. Risco: em falha entre persistência e publish pode faltar evento; mitigação parcial com idempotência e possibilidade de replays manuais.
- **Saga / Orquestrador**: arquitetura preparada para evoluir, mas não implementada por falta de tempo. Fluxos atuais (RF1, RF2) usam consistência eventual via eventos; não há compensações automáticas.
- **CQRS completo**: arquitetura preparada (podemos separar comandos/queries e modelos de leitura), mas não implementada por falta de tempo. Mantivemos modelo único para reduzir complexidade.

**Estado futuro recomendado**

- Adicionar Outbox para garantir entrega exatamente uma vez em caso de falha entre DB e broker.
- Introduzir orquestração/compensações (Saga) se surgirem workflows mais longos (ex.: pagamento + lending).
- Evoluir para CQRS em serviços com leitura intensiva (Books) se houver requisitos de escala de leitura.

## 4. Comunicação entre Micro-serviços

### 4.1. RabbitMQ - Message Broker

O RabbitMQ é utilizado como message broker para comunicação assíncrona entre micro-serviços, seguindo o padrão **Event-Driven Architecture**.

### 4.2. Padrão de Comunicação

#### 4.2.1. Direct Exchange Pattern

Os micro-serviços utilizam **Direct Exchange** para routing de mensagens:

```plaintext
Publisher → Direct Exchange → Routing Key → Queue → Consumer
```

**Exemplo - Book Events:**

```java
// Publisher (lms-books)
template.convertAndSend(
    direct.getName(),           // Exchange: "book.exchange"
    BookEvents.BOOK_CREATED,    // Routing Key: "BOOK_CREATED"
    bookViewAMQPinString        // Message payload
);
```

#### 4.2.2. Event Types

**Book Events (lms-books):**

- `BOOK_CREATED` - Novo livro no catálogo
- `BOOK_UPDATED` - Livro atualizado
- `BOOK_DELETED` - Livro removido

**Lending Events (lms-lendings):**

- `LENDING_CREATED` - Novo empréstimo
- `LENDING_UPDATED` - Estado do empréstimo alterado
- `LENDING_DELETED` - Empréstimo cancelado

**Reader Events (lms-readers):**

- `READER_CREATED` - Novo leitor registado
- `READER_UPDATED` - Dados do leitor atualizados
- `READER_DELETED` - Leitor removido

**Genre Events (lms-genres):**

- `Genre.Created` - Novo género criado
- `Genre.Updated` - Género atualizado
- `Genre.Deleted` - Género removido

### 4.3. Consistência Eventual

A arquitetura adota o modelo de **Consistência Eventual** (Eventual Consistency):

- Alterações são propagadas assincronamente
- Cada micro-serviço mantém cache/réplica dos dados necessários
- Eventos garantem sincronização entre serviços
- Trade-off: Disponibilidade vs Consistência forte (CAP Theorem)

---

## 5. Persistência de Dados

### 5.1. Database per Service Pattern

Cada micro-serviço possui a sua base de dados PostgreSQL independente:

```plaintext
┌─────────────────┐     ┌─────────────────┐     ┌────────────────┐
│  lms-authnusers │     │    lms-books    │     │   lms-readers  │
│        ↓        │     │        ↓        │     │        ↓       │
│   PostgreSQL    │     │   PostgreSQL    │     │   PostgreSQL   │
│   (users_db)    │     │   (books_db)    │     │  (readers_db)  │
└─────────────────┘     └─────────────────┘     └────────────────┘

┌─────────────────┐     ┌─────────────────┐
│  lms-lendings   │     │   lms-genres    │
│        ↓        │     │        ↓        │
│   PostgreSQL    │     │   PostgreSQL    │
│  (lendings_db)  │     │   (genres_db)   │
└─────────────────┘     └─────────────────┘
```

#### 5.1.1. Vantagens

- Isolamento completo de dados
- Escalabilidade independente
- Flexibilidade tecnológica (cada serviço pode usar DB diferente)
- Falhas isoladas

#### 5.1.2. Desafios

- Joins distribuídos não são possíveis
- Transações distribuídas complexas
- Duplicação de dados necessária

Cada micro-serviço cria o seu schema/database independente.

## 6. Segurança

### 6.1. Autenticação (JWT)

O micro-serviço **lms-authnusers** é responsável por:

- Emissão de tokens JWT
- Validação de credenciais
- Gestão de sessões

#### 6.1.1. Flow

```plaintext
1. Cliente → POST /api/login (credentials)
2. lms-authnusers → Valida e emite JWT
3. Cliente → Requests com header: "Authorization: Bearer {token}"
4. Micro-serviços → Validam JWT localmente (verificação signature)
```

#### 6.1.2. Validação JWT Distribuída

Cada micro-serviço valida tokens JWT **independentemente**:

- **Assinatura**: JWT assinado com chave secreta (HS256) partilhada entre serviços via variável de ambiente `JWT_SECRET`
- **Claims**: Verificação de `sub` (username), `exp` (expiração), `roles` (autorização)
- **Stateless**: Sem necessidade de consultar base de dados para validação
- **Spring Security**: `JwtAuthenticationFilter` interceta requests e valida token antes de autorizar acesso

**Limitação atual**: Partilha de secret via env variables (não recomendado para produção; considerar Vault/AWS Secrets Manager).

### 6.2. Autorização (RBAC)

Roles suportadas:

- `LIBRARIAN` - Gestão completa do sistema
- `READER` - Acesso limitado (consulta e empréstimos)
- `ADMIN` - Administração do sistema

---

## 7. Qualidade e Testes

### 7.1. Contract Testing (CDC)

Consumer-Driven Contracts implementados com **Pact** para garantir compatibilidade entre serviços:

**Localização dos testes**: `target/test-classes/CDC/` em cada microserviço (lms-books, lms-genres, lms-readers, lms-lendings)

**Padrão**:

- **Consumer tests**: Definem expectativas sobre API do provider
- **Provider tests**: Verificam se API cumpre contratos definidos pelos consumers
- **Pact files**: Contratos JSON gerados em `target/pacts/` após execução dos testes

**Documentação completa**: [NonFunctionalCompliance.md](NonFunctionalCompliance.md) documenta evidência de implementação CDC.

#### 7.1.1. Exemplo (lms-books)

```java
@Test
public void testBooksCDCConsumer() {
    // Valida contrato entre lms-books (consumer) e lms-lendings (provider)
    // Garante que alterações no provider não quebram consumer
}
```

### 7.2. Integration Tests

Testes de integração com RabbitMQ e PostgreSQL.

### 7.3. Performance Testing

**Load tests implementados** com k6 em `load/load-test.js`:

- **Cenários**: ramping-arrival-rate de 50→250 req/s ao longo de 8 minutos
- **Thresholds**: p95<500ms, p99<800ms, taxa de erro<1%
- **Cobertura**: Todos os módulos (books, authors, genres, readers, lendings)
- **Modos**: Suporta testes contra monolito (`http://localhost:8080`) e microserviços individuais

**Documentação**: [NonFunctionalCompliance.md](NonFunctionalCompliance.md) documenta requisitos de performance e evidências.

## 8. Deployment e Orquestração

### 8.1. Docker Swarm

Deployment em produção com **Docker Swarm** (orchestration nativa):

**Configuração**: `docker-compose-swarm.yml` em cada microserviço

**Características**:

- **Replicas**: 2 réplicas por serviço (alta disponibilidade)
- **Resource Limits**: CPU/memória limitados para parcimónia hardware:
  - Apps: 0.50 CPU / 512M limit, 0.25 CPU / 256M reservation
  - PostgreSQL: 1.00 CPU / 1G limit, 0.50 CPU / 512M reservation
  - RabbitMQ: 0.50 CPU / 512M limit
- **Health Checks**: Spring Boot Actuator (`/actuator/health`) integrado com Swarm healthcheck
- **Restart Policies**: `on-failure` para resiliência automática

**Documentação**: [NonFunctionalCompliance.md](NonFunctionalCompliance.md) documenta conformidade com requisitos não funcionais (disponibilidade, parcimónia hardware).

## 9. Próximos Passos e Melhorias

### 9.1. API Gateway

Implementar gateway centralizado (Spring Cloud Gateway / Kong) para:

- Routing unificado
- Rate limiting
- Authentication central

### 9.2. Service Discovery

Adicionar Eureka/Consul para service registry e discovery automático.

### 9.3. Distributed Tracing

Integrar Zipkin/Jaeger para tracing de pedidos distribuídos.

### 9.4. Circuit Breaker

Implementar Resilience4j para fault tolerance.

### 9.5. Event Sourcing

Considerar Event Sourcing para auditoria completa.

---

## 10. Conclusão

A migração de uma arquitetura monolítica para micro-serviços com comunicação assíncrona via RabbitMQ proporciona:

**Escalabilidade** - Cada serviço escala independentemente  
**Resiliência** - Falhas isoladas não afetam todo o sistema  
**Flexibilidade** - Deploy e desenvolvimento independentes  
**Performance** - Comunicação assíncrona não-bloqueante  
**Manutenibilidade** - Código organizado por bounded contexts

## O sistema está preparado para crescimento e evolução contínua, mantendo os princípios de Domain-Driven Design e Event-Driven Architecture.

## Referências Cruzadas

- **[Event-Driven-Architecture.md](Event-Driven-Architecture.md)**: Documentação completa dos 5 fluxos de eventos, publishers/consumers, configuração RabbitMQ
- **[NonFunctionalCompliance.md](NonFunctionalCompliance.md)**: Conformidade com requisitos não funcionais (disponibilidade, performance, parcimónia hardware, CDC, SOA), requisitos funcionais RF1/RF2
- **[README.md](README.md)**: Guia completo de setup, deployment, testes e troubleshooting
