-- Flyway Migration V14__Add_Comment_Interactions.sql

ALTER TABLE comments ADD COLUMN parent_id BIGINT REFERENCES comments(id) ON DELETE CASCADE;

CREATE TABLE comment_upvotes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_comment_upvote UNIQUE (comment_id, user_id)
);

CREATE INDEX idx_comment_upvotes_comment_id ON comment_upvotes(comment_id);
CREATE INDEX idx_comment_upvotes_user_id ON comment_upvotes(user_id);
