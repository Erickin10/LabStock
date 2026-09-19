CREATE TABLE lendings (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    returned BOOLEAN NOT NULL DEFAULT FALSE,
    lending_date TIMESTAMP,
    return_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_lendings_item FOREIGN KEY (item_id) REFERENCES items (id),
    CONSTRAINT fk_lendings_student FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_lendings_teacher FOREIGN KEY (teacher_id) REFERENCES users (id)
);
