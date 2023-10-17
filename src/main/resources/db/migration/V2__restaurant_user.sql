CREATE TABLE restaurant_user (
id              UUID PRIMARY KEY,
restaurant_id   UUID NOT NULL REFERENCES restaurant(id),
user_id         UUID NOT NULL REFERENCES users(id),
position        TEXT NOT NULL,
created_at      TIMESTAMPTZ NOT NULL,
archived_at     TIMESTAMPTZ NULL
);

CREATE INDEX ON restaurant_user (restaurant_id);
CREATE INDEX ON restaurant_user (user_id);