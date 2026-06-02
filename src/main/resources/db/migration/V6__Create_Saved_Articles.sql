CREATE TABLE saved_articles (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_saved_articles UNIQUE (article_id, user_id)
);

CREATE INDEX idx_saved_articles_article_id ON saved_articles(article_id);
CREATE INDEX idx_saved_articles_user_id ON saved_articles(user_id);
