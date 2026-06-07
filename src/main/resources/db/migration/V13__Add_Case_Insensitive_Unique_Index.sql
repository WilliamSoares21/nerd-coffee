-- Resolve conflicts by renaming duplicates (keeping the one with the smallest id)
WITH conflicts AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY LOWER(username) ORDER BY id ASC) as rn
    FROM users
    WHERE username IS NOT NULL
)
UPDATE users u
SET username = LOWER(u.username) || '_conflict_' || u.id::text
FROM conflicts c
WHERE u.id = c.id AND c.rn > 1;

-- Create case-insensitive unique index
CREATE UNIQUE INDEX idx_users_username_lower ON users (LOWER(username));
