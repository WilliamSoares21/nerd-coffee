-- Flyway Migration V1__Initial_Schema.sql

-- Create enum type for user roles
CREATE TYPE user_role AS ENUM ('ADMIN', 'EDITOR', 'VIEWER');

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'VIEWER',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

-- Create articles table
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    summary TEXT,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP
);

-- Create indexes on articles
CREATE INDEX idx_articles_author_id ON articles(author_id);
CREATE INDEX idx_articles_published ON articles(published);
CREATE INDEX idx_articles_created_at ON articles(created_at DESC);
CREATE INDEX idx_articles_title ON articles USING GIN(to_tsvector('english', title));

-- Insert default admin user (password: admin123 - must be changed in production)
-- Password hash generated with BCryptPasswordEncoder
INSERT INTO users (name, email, password, role, active)
VALUES ('Admin User', 'admin@coffenerd.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36XQuvjO', 'ADMIN', true)
ON CONFLICT DO NOTHING;

-- Create sample articles
INSERT INTO articles (title, content, summary, author_id, published, published_at)
SELECT 
    'Bem-vindo ao Coffe Nerd',
    'Este é um exemplo de artigo no blog Coffe Nerd. Aqui você encontrará conteúdo sobre tech news, programação e tópicos relacionados à tecnologia para nerds que gostam de café.',
    'Um artigo introdutório sobre o Coffe Nerd blog',
    id,
    true,
    CURRENT_TIMESTAMP
FROM users 
WHERE email = 'admin@coffenerd.com'
ON CONFLICT DO NOTHING;
