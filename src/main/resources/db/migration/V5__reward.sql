CREATE TABLE reward (
id                  UUID PRIMARY KEY,
name                TEXT NOT NULL,
restaurant_id       UUID NOT NULL REFERENCES restaurant(id),
archived_at         TIMESTAMPTZ NULL
);

CREATE INDEX ON reward (restaurant_id);