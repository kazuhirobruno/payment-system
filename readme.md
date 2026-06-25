# Payment System 🪙

[![Java Version](https://shields.io)](https://openjdk.org)
[![Spring Boot](https://shields.io)](https://spring.io)
[![Database](https://shields.io)](https://postgresql.org)

Um sistema transacional simplificado de carteira digital projetado sob princípios de engenharia de software de alta performance. O foco principal da aplicação está na consistência de dados, isolamento de escopo e robustez arquitetural, servindo como consolidação de boas práticas em desenvolvimento backend Java e Spring Boot corporativo.

## 🚀 Funcionalidades do Sistema

A API gerencia movimentações financeiras individuais e intercontas de usuários autenticados, expondo endpoints específicos orientados a recursos:

- **Gerenciamento de Usuários (`/user`)**: Fluxos de cadastro de clientes com aporte de saldo inicial, obtenção de dados consolidados de perfil, alteração de credenciais e exclusão lógica segura (_Soft Delete_).
- **Depósitos (`POST /transaction/deposit`)**: Injeção de valores na carteira digital com validações automáticas de formato e limites de tipo.
- **Saques (`POST /transaction/withdraw`)**: Retirada de valores com verificação em tempo real de consistência de saldo.
- **Transferências (`POST /transaction/transfer`)**: Movimentação de valores entre contas distintas de usuários de forma segura.
- **Extrato Financeiro (`GET /transaction/statement`)**: Histórico consolidado de transações lido de forma paginada e ordenada.

---

## 🏛️ Decisões de Arquitetura e Engenharia

Para garantir que a aplicação simule o comportamento de sistemas bancários de grande porte sob alta carga, as seguintes premissas foram adotadas:

### 1. Prevenção de Concorrência e Deadlocks (Pessimistic Locking)

Operações de débito (como Saque e Transferência) utilizam **Lock Pessimista** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) a nível de banco de dados.

- No fluxo de transferência, para evitar furos de concorrência (_Race Conditions_) e travamentos mútuos (_Deadlocks_), o sistema aplica uma **estratégia de ordenação estrita dos UUIDs envolvidos**, garantindo que as linhas do banco sejam travadas sempre na mesma sequência alfabética.

### 2. Arquitetura Orientada a Casos de Uso (Use Cases)

O domínio de negócio foi desacoplado em componentes de caso de uso únicos (_Single Responsibility Principle_). As controllers interagem exclusivamente com fluxos isolados (`DepositUseCase`, `WithdrawUseCase`, `TransferUseCase`, `GetStatementUseCase`), mantendo a camada HTTP limpa e agnóstica às regras internas de persistência.

### 3. Isolamento Rígido entre Módulos

Para preservar o isolamento arquitetural entre o módulo de usuários (`user`) e de movimentações (`transaction`), a camada de transações nunca acessa ou injeta os repositórios de usuários. Toda validação, checagem de existência ou mutação matemática de saldo é delegada via código para o `UserService`.

### 4. Pirâmide de Testes Automatizados Robustos

A aplicação conta com uma suite de **80 testes automatizados** com 100% de sucesso, dividida de forma cirúrgica:

- **Testes Unitários de Negócio**: Isolamento completo utilizando JUnit 5 e Mockito para validar fluxos lógicos e exceções customizadas (`UserNotFoundException`, `DeletedUserLoginException`, `NegativeAmountException`, `SameAccountTransferException`, `ReceiverUserInactiveException`).
- **Testes WebMvc (Fatiamento)**: Simulação de requisições HTTP via `MockMvc` para validar códigos de status (`201`, `403`, `404`, `422`), contratos de DTOs e ativação do `Jakarta Bean Validation`.
- **Testes de Integração de Ponta a Ponta**: Execução de cenários reais contra banco de dados em memória **H2** utilizando `@SpringBootTest` e `@Transactional` para garantir o comportamento correto de persistência física e rollbacks automáticos.

---

## 🛠️ Tecnologias Utilizadas

- **Java 17 / Spring Boot 3**
- **Spring Data JPA & Hibernate**
- **PostgreSQL** (Produção) / **H2 Database** (Ambiente de Testes)
- **Spring Security & JWT** (Autenticação baseada em contextos de atributos de requisição)
- **OpenAPI 3 / Swagger** (Documentação viva e integrada das rotas e esquemas)
- **Jakarta Bean Validation** (Segurança na borda contra payloads inválidos)

---

## 📋 Como Executar a Suite de Testes

Para validar a integridade de todas as camadas e compilar o projeto de forma limpa, execute o comando abaixo no terminal da raiz do projeto:

```bash
mvn clean test
```
