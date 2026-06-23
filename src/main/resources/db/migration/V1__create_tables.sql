-- Criação da tabela de Usuários
CREATE TABLE "user" (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Criação da tabela de Contas
CREATE TABLE account (
    id UUID PRIMARY KEY,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    user_id UUID NOT NULL,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

-- Criação da tabela de Transações
CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_transaction_sender FOREIGN KEY (sender_id) REFERENCES "user" (id),
    CONSTRAINT fk_transaction_receiver FOREIGN KEY (receiver_id) REFERENCES "user" (id)
);