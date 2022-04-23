-- liquibase formatted sql
-- changeset estonec:1
CREATE TABLE notice (
    id SERIAL UNIQUE,
    chat_id SERIAL,
    text TEXT,
    firstname TEXT,
    notice_timestamp TIMESTAMP
)
