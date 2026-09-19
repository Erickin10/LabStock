CREATE TABLE project_items (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    quantity_needed INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_project_items_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_items_item FOREIGN KEY (item_id) REFERENCES items (id)
);
