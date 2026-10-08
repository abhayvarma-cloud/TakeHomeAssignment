CREATE TABLE IF NOT EXISTS item_seq (
    id       INT PRIMARY KEY CHECK (id = 1),
    last_seq BIGINT NOT NULL
);
CREATE TABLE IF NOT EXISTS outbox_event (
    id              UUID PRIMARY KEY,
    topic           VARCHAR(249) NOT NULL,          -- Kafka topic to publish to
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,          -- used as the Kafka message key
    event_type      VARCHAR(150) NOT NULL,
    payload         JSONB        NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON outbox_event (created_at) WHERE status = 'PENDING';