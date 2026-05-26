-- Flyway Migration V2__Add_Constraints.sql

-- Add comment constraints
ALTER TABLE users ADD CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$');
ALTER TABLE articles ADD CONSTRAINT chk_title_not_empty CHECK (length(trim(title)) > 0);
ALTER TABLE articles ADD CONSTRAINT chk_content_not_empty CHECK (length(trim(content)) > 0);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_update_timestamp
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

CREATE TRIGGER articles_update_timestamp
BEFORE UPDATE ON articles
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- Create view for published articles statistics
CREATE OR REPLACE VIEW article_statistics AS
SELECT 
    u.id,
    u.name,
    u.email,
    COUNT(a.id) as total_articles,
    COUNT(CASE WHEN a.published = true THEN 1 END) as published_articles,
    COUNT(CASE WHEN a.published = false THEN 1 END) as draft_articles,
    MAX(a.published_at) as last_published_at
FROM users u
LEFT JOIN articles a ON u.id = a.author_id
GROUP BY u.id, u.name, u.email;
