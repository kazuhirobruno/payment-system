-- 1. Remover a restrição NOT NULL das colunas de chaves estrangeiras
ALTER TABLE transaction ALTER COLUMN sender_id DROP NOT NULL;

ALTER TABLE transaction ALTER COLUMN receiver_id DROP NOT NULL;

-- 2. Adicionar a nova coluna 'type' para identificar o tipo da transação
-- Usamos VARCHAR(20) para armazenar os nomes do Enum (DEPOSIT, WITHDRAW, TRANSFER)
ALTER TABLE transaction ADD COLUMN type VARCHAR(20);

-- 3. [Opcional] Atualizar dados antigos (caso já existam transações no banco)
-- Como a estrutura antiga só permitia transferências, definimos o padrão antigo como 'TRANSFER'
UPDATE transaction SET type = 'TRANSFER' WHERE type IS NULL;

-- 4. Tornar a coluna 'type' obrigatória após garantir que todos os campos estão preenchidos
ALTER TABLE transaction ALTER COLUMN type SET NOT NULL;