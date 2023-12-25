CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
id              UUID PRIMARY KEY,
email           TEXT NOT NULL,
password        TEXT NOT NULL,
first_name      TEXT NOT NULL,
last_name       TEXT NOT NULL,
nick_name       TEXT NOT NULL,
role            TEXT NOT NULL,
created_at      TIMESTAMPTZ NOT NULL,
blocked_at      TIMESTAMPTZ NULL,
archived_at     TIMESTAMPTZ NULL
);

CREATE TABLE restaurant (
id              UUID PRIMARY KEY,
email           TEXT NOT NULL,
name            TEXT NOT NULL,
created_by      UUID NOT NULL REFERENCES users(id),
created_at      TIMESTAMPTZ NOT NULL,
archived_at     TIMESTAMPTZ NULL
);

CREATE TABLE stamp (
id              UUID PRIMARY KEY,
restaurant_id   UUID NOT NULL REFERENCES restaurant(id),
user_id         UUID NOT NULL REFERENCES users(id),
created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX ON stamp (restaurant_id);
CREATE INDEX ON stamp (user_id);