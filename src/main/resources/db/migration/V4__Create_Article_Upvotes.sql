-- Flyway Migration V4__Create_Article_Upvotes.sql

CREATE TABLE article_upvotes (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_article_upvote UNIQUE (article_id, user_id)
);

-- Índices adicionais para otimização de pesquisas e agregados
CREATE INDEX idx_article_upvotes_article_id ON article_upvotes(article_id);
CREATE INDEX idx_article_upvotes_user_id ON article_upvotes(user_id);
