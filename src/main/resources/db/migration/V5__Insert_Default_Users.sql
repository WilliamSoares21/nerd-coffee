-- Flyway Migration V5__Insert_Default_Users.sql

-- Insere um usuário administrador padrão (caso não exista)
-- Senha do hash: admin123 (gerado via BCrypt)
INSERT INTO users (name, email, password, role, active)
VALUES ('Admin Morning', 'admin@morning.dev', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36XQuvjO', 'ADMIN', true)
ON CONFLICT (email) DO NOTHING;
