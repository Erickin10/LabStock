CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    suggestion INTEGER NOT NULL,
    bought BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_purchases_item FOREIGN KEY (item_id) REFERENCES items (id)
);
