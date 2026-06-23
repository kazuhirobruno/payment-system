# Wallet Core API

API REST para gerenciamento de usuários, contas e transferências financeiras entre usuários, desenvolvida com Java e Spring Boot.

## Objetivo

O projeto tem como objetivo demonstrar conhecimentos valorizados em processos seletivos para vagas backend em instituições financeiras e grandes empresas de tecnologia, incluindo:

- Desenvolvimento de APIs REST
- Segurança com JWT
- Persistência de dados
- Testes automatizados
- Documentação de APIs
- Boas práticas de arquitetura
- Tratamento de exceções
- Versionamento de banco de dados

---

# Stack Tecnológica

## Backend

- Java 21
- Spring Boot 3

## Persistência

- Spring Data JPA
- PostgreSQL
- Flyway

## Segurança

- Spring Security
- JWT

## Documentação

- OpenAPI / Swagger

## Qualidade

- JUnit 5
- Mockito
- Testcontainers

## Utilitários

- Lombok
- MapStruct

---

# Funcionalidades

## Usuários

- Criar usuário
- Buscar usuário por ID
- Listar usuários
- Atualizar usuário
- Bloquear usuário

## Autenticação

- Login
- Geração de Token JWT
- Controle de acesso por perfil

## Contas

- Consultar saldo
- Realizar depósito
- Consultar informações da conta

## Transferências

- Transferir valores entre usuários
- Consultar histórico de transferências
- Buscar transferência por ID

---

# Estrutura do Projeto

```text
br.com.seunome.wallet

├── account
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   └── mapper
│
├── transaction
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   ├── dto
│   └── mapper
│
├── auth
│   ├── controller
│   ├── service
│   ├── dto
│   └── security
│
├── exception
├── config
└── shared
```

---

# Entidades Principais

## User

```java
@Entity
public class User {

    @Id
    private UUID id;

    private String name;

    private String email;

    private String cpf;

    private boolean active;
}
```

## Account

```java
@Entity
public class Account {

    @Id
    private UUID id;

    private BigDecimal balance;

    @OneToOne
    private User user;
}
```

## Transaction

```java
@Entity
public class Transaction {

    @Id
    private UUID id;

    private UUID senderId;

    private UUID receiverId;

    private BigDecimal amount;

    private LocalDateTime createdAt;
}
```

---

# Segurança

## Autenticação

JWT (JSON Web Token)

## Perfis

```text
ROLE_USER
ROLE_ADMIN
```

## Endpoints Públicos

```http
POST /auth/login
POST /users
```

## Endpoints Protegidos

```http
GET  /accounts/{id}
POST /transfers
GET  /transactions
```

---

# Validações

Implementadas utilizando Bean Validation.

Exemplos:

```java
@NotBlank
private String name;

@Email
private String email;

@NotNull
@Positive
private BigDecimal amount;
```

---

# Tratamento Global de Exceções

Centralização de erros utilizando:

```java
@RestControllerAdvice
```

Exemplo de resposta:

```json
{
  "timestamp": "2026-06-17T10:00:00",
  "status": 400,
  "message": "Saldo insuficiente"
}
```

---

# Banco de Dados

## PostgreSQL

Banco relacional utilizado pela aplicação.

## Flyway

Controle de versionamento das migrations.

Exemplo:

```text
V1__create_users_table.sql
V2__create_accounts_table.sql
V3__create_transactions_table.sql
```

---

# Documentação

## Swagger / OpenAPI

Disponível em:

```text
/swagger-ui.html
```

Documentação dos endpoints:

- Autenticação
- Usuários
- Contas
- Transferências

---

# Testes

## Testes Unitários

Ferramentas:

- JUnit 5
- Mockito

Cobertura esperada:

```text
80% ou superior
```

Principais cenários:

- Regras de negócio
- Transferências
- Validações
- Tratamento de exceções

---

## Testes de Integração

Ferramentas:

- Spring Boot Test
- Testcontainers
- PostgreSQL Container

Validações:

- Endpoints REST
- Integração com banco
- Fluxos autenticados
- Persistência de dados

---

# Dependências do Spring Initializr

Selecionar:

- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- PostgreSQL Driver
- Spring Boot Actuator

---

# Dependências Adicionais

## Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## JWT

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.7</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.7</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.7</version>
    <scope>runtime</scope>
</dependency>
```

## Flyway

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## OpenAPI

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

## MapStruct

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.6.3</version>
</dependency>
```

## Testcontainers

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

---

# Diferenciais do Projeto

- Arquitetura organizada por domínio
- Segurança com JWT
- Banco PostgreSQL
- Versionamento com Flyway
- Testes unitários
- Testes de integração
- Swagger/OpenAPI
- Tratamento global de exceções
- Bean Validation
- Código limpo e documentado

---

# Configuração do Spring Initializr

```text
Project: Maven
Language: Java
Spring Boot: 3.x

Group: br.com.seunome
Artifact: wallet-core
Name: Wallet Core
Description: Digital wallet and payment transfer platform

Package Name:
br.com.seunome.wallet

Packaging: Jar
Java: 21
```
