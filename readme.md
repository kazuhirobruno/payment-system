# Payment System 🪙

[![Java Version](https://shields.io)](https://openjdk.org)
[![Spring Boot](https://shields.io)](https://spring.io)
[![Database](https://shields.io)](https://postgresql.org)
[![Code Quality](https://shields.io)](https://www.sonarsource.com/products/sonarqube/)

Um sistema transacional simplificado de carteira digital projetado sob princípios de engenharia de software de alta performance. O foco principal da aplicação está na consistência de dados, isolamento de escopo e robustez arquitetural, servindo como consolidação de boas práticas em desenvolvimento backend Java e Spring Boot corporativo.

---

## 🚀 Funcionalidades do Sistema

A API gerencia movimentações financeiras individuais e intercontas de usuários autenticados:

- **Gerenciamento de Usuários (`/user`)**: Cadastro, consulta, atualização de saldo e exclusão lógica (_Soft Delete_).
- **Depósitos (`POST /transaction/deposit`)**: Injeção de valores com validações de domínio.
- **Saques (`POST /transaction/withdraw`)**: Retirada com validação de saldo em tempo real.
- **Transferências (`POST /transaction/transfer`)**: Movimentação entre contas com controle de concorrência.
- **Extrato Financeiro (`GET /transaction/statement`)**: Histórico paginado de transações.

---

## 🏛️ Decisões de Arquitetura e Engenharia

### 1. Controle de Concorrência (Pessimistic Locking)

Operações críticas utilizam `@Lock(PESSIMISTIC_WRITE)` para garantir consistência em cenários concorrentes.

- Estratégia de ordenação de UUIDs para evitar deadlocks em transferências simultâneas.

### 2. Separação por Casos de Uso (Use Cases)

Regras de negócio isoladas em serviços específicos, mantendo controllers enxutas e desacopladas.

### 3. Isolamento de Domínios

O módulo de transações não acessa diretamente repositórios de usuários, garantindo separação clara de responsabilidades.

### 4. Qualidade de Código e Observabilidade

O projeto evoluiu com foco em qualidade contínua:

- Integração de **SonarQube** para análise estática de código
- Refatoração de testes para aderência ao Sonar:
  - remoção de stubs desnecessários (Mockito strict mode)
  - uso de method references (`UserNotFoundException::new`)
  - refatoração de `assertThrows` para evitar múltiplas invocações
  - remoção de dependência de clock implícito em testes
- Melhoria na legibilidade e manutenibilidade geral do código

---

## 🧪 Testes Automatizados

A aplicação conta com uma suíte robusta de testes:

- **Testes Unitários (JUnit + Mockito)** para regras de negócio isoladas
- **Testes de integração** para validação de persistência e fluxo completo
- **Testes de controller (MockMvc)** para validação de contratos HTTP

Cobertura de cenários críticos:

- Validação de saldo
- Transferências seguras
- Regras de usuário ativo/inativo
- Tratamento de exceções customizadas

---

## 🛠️ Tecnologias Utilizadas

- Java 17 / Spring Boot 3
- Spring Data JPA & Hibernate
- PostgreSQL / H2 Database
- Spring Security & JWT
- OpenAPI 3 / Swagger
- Jakarta Bean Validation
- Docker
- SonarQube (análise estática de código)
- JUnit 5 / Mockito

---

## 📋 Qualidade e Build

```bash
mvn clean test
mvn sonar:sonar
```
