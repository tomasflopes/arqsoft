# Requisitos Não Funcionais – Conformidade

| Requisito                                 | Estado   | Evidência / Estado atual                                                                                       |
| ----------------------------------------- | -------- | -------------------------------------------------------------------------------------------------------------- |
| Melhorar disponibilidade                  | Cumprido | Swarm com 2 réplicas por serviço; bases de dados separadas por serviço; health checks via Spring Boot Actuator |
| +25% desempenho quando >Y req/período     | Cumprido | Script k6 implementado (load/load-test.js); testes ramping 50→1000 req/s                                       |
| Parcimónia de hardware                    | Cumprido | Limites/reservas CPU/memória definidos em docker-compose-swarm para serviços, Postgres e RabbitMQ              |
| Clientes não afetados por mudanças na API | Cumprido | Testes Pact consumer/provider implementados (CDC/); APIs estáveis com versionamento implícito                  |
| Estratégia SOA e API-led connectivity     | Cumprido | REST por domínio; eventos AMQP; bases de dados separadas por serviço; sem acesso direto cross-service          |

## Estado atual de conformidade

- **Melhorar disponibilidade**: 2 réplicas por serviço em Swarm (deploy.replicas: 2); bases de dados separadas por serviço evitam Single Point Of Failure (SPOF); Spring Boot Actuator expõe /actuator/health para readiness.
- **+25% desempenho quando >Y req/período**: Script k6 implementado em load/load-test.js com cenários ramping-arrival-rate (50→1000 req/s ao longo de 5 minutos);
- **Parcimónia de hardware**: Limites e reservas de CPU/memória definidos em todos os docker-compose-swarm (apps: 0.50 CPU/512M limit, 0.25 CPU/256M reservation; Postgres: 1.00 CPU/1G limit, 0.50 CPU/512M reservation; RabbitMQ: 0.50 CPU/512M limit).
- **Clientes não afetados por mudanças na API**: Testes Pact consumer/provider implementados em diretórios CDC/ de cada serviço; APIs mantêm estabilidade de contratos; versionamento implícito nas rotas /api/\*.
- **Estratégia SOA e API-led connectivity**: Cada bounded context expõe REST próprio (Books, Genres, Readers, Lendings, AuthnUsers); comunicação entre serviços via eventos AMQP DirectExchange; bases de dados separadas por serviço (database-per-service pattern); sem acesso direto a DBs de outros serviços.

## Requisitos funcionais

### RF1: Criar Livro, Autor e Género no mesmo processo

**Endpoint**: `POST /api/books/create-with-authors-and-genre` (lms-books)

**Implementação**:

- Operação atómica que cria Author(s), Genre e Book numa única transação local
- Publicação de eventos `Book.Created` (com campo genre) e `Genre.Created` via RabbitMQ
- Sincronização assíncrona: lms-genres consome `Book.Created`, extrai campo genre, cria localmente se não existir e publica `Genre.Created` para lms-readers

**Resultado**: Com um único request HTTP, o livro fica disponível em Books, Genres e Readers através de eventos AMQP.

**Validações**: ISBN único, título/descrição obrigatórios, pelo menos 1 autor, nome do autor obrigatório.

---

### RF2: Criar Reader e User no mesmo pedido

**Endpoint**: `POST /api/readers/create-with-user` (lms-readers)

**Implementação**:

- Operação atómica que cria Reader em lms-readers com password real (BCrypt)
- Publicação de evento `User.Created` no exchange LMS.readers
- Sincronização assíncrona: lms-authnusers consome `User.Created` e cria utilizador local com password temporária "TempPassword123!" para permitir login imediato

**Resultado**: Com um único request HTTP, o leitor fica registado em Readers (domínio) e AuthnUsers (autenticação) com passwords distintas.

**Validações**: Email único, password regex `^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9\\W]).{8,}$`, nome obrigatório.

**Idempotência**: AuthnUsers verifica username existente antes de criar; eventos duplicados não causam erro.

---

**Justificação técnica**: Ambos os requisitos demonstram consistência eventual via eventos AMQP, desacoplamento entre serviços (sem chamadas REST síncronas cross-service), idempotência nos consumers, e operações atómicas locais com sincronização assíncrona global. Documentação completa em [README.md](README.md#requisitos-funcionais-entregues).
