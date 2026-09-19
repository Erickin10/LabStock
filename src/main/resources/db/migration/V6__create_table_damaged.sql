CREATE TABLE damaged (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    lab_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_damaged_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_damaged_student FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_damaged_lab FOREIGN KEY (lab_id) REFERENCES users (id)
);
