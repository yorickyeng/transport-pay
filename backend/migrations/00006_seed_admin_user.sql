-- +goose Up
-- +goose StatementBegin
INSERT INTO users (login, password_hash, is_admin) 
VALUES ('admin', '$2a$12$azREhx9pYZllTT1ZQNnq..N.yhGPjSjY94ZqBHlQVYxPNDIf31kfa', 1);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DELETE FROM users WHERE login = 'admin';
-- +goose StatementEnd