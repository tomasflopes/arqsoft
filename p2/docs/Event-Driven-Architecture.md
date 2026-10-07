# Event-Driven Architecture - LMS Microservices

- [Event-Driven Architecture - LMS Microservices](#event-driven-architecture---lms-microservices)
  - [Visão Geral](#visão-geral)
    - [Infraestrutura RabbitMQ](#infraestrutura-rabbitmq)
  - [Mapeamento Completo de Eventos](#mapeamento-completo-de-eventos)
    - [1. lms-genres (Genres Service)](#1-lms-genres-genres-service)
      - [Publisher](#publisher)
      - [Consumer](#consumer)
    - [2. lms-books (Books Service)](#2-lms-books-books-service)
      - [Publisher](#publisher-1)
      - [Consumer](#consumer-1)
    - [3. lms-readers (Readers Service)](#3-lms-readers-readers-service)
      - [Publisher](#publisher-2)
      - [Consumer](#consumer-2)
    - [4. lms-lendings (Lendings Service)](#4-lms-lendings-lendings-service)
      - [Publisher](#publisher-3)
      - [Consumer](#consumer-3)
    - [5. lms-authnusers (AuthN/Users Service)](#5-lms-authnusers-authnusers-service)
      - [Publisher](#publisher-4)
      - [Consumer](#consumer-4)
  - [Fluxos de Eventos Completos](#fluxos-de-eventos-completos)
    - [Fluxo 1: Criar Genre](#fluxo-1-criar-genre)
    - [Fluxo 2: Apagar Genre](#fluxo-2-apagar-genre)
    - [Fluxo 3: Criar Book](#fluxo-3-criar-book)
    - [Fluxo 4: Criar Book com Autores e Género (Feature Atómica)](#fluxo-4-criar-book-com-autores-e-género-feature-atómica)
    - [Fluxo 5: Criar Reader com User (Feature Atómica)](#fluxo-5-criar-reader-com-user-feature-atómica)
  - [Configuração RabbitMQ (RabbitmqClientConfig)](#configuração-rabbitmq-rabbitmqclientconfig)
    - [Componentes Necessários em Cada Serviço](#componentes-necessários-em-cada-serviço)
      - [1. **DirectExchange** - Define o canal de publicação](#1-directexchange---define-o-canal-de-publicação)
      - [2. **Queue** - Fila anônima para consumir eventos](#2-queue---fila-anônima-para-consumir-eventos)
      - [3. **Binding** - Liga Queue → Exchange via Routing Key](#3-binding---liga-queue--exchange-via-routing-key)
      - [4. **Controller** - Consome mensagens da fila](#4-controller---consome-mensagens-da-fila)
  - [Issues Resolvidos](#issues-resolvidos)
    - [Issue 1: Classe ReceiverConfig não reconhecida](#issue-1-classe-receiverconfig-não-reconhecida)
    - [Issue 2: Campo `_links` não reconhecido](#issue-2-campo-_links-não-reconhecido)
  - [Checklist de Implementação](#checklist-de-implementação)
    - [Implementado e funcional](#implementado-e-funcional)
    - [Parcialmente implementado](#parcialmente-implementado)
    - [Próximos passos](#próximos-passos)
  - [Architecture Decision Records (ADRs) - Technical Memos](#architecture-decision-records-adrs---technical-memos)
    - [ADR-001: Event-Driven Architecture com RabbitMQ](#adr-001-event-driven-architecture-com-rabbitmq)
      - [Alternativas Consideradas](#alternativas-consideradas)
        - [Alternativa 1: REST Síncrono (Rejeitada)](#alternativa-1-rest-síncrono-rejeitada)
        - [Alternativa 2: Message Broker (Kafka) (Rejeitada)](#alternativa-2-message-broker-kafka-rejeitada)
        - [Alternativa 3: RabbitMQ com DirectExchange (ACEITE)](#alternativa-3-rabbitmq-com-directexchange-aceite)
    - [ADR-002: Database-per-Service Pattern](#adr-002-database-per-service-pattern)
      - [Alternativas Consideradas](#alternativas-consideradas-1)
        - [Alternativa 1: Database Partilhada (Rejeitada)](#alternativa-1-database-partilhada-rejeitada)
        - [Alternativa 2: Database-per-Service (ACEITE)](#alternativa-2-database-per-service-aceite)
    - [ADR-003: DirectExchange vs Topic/Fanout Exchange](#adr-003-directexchange-vs-topicfanout-exchange)
      - [Alternativas Consideradas](#alternativas-consideradas-2)
        - [Alternativa 1: FanoutExchange (Rejeitada)](#alternativa-1-fanoutexchange-rejeitada)
        - [Alternativa 2: TopicExchange (Rejeitada)](#alternativa-2-topicexchange-rejeitada)
        - [Alternativa 3: DirectExchange (ACEITE)](#alternativa-3-directexchange-aceite)
    - [ADR-004: Consistência Eventual vs Transações Distribuídas](#adr-004-consistência-eventual-vs-transações-distribuídas)
      - [Alternativas Consideradas](#alternativas-consideradas-3)
        - [Alternativa 1: Two-Phase Commit (2PC) (Rejeitada)](#alternativa-1-two-phase-commit-2pc-rejeitada)
        - [Alternativa 2: Saga Pattern com Orquestração (Não implementado)](#alternativa-2-saga-pattern-com-orquestração-não-implementado)
        - [Alternativa 3: Consistência Eventual com Eventos (ACEITE)](#alternativa-3-consistência-eventual-com-eventos-aceite)
    - [ADR-005: Outbox Pattern - Não Implementado](#adr-005-outbox-pattern---não-implementado)
      - [Problema](#problema)
      - [Alternativas Consideradas](#alternativas-consideradas-4)
        - [Alternativa 1: Publicação Direta (IMPLEMENTADO - com risco)](#alternativa-1-publicação-direta-implementado---com-risco)
        - [Alternativa 2: Outbox Pattern com Polling (NÃO IMPLEMENTADO)](#alternativa-2-outbox-pattern-com-polling-não-implementado)
        - [Alternativa 3: Transaction Log Tailing (Debezium/CDC) (NÃO IMPLEMENTADO)](#alternativa-3-transaction-log-tailing-debeziumcdc-não-implementado)
    - [ADR-006: Polyglot Persistence - MySQL para Genres](#adr-006-polyglot-persistence---mysql-para-genres)
      - [Alternativas Consideradas](#alternativas-consideradas-5)
        - [Alternativa 1: PostgreSQL para Todos (Rejeitada)](#alternativa-1-postgresql-para-todos-rejeitada)
        - [Alternativa 2: MySQL para Genres (ACEITE)](#alternativa-2-mysql-para-genres-aceite)
  - [Referências](#referências)

## Visão Geral

O sistema LMS usa **RabbitMQ** com padrão **Publisher-Subscriber** através de **DirectExchange** para comunicação assíncrona entre micro-serviços.

### Infraestrutura RabbitMQ

- **Broker**: RabbitMQ 3-management
- **Padrão**: DirectExchange com routing keys
- **Queues**: AnonymousQueue (auto-delete) por consumer
- **Credenciais**: guest/guest
- **Portas**: 5672 (AMQP), 15672 (Management UI)

---

## Mapeamento Completo de Eventos

### 1. lms-genres (Genres Service)

#### Publisher

**Exchange**: `LMS.genres`  
**Routing Keys**: `Genre.Created`, `Genre.Updated`, `Genre.Deleted`

| Evento           | Routing Key     | Payload                    | Quando Publicado           |
| ---------------- | --------------- | -------------------------- | -------------------------- |
| Genre Criado     | `Genre.Created` | `{genre, version, _links}` | POST /api/genres           |
| Genre Atualizado | `Genre.Updated` | `{genre, version, _links}` | PUT /api/genres/{genre}    |
| Genre Apagado    | `Genre.Deleted` | `{genre, version, _links}` | DELETE /api/genres/{genre} |

**Implementação**: `GenreEventsRabbitmqPublisherImpl`

```java
@Service
public class GenreEventsRabbitmqPublisherImpl implements GenreEventsPublisher {
  // Publica para LMS.genres com routing keys Genre.Created/Updated/Deleted
}
```

**Invocado por**: `GenreServiceImpl.save()`, `update()`, `deleteByName()`

---

#### Consumer

**Exchange**: `LMS.books`  
**Routing Keys**: `Book.Created`, `Book.Updated`, `Book.Deleted`

| Evento Consumido | Queue                          | Ação                                         |
| ---------------- | ------------------------------ | -------------------------------------------- |
| Book Criado      | `autoDeleteQueue_Book_Created` | Persiste Book localmente                     |
| Book Atualizado  | `autoDeleteQueue_Book_Updated` | Atualiza Book local                          |
| Book Apagado     | `autoDeleteQueue_Book_Deleted` | Remove Book local via `bookService.delete()` |

**Implementação**: `BookRabbitmqController` (em lms-genres)

```java
@RabbitListener(queues = "#{autoDeleteQueue_Book_Created.name}")
public void receiveBookCreatedMsg(Message msg) {
    BookViewAMQP book = objectMapper.readValue(jsonReceived, BookViewAMQP.class);
    bookService.save(new Book(book)); // Cópia local
}
```

**Objetivo**: Manter cópia sincronizada de Books para estatísticas e análise de géneros

---

### 2. lms-books (Books Service)

#### Publisher

**Exchange**: `LMS.books`  
**Routing Keys**: `Book.Created`, `Book.Updated`, `Book.Deleted`

| Evento          | Routing Key    | Payload                                                 | Quando Publicado         |
| --------------- | -------------- | ------------------------------------------------------- | ------------------------ |
| Book Criado     | `Book.Created` | `{isbn, title, description, genre, authorIds, version}` | POST /api/books          |
| Book Atualizado | `Book.Updated` | `{isbn, title, description, genre, authorIds, version}` | PUT /api/books/{isbn}    |
| Book Apagado    | `Book.Deleted` | `{isbn, title, description, genre, authorIds, version}` | DELETE /api/books/{isbn} |

**Implementação**: `BookEventsRabbitmqPublisherImpl`

```java
@Service
public class BookEventsRabbitmqPublisherImpl implements BookEventsPublisher {
  // Publica para LMS.books com routing keys Book.Created/Updated/Deleted
}
```

**Invocado por**: `BookServiceImpl.create()`, `update()`, `delete()`

---

#### Consumer

**Exchange**: `LMS.genres`  
**Routing Keys**: `Genre.Created`, `Genre.Updated`, `Genre.Deleted`

| Evento Consumido | Queue                           | Ação                      |
| ---------------- | ------------------------------- | ------------------------- |
| Genre Criado     | `autoDeleteQueue_Genre_Created` | Persiste Genre localmente |
| Genre Atualizado | `autoDeleteQueue_Genre_Updated` | Atualiza Genre local      |
| Genre Apagado    | `autoDeleteQueue_Genre_Deleted` | Remove Genre local        |

**Implementação**: `BookRabbitmqController` (em lms-books)

```java
@RabbitListener(queues = "#{autoDeleteQueue_Genre_Created.name}")
public void receiveGenreCreated(Message msg) {
    GenreViewAMQP genre = objectMapper.readValue(jsonReceived, GenreViewAMQP.class);
    genreService.save(new Genre(genre.getGenre())); // Cópia local
}

@RabbitListener(queues = "#{autoDeleteQueue_Genre_Deleted.name}")
public void receiveGenreDeleted(Message msg) {
    genreService.findByString(genre).ifPresent(genreService::delete);
}
```

**Objetivo**: Manter cópia sincronizada de Genres para uso em Books

**Importante**: O `GenreViewAMQP` precisa de `@JsonIgnoreProperties(ignoreUnknown = true)` para ignorar o campo `_links` enviado pelo Genres.

---

### 3. lms-readers (Readers Service)

#### Publisher

**Exchange**: `LMS.readers`  
**Routing Keys**: `User.Created`, `User.Updated`, `User.Deleted`

| Evento          | Routing Key    | Payload                             |
| --------------- | -------------- | ----------------------------------- |
| User Criado     | `User.Created` | `{id, username, fullName, version}` |
| User Atualizado | `User.Updated` | `{id, username, fullName, version}` |
| User Apagado    | `User.Deleted` | `{id, username, fullName, version}` |

**Implementação**: `UserEventsRabbitmqPublisherImpl`, chamado por `ReaderServiceImpl.createWithUser()`

#### Consumer

**Exchange**: `LMS.readers` (próprio), `LMS.genres`

| Evento Consumido  | Exchange      | Queue                            | Ação                       |
| ----------------- | ------------- | -------------------------------- | -------------------------- |
| Reader Criado     | `LMS.readers` | `autoDeleteQueue_Reader_Created` | Persiste Reader localmente |
| Reader Atualizado | `LMS.readers` | `autoDeleteQueue_Reader_Updated` | Atualiza Reader local      |
| Reader Apagado    | `LMS.readers` | `autoDeleteQueue_Reader_Deleted` | Remove Reader local        |
| Genre Criado      | `LMS.genres`  | `autoDeleteQueue_Genre_Created`  | Persiste Genre localmente  |
| Genre Atualizado  | `LMS.genres`  | `autoDeleteQueue_Genre_Updated`  | Atualiza Genre local       |
| Genre Apagado     | `LMS.genres`  | `autoDeleteQueue_Genre_Deleted`  | Remove Genre local         |

**Implementação**: `ReaderRabbitmqController`, `GenreRabbitmqController`

---

### 4. lms-lendings (Lendings Service)

#### Publisher

**Exchange**: `LMS.lendings`  
**Routing Keys**: `Lending.Created`, `Lending.Updated`, `Lending.Deleted`

| Evento             | Routing Key       | Payload                                        |
| ------------------ | ----------------- | ---------------------------------------------- |
| Lending Criado     | `Lending.Created` | `{lendingNumber, bookIsbn, readerNumber, ...}` |
| Lending Atualizado | `Lending.Updated` | `{lendingNumber, bookIsbn, readerNumber, ...}` |
| Lending Apagado    | `Lending.Deleted` | `{lendingNumber, bookIsbn, readerNumber, ...}` |

**Implementação**: `LendingEventsRabbitmqPublisherImpl`

---

#### Consumer

**Exchange**: `LMS.lendings` (próprio), `LMS.books`, `LMS.readers`

| Evento Consumido   | Exchange       | Queue                             | Ação                        |
| ------------------ | -------------- | --------------------------------- | --------------------------- |
| Lending Criado     | `LMS.lendings` | `autoDeleteQueue_Lending_Created` | Persiste Lending localmente |
| Lending Atualizado | `LMS.lendings` | `autoDeleteQueue_Lending_Updated` | Atualiza Lending local      |
| Lending Apagado    | `LMS.lendings` | `autoDeleteQueue_Lending_Deleted` | Remove Lending local        |
| Book Criado        | `LMS.books`    | `autoDeleteQueue_Book_Created`    | Persiste Book localmente    |
| Book Atualizado    | `LMS.books`    | `autoDeleteQueue_Book_Updated`    | Atualiza Book local         |
| Book Apagado       | `LMS.books`    | `autoDeleteQueue_Book_Deleted`    | Remove Book local           |
| Reader Criado      | `LMS.readers`  | `autoDeleteQueue_Reader_Created`  | Persiste Reader localmente  |
| Reader Atualizado  | `LMS.readers`  | `autoDeleteQueue_Reader_Updated`  | Atualiza Reader local       |
| Reader Apagado     | `LMS.readers`  | `autoDeleteQueue_Reader_Deleted`  | Remove Reader local         |

**Implementação**: `LendingRabbitmqController`, `ReaderRabbitmqController`

---

### 5. lms-authnusers (AuthN/Users Service)

#### Publisher

Não publica eventos de utilizadores.

#### Consumer

**Exchange**: `LMS.readers`

| Evento Consumido | Queue                           | Ação                     | Estado |
| ---------------- | ------------------------------- | ------------------------ | ------ |
| User Criado      | `authnusers.queue.user-created` | Persiste User localmente | Ativo  |
| User Atualizado  | `authnusers.queue.user-updated` | Atualiza User local      | Ativo  |
| User Apagado     | `authnusers.queue.user-deleted` | Remove User local        | Ativo  |

**Implementação**: `UserRabbitmqController` chama `UserService.createFromAMQP`, `updateFromAMQP`, `deleteFromAMQP`

**Configuração RabbitMQ em AuthnUsers**:

```java
@Configuration
public class RabbitmqClientConfig {

    @Bean
    public DirectExchange lmsReadersExchange() {
        return new DirectExchange("LMS.readers");
    }

    @Bean
    public Queue autoDeleteQueue_User_Created() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding binding_User_Created(Queue autoDeleteQueue_User_Created,
                                        DirectExchange lmsReadersExchange) {
        return BindingBuilder.bind(autoDeleteQueue_User_Created)
                .to(lmsReadersExchange)
                .with(UserEvents.USER_CREATED); // "User.Created"
    }

    // Similar para User.Updated e User.Deleted
}
```

---

## Fluxos de Eventos Completos

### Fluxo 1: Criar Genre

```plaintext
1. POST /api/genres em lms-genres
   ↓
2. GenreServiceImpl.save() → GenreEventsPublisher.sendGenreCreated()
   ↓
3. RabbitMQ: LMS.genres exchange, routing key "Genre.Created"
   ↓
4. lms-books recebe via BookRabbitmqController.receiveGenreCreated()
   ↓
5. Books persiste Genre local: genreService.save(new Genre(genre))
   Género disponível em ambos os serviços
```

### Fluxo 2: Apagar Genre

```plaintext
1. DELETE /api/genres/{genre} em lms-genres
   ↓
2. GenreServiceImpl.deleteByName() → GenreEventsPublisher.sendGenreDeleted()
   ↓
3. RabbitMQ: LMS.genres exchange, routing key "Genre.Deleted"
   ↓
4. lms-books recebe via BookRabbitmqController.receiveGenreDeleted()
   ↓
5. Books remove Genre local: genreService.delete(genre)
   Género removido em ambos os serviços
```

### Fluxo 3: Criar Book

```plaintext
1. POST /api/books em lms-books
   ↓
2. BookServiceImpl.create() → BookEventsPublisher.sendBookCreated()
   ↓
3. RabbitMQ: LMS.books exchange, routing key "Book.Created"
   ↓
4. lms-genres recebe via BookRabbitmqController.receiveBookCreatedMsg()
   ↓
5. Genres apenas loga (não persiste Books)

   lms-lendings recebe via BookRabbitmqController (em lendings)
   ↓
6. Lendings sincroniza Book para uso em Lending
   Livro disponível para empréstimos
```

### Fluxo 4: Criar Book com Autores e Género (Feature Atómica)

```plaintext
1. POST /api/books/create-with-authors-and-genre em lms-books
   Request: {isbn, title, description, genre: "cyberpunk", authors: [{name, bio}]}
   ↓
2. BookServiceImpl.createWithAuthorsAndGenre():
   - Cria cada autor via AuthorService.create()
   - Verifica se género existe localmente (genreRepository.findByString("cyberpunk"))
   - Se não existe: cria Genre local com new Genre("cyberpunk")
   - Chama genreService.save() → GenreEventsPublisher.sendGenreCreated()
   - Cria Book com authorIds e género
   - BookEventsPublisher.sendBookCreated() com campo genre: "cyberpunk"
   ↓
3. RabbitMQ: LMS.books exchange, routing key "Book.Created"
   Payload: {isbn, title, description, genre: "cyberpunk", authorIds, version}
   ↓
4. lms-genres consome Book.Created via BookRabbitmqController.receiveBookCreated():
   - Extrai campo genre do payload
   - Verifica se género existe: genreService.findByString("cyberpunk")
   - Se não existe: cria Genre local com new Genre("cyberpunk")
   - Publica Genre.Created no exchange LMS.genres
   ↓
5. lms-readers consome Genre.Created e cria género local
   Resultado: Género "cyberpunk" disponível em Books, Genres e Readers
```

### Fluxo 5: Criar Reader com User (Feature Atómica)

```plaintext
1. POST /api/readers/create-with-user em lms-readers
   Request: {email: "carlos@example.com", fullName: "Carlos Mendes", password: "MyPass123"}
   ↓
2. ReaderServiceImpl.createWithUser():
   - Valida email único: readerRepository.findByUsername(email)
   - Valida password: mínimo 8 caracteres, maiúscula, minúscula, número/especial
   - Codifica password: passwordEncoder.encode(password) → BCrypt hash
   - Cria Reader: Reader.newReader(email, encodedPassword, fullName)
   - Guarda: readerRepository.save(reader) → gera reader number (2026/001)
   - Publica evento: userEventsPublisher.sendUserCreated(reader)
   ↓
3. RabbitMQ: LMS.readers exchange, routing key "User.Created"
   Payload: {id: 1, username: "carlos@example.com", fullName: "Carlos Mendes", version: 0}
   ↓
4. lms-authnusers consome User.Created via UserRabbitmqController.receiveUserCreatedMsg():
   - Deserializa UserViewAMQP do payload JSON
   - Chama userService.createFromAMQP(userViewAMQP)
   - Verifica duplicado: userRepo.findByUsername(username)
   - Se não existe: cria Reader local com password temporária "TempPassword123!"
     (password validada: 8+ chars, maiúscula, minúscula, número/especial)
   - Guarda: userRepo.save(user)
   - Log: "New user synchronized in AuthnUsers: carlos@example.com"
   ↓
5. Resultado: Utilizador criado em Readers (password real) e AuthnUsers (password temporária)
   - Readers DB: username=carlos@example.com, password=<BCrypt hash do MyPass123>
   - AuthnUsers DB: username=carlos@example.com, password=<BCrypt hash do TempPassword123!>
```

**Notas importantes:**

- Password temporária em AuthnUsers permite login imediato mas deve ser alterada
- Idempotência: se User.Created chegar duas vezes, findByUsername evita duplicação
- Validação: Password class valida regex ^(?=._[a-z])(?=._[A-Z])(?=.\*[0-9\\W]).{8,}$

---

## Configuração RabbitMQ (RabbitmqClientConfig)

### Componentes Necessários em Cada Serviço

#### 1. **DirectExchange** - Define o canal de publicação

```java
@Bean
public DirectExchange genreExchange() {
    return new DirectExchange("LMS.genres");
}
```

#### 2. **Queue** - Fila anônima para consumir eventos

```java
@Bean
public Queue autoDeleteQueue_Genre_Created() {
    return new AnonymousQueue(); // Auto-delete quando consumer desconecta
}
```

#### 3. **Binding** - Liga Queue → Exchange via Routing Key

```java
@Bean
public Binding binding4(@Qualifier("genreExchange") DirectExchange genreExchange,
                        Queue autoDeleteQueue_Genre_Created) {
    return BindingBuilder.bind(autoDeleteQueue_Genre_Created)
            .to(genreExchange)
            .with(GenreEvents.GENRE_CREATED.getEventName()); // "Genre.Created"
}
```

#### 4. **Controller** - Consome mensagens da fila

```java
@RabbitListener(queues = "#{autoDeleteQueue_Genre_Created.name}")
public void receiveGenreCreated(Message msg) {
    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
    GenreViewAMQP genre = objectMapper.readValue(json, GenreViewAMQP.class);
    // Processar evento
}
```

---

## Issues Resolvidos

### Issue 1: Classe ReceiverConfig não reconhecida

**Problema**: Beans de Queue/Binding dentro de classe estática sem `@Configuration`  
**Solução**: Mover todos os beans para classe principal ou adicionar `@Configuration`

### Issue 2: Campo `_links` não reconhecido

**Problema**: Genres adiciona `_links:{}` no JSON mas Books não espera  
**Solução**: Adicionar `@JsonIgnoreProperties(ignoreUnknown = true)` no `GenreViewAMQP`

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconhecidos
public class GenreViewAMQP {
    private String genre;
    private Long version;
}
```

---

## Checklist de Implementação

### Implementado e funcional

- [x] Genres publica Genre.Created/Updated/Deleted
- [x] Books publica Book.Created/Updated/Deleted
- [x] Books consome Genre events e sincroniza localmente
- [x] Genres consome Book events (read-only)
- [x] Lendings consome Book/Reader events
- [x] Readers publisher integrado
- [x] Lendings publisher integrado
- [x] Routing keys padronizadas (PascalCase)
- [x] JSON deserialization com @JsonIgnoreProperties

### Parcialmente implementado

- [ ] AuthnUsers events (não implementado)

### Próximos passos

1. Considerar adicionar eventos de AuthnUsers (User.Created/Updated)
2. Monitorizar RabbitMQ Management UI (`http://localhost:15672`) para debug
3. Adicionar Dead Letter Queues (DLQ) para mensagens falhadas

---

## Architecture Decision Records (ADRs) - Technical Memos

Esta secção documenta as decisões arquiteturais tomadas durante o design do sistema LMS, apresentando alternativas consideradas, prós/contras e justificação para cada escolha.

---

### ADR-001: Event-Driven Architecture com RabbitMQ

**Contexto**: Necessidade de comunicação assíncrona entre microserviços para desacoplamento e escalabilidade.

#### Alternativas Consideradas

##### Alternativa 1: REST Síncrono (Rejeitada)

**Descrição**: Comunicação direta HTTP/REST entre serviços.

**Prós**:

- Simples de implementar
- Resposta imediata
- Fácil debugging com logs HTTP

**Contras**:

- Acoplamento temporal: serviços devem estar disponíveis simultaneamente
- Cascata de falhas: se um serviço falha, toda a cadeia falha
- Escalabilidade limitada: cada request bloqueia thread
- Timeouts e retries complexos

**Diagrama de dependências REST**:

```plaintext
Books ──REST──> Genres ──REST──> Readers
  │                               ▲
  └─────────REST──────────────────┘
```

##### Alternativa 2: Message Broker (Kafka) (Rejeitada)

**Descrição**: Apache Kafka para streaming de eventos.

**Prós**:

- Alta throughput (milhões de eventos/seg)
- Persistência de eventos com log append-only
- Replay de eventos históricos

**Contras**:

- Overhead de infraestrutura (Zookeeper/Kafka)
- Complexidade de configuração (partitions, replication)
- Overkill para volumes moderados (<10k eventos/seg)
- Curva de aprendizagem elevada

##### Alternativa 3: RabbitMQ com DirectExchange (ACEITE)

**Descrição**: RabbitMQ com padrão DirectExchange e routing keys tipadas.

**Prós**:

- Desacoplamento temporal: publisher não aguarda consumer
- Resiliência: falhas isoladas não afetam producers
- Escalabilidade: múltiplos consumers por queue
- Baixa latência (<10ms publish)
- Infraestrutura leve (container único RabbitMQ)
- Management UI para monitorização
- Suporte nativo Spring AMQP

**Contras**:

- Consistência eventual (não imediata)
- Debugging mais complexo que REST
- Ordem de eventos não garantida entre exchanges

**Diagrama de fluxo**:

```plaintext
Books ──Book.Created──> Exchange LMS.books ──> Queue ──> Genres
  │                                                │
  │                                                └──> Lendings
  │
  └──Genre.Created──> Exchange LMS.genres ──> Queue ──> Readers
```

**Decisão**: RabbitMQ escolhido por balancear simplicidade, performance e resiliência para volumes esperados.

**Riscos Mitigados**:

- Perda de eventos: RabbitMQ persiste mensagens antes de ack
- Ordem: routing keys garantem ordem dentro do mesmo tipo de evento
- Monitorização: Management UI expõe métricas de queues e consumers

---

### ADR-002: Database-per-Service Pattern

**Contexto**: Isolamento de dados entre microserviços e autonomia de esquema.

#### Alternativas Consideradas

##### Alternativa 1: Database Partilhada (Rejeitada)

**Descrição**: Todos os serviços partilham a mesma instância PostgreSQL com schemas separados.

**Prós**:

- Joins SQL diretos entre tabelas
- Transações ACID cross-service
- Backup/restore simplificado

**Contras**:

- Acoplamento de esquema: mudanças afetam múltiplos serviços
- Contenção de recursos: um serviço pode esgotar conexões/CPU
- SPOF (Single Point of Failure): falha da DB afeta todos
- Escalabilidade limitada: não podemos escalar DBs independentemente

**Diagrama**:

```plaintext
┌────────────────────────────────────┐
│      PostgreSQL Partilhada         │
│  ┌────────┬────────┬────────────┐  │
│  │ Books  │ Genres │ Lendings   │  │
│  │ schema │ schema │ schema     │  │
│  └────────┴────────┴────────────┘  │
└────────────────────────────────────┘
        ▲         ▲           ▲
        │         │           │
    Books     Genres     Lendings
```

##### Alternativa 2: Database-per-Service (ACEITE)

**Descrição**: Cada microserviço possui a sua instância de base de dedicada.

**Prós**:

- Isolamento completo: falha de uma DB não afeta outros serviços
- Autonomia de esquema: cada serviço evolui schema independentemente
- Escalabilidade independente: podemos adicionar réplicas apenas onde necessário
- Polyglot persistence: Books usa PostgreSQL, Genres pode usar MySQL

**Contras**:

- Joins distribuídos impossíveis: necessário cache local ou agregação em código
- Transações distribuídas complexas: requer Saga pattern
- Duplicação de dados: cada serviço mantém cópia de entidades relacionadas
- Backup/restore mais complexo

**Diagrama implementado**:

```plaintext
lms-authnusers  →  PostgreSQL (users_db)
lms-books       →  PostgreSQL (books_db)
lms-genres      →  MySQL      (genres_db)
lms-readers     →  PostgreSQL (readers_db)
lms-lendings    →  PostgreSQL (lendings_db)
```

**Decisão**: Database-per-Service aceite para garantir isolamento e autonomia, essenciais para microserviços resilientes.

**Compromissos**:

- Cache local: Lendings mantém cópia de Books/Readers sincronizada via eventos
- Consistência eventual: sincronização via RabbitMQ (latência <100ms)
- Sem transações distribuídas: operações idempotentes e compensações manuais

---

### ADR-003: DirectExchange vs Topic/Fanout Exchange

**Contexto**: Escolha do tipo de exchange RabbitMQ para routing de eventos.

#### Alternativas Consideradas

##### Alternativa 1: FanoutExchange (Rejeitada)

**Descrição**: Broadcast de eventos para todas as queues ligadas, sem routing key.

**Prós**:

- Simples: não requer routing keys
- Broadcast automático para todos os consumers

**Contras**:

- Sem filtragem: todos recebem todos os eventos
- Desperdício de recursos: consumers processam eventos irrelevantes
- Escalabilidade pobre: tráfego cresce linearmente com consumers

**Diagrama**:

```plaintext
Publisher ──> FanoutExchange ──┬──> Queue1 (recebe tudo)
                               ├──> Queue2 (recebe tudo)
                               └──> Queue3 (recebe tudo)
```

##### Alternativa 2: TopicExchange (Rejeitada)

**Descrição**: Routing baseado em patterns (wildcards: `*`, `#`).

**Prós**:

- Flexibilidade: consumers podem subscrever patterns (`book.*`, `*.created`)
- Extensível: novos tipos de eventos sem reconfigurar bindings

**Contras**:

- Complexidade: patterns difíceis de manter
- Performance: matching de patterns mais lento que exact match
- Risco de subscrições incorretas: pattern errado recebe eventos inesperados

**Exemplo**:

```java
// Routing key: "book.created.fiction"
binding.with("book.*.fiction"); // Matches
binding.with("book.#");         // Matches
```

##### Alternativa 3: DirectExchange (ACEITE)

**Descrição**: Routing exato baseado em routing key específica.

**Prós**:

- Performance: O(1) lookup por routing key
- Clareza: binding explícito `Book.Created` → `autoDeleteQueue_Book_Created`
- Controlo fino: cada consumer escolhe exatamente os eventos que quer
- Debugging simples: routing keys aparecem nos logs RabbitMQ

**Contras**:

- Menos flexível: novos eventos requerem novos bindings
- Verboso: múltiplos bindings por consumer (Created/Updated/Deleted)

**Diagrama implementado**:

```plaintext
Books Publisher
  ├──> DirectExchange "LMS.books"
  │      └─── Book.Created ──> Queue ──> Lendings Consumer
  │      └─── Book.Updated ──> Queue ──> Lendings Consumer
  │      └─── Book.Deleted ──> Queue ──> Lendings Consumer
```

**Implementação**:

```java
@Bean
public Binding bindBookCreated(DirectExchange booksExchange,
                                Queue autoDeleteQueue_Book_Created) {
    return BindingBuilder.bind(autoDeleteQueue_Book_Created)
            .to(booksExchange)
            .with("Book.Created"); // Exact match
}
```

**Decisão**: DirectExchange escolhido por performance e clareza de bindings.

**Convenção de Naming**:

- Exchange: `LMS.{service}` (ex: `LMS.books`)
- Routing Key: `{Entity}.{Action}` (ex: `Book.Created`)
- Queue: `autoDeleteQueue_{Entity}_{Action}` (ex: `autoDeleteQueue_Book_Created`)

---

### ADR-004: Consistência Eventual vs Transações Distribuídas

**Contexto**: Garantir consistência de dados entre serviços sem transações distribuídas (2PC/3PC).

#### Alternativas Consideradas

##### Alternativa 1: Two-Phase Commit (2PC) (Rejeitada)

**Descrição**: Protocolo de transação distribuída com prepare/commit phases.

**Prós**:

- Consistência forte: ACID garantido cross-service
- Rollback automático em falha

**Contras**:

- Bloqueio: locks mantidos durante prepare phase (latência >500ms)
- SPOF: coordinator falha → toda a transação bloqueia
- Escalabilidade pobre: throughput reduz com número de participantes
- Complexidade: requer Transaction Manager (JTA, Atomikos)

**Diagrama**:

```plaintext
Coordinator
  ├─ Prepare → Books DB (lock)
  ├─ Prepare → Genres DB (lock)
  ├─ Prepare → Readers DB (lock)
  └─ Commit all or Rollback all
```

##### Alternativa 2: Saga Pattern com Orquestração (Não implementado)

**Descrição**: Transações locais + compensações coordenadas por orquestrador.

**Prós**:

- Sem locks distribuídos: cada transação local é rápida
- Compensações automáticas: orquestrador executa rollback semântico
- Visibilidade: orquestrador mantém estado da saga

**Contras**:

- SPOF: orquestrador centralizado
- Complexidade: definir compensações para cada step
- Não implementado: requer framework (Axon, Eventuate)

**Diagrama**:

```plaintext
Saga Orchestrator
  1. Create Book → Success
  2. Create Genre → Failure
  3. Compensate: Delete Book (rollback semântico)
```

##### Alternativa 3: Consistência Eventual com Eventos (ACEITE)

**Descrição**: Cada serviço faz transação local + publica evento; consumers sincronizam assincronamente.

**Prós**:

- Sem locks: alta throughput e baixa latência
- Resiliência: falha de um consumer não afeta producer
- Escalabilidade: consumers processam eventos em paralelo
- Simples: sem frameworks adicionais

**Contras**:

- Janela de inconsistência: dados sincronizam em ~100ms (eventual)
- Idempotência obrigatória: consumers devem lidar com eventos duplicados
- Sem compensação automática: rollback manual via eventos compensatórios

**Diagrama implementado**:

```plaintext
1. Books cria Book + publica Book.Created (transação local ACID)
   ──[100ms]──>
2. Genres consome Book.Created + sincroniza Book localmente
   ──[100ms]──>
3. Readers consome Genre.Created + sincroniza Genre localmente

Resultado: Consistência alcançada em ~200ms (eventual)
```

**Implementação**:

```java
@Transactional
public BookView create(CreateBookRequest request) {
    Book book = bookRepository.save(new Book(request)); // 1. Persistência local
    bookEventsPublisher.sendBookCreated(book);          // 2. Evento assíncrono
    return bookViewMapper.toBookView(book);
}
```

**Decisão**: Consistência Eventual aceite; trade-off de 100-200ms de latência é aceitável para operações CRUD de biblioteca.

**Garantias Implementadas**:

- Idempotência: consumers verificam existência antes de criar
- Ordenação: eventos do mesmo tipo processados em ordem (RabbitMQ FIFO)
- Retry: RabbitMQ redelivery em caso de falha de consumer

**Limitações Aceites**:

- Sem rollback automático: se Books cria livro mas Genres falha, livro permanece (eventual fix via admin ou evento compensatório manual)
- Janela de leitura inconsistente: GET imediatamente após POST pode não ver dados propagados

---

### ADR-005: Outbox Pattern - Não Implementado

**Contexto**: Garantir atomicidade entre persistência de dados e publicação de eventos.

#### Problema

**Cenário de falha sem Outbox**:

```plaintext
1. BEGIN TRANSACTION
2. bookRepository.save(book) → Sucesso (commit)
3. [FALHA AQUI: JVM crash, network timeout]
4. bookEventsPublisher.sendBookCreated() → Nunca executado
5. Resultado: Book persistido mas evento não publicado (inconsistência)
```

#### Alternativas Consideradas

##### Alternativa 1: Publicação Direta (IMPLEMENTADO - com risco)

**Descrição**: Publicar evento imediatamente após save, no mesmo método.

**Prós**:

- Simples: sem tabelas ou jobs adicionais
- Baixa latência: evento publicado em <10ms

**Contras**:

- Risco de perda: falha entre commit e publish → evento perdido
- Sem retry automático: se RabbitMQ down, evento não é enviado

**Implementação atual**:

```java
@Transactional
public BookView create(CreateBookRequest request) {
    Book book = bookRepository.save(book);          // Commit DB
    bookEventsPublisher.sendBookCreated(book);      // Publish (pode falhar)
    return bookViewMapper.toBookView(book);
}
```

##### Alternativa 2: Outbox Pattern com Polling (NÃO IMPLEMENTADO)

**Descrição**: Gravar evento numa tabela `outbox` na mesma transação; job assíncrono publica e marca como enviado.

**Prós**:

- Atomicidade: evento e dados commitados juntos
- Retry automático: poller re-envia eventos falhados
- Auditoria: histórico de eventos na tabela

**Contras**:

- Complexidade: requer tabela `outbox`, poller job, limpeza de eventos antigos
- Latência: polling interval (ex: 1s) adiciona delay
- Overhead: write adicional na DB por evento

**Diagrama**:

```plaintext
1. BEGIN TRANSACTION
2. bookRepository.save(book)
3. outboxRepository.save(OutboxEvent(BOOK_CREATED, payload))
4. COMMIT (ambos atómicos)

[Async Poller Job - cada 1s]
5. SELECT * FROM outbox WHERE sent=false
6. Para cada evento: rabbitTemplate.send()
7. UPDATE outbox SET sent=true WHERE id=?
```

**Implementação não realizada**:

```java
// Entity não criada
@Entity
public class OutboxEvent {
    @Id UUID id;
    String eventType;      // "BOOK_CREATED"
    String payload;        // JSON serializado
    boolean sent;
    LocalDateTime createdAt;
}

// Poller não implementado
@Scheduled(fixedDelay = 1000)
public void publishPendingEvents() {
    List<OutboxEvent> events = outboxRepo.findBySentFalse();
    for (OutboxEvent event : events) {
        rabbitTemplate.send(event.getPayload());
        event.setSent(true);
        outboxRepo.save(event);
    }
}
```

##### Alternativa 3: Transaction Log Tailing (Debezium/CDC) (NÃO IMPLEMENTADO)

**Descrição**: Debezium lê o log de transações do PostgreSQL e publica eventos no RabbitMQ.

**Prós**:

- Zero latência adicional: não afeta app
- Garantia: captura todas as alterações commitadas
- Sem código na app: Debezium é sidecar

**Contras**:

- Infraestrutura complexa: Kafka Connect, Debezium connector
- Configuração: mapeamento de colunas → eventos
- Overhead: não implementado por falta de tempo

**Decisão**: Outbox não implementado por falta de tempo; aceite risco de perda de eventos em cenários raros de crash.

**Mitigações Implementadas**:

- Idempotência: consumers verificam se entidade já existe antes de criar
- Health checks: Spring Actuator detecta falhas rapidamente
- RabbitMQ persistence: mensagens persistidas em disco antes de ack

**Plano Futuro**: Adicionar Outbox Pattern se requisitos de garantia aumentarem (SLA >99.9%).

---

### ADR-006: Polyglot Persistence - MySQL para Genres

**Contexto**: Demonstrar capacidade de usar diferentes bases de dados por serviço.

#### Alternativas Consideradas

##### Alternativa 1: PostgreSQL para Todos (Rejeitada)

**Descrição**: Usar PostgreSQL uniformemente em todos os microserviços.

**Prós**:

- Consistência de ferramentas: mesmo cliente psql, queries SQL compatíveis
- Menos configuração: um único driver JDBC
- Backups uniformes: pg_dump para todos

**Contras**:

- Não demonstra polyglot persistence
- Perde oportunidade de otimizar por workload

##### Alternativa 2: MySQL para Genres (ACEITE)

**Descrição**: lms-genres usa MySQL 8.0 enquanto outros serviços usam PostgreSQL 15.

**Prós**:

- Polyglot demonstration: prova que podemos misturar DBs
- Otimização: MySQL pode ser melhor para read-heavy workloads (Genres é consulta intensiva)
- Aprendizagem: equipa ganha experiência com múltiplas DBs

**Contras**:

- Overhead operacional: dois tipos de backup/restore
- Diferenças de SQL: queries não portáveis (ex: `LIMIT` vs `TOP`)
- Configuração duplicada: dois docker-compose DBs

**Implementação**:

```yaml
# docker-compose-swarm.yml (lms-genres)
genres_db_1:
  image: mysql:8.0
  environment:
    MYSQL_ROOT_PASSWORD: root
    MYSQL_DATABASE: genresDB
    MYSQL_USER: genres
    MYSQL_PASSWORD: genres
```

**application.properties**:

```properties
spring.datasource.url=jdbc:mysql://genres_db_1:3306/genresDB
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

**Decisão**: MySQL escolhido para Genres para demonstrar polyglot persistence e capacidade de otimizar DBs por características do serviço.

**Critério de Escolha**:

- Genres: read-heavy, queries simples → MySQL (ótimo para SELECT)
- Books/Lendings: write-heavy, relações complexas → PostgreSQL (ACID forte)

**Diagramas Atualizados**:

- Deployment-Diagram.puml reflete MySQL para genres_db
- C4-Level2-Container.puml mostra tecnologias diferentes

---

## Referências

- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials/)
- [Event-Driven Microservices Patterns](https://microservices.io/patterns/data/event-driven-architecture.html)
- [Microservices Patterns by Chris Richardson](https://microservices.io/book)
- [Database per Service Pattern](https://microservices.io/patterns/data/database-per-service.html)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)

---
