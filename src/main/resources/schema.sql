CREATE TABLE IF NOT EXISTS words
(
    id   SERIAL PRIMARY KEY,
    word varchar(255)     NOT NULL
);

CREATE INDEX if not exists trgm_name_idx ON words USING gist (word gist_trgm_ops);