UPDATE users SET username = LOWER(username) WHERE username IS NOT NULL;
