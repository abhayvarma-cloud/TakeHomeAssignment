CREATE TABLE IF NOT EXISTS item_seq (
    id       INT PRIMARY KEY CHECK (id = 1),
    last_seq BIGINT NOT NULL
);
