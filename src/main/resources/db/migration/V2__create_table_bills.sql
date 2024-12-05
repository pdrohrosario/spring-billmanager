CREATE TABLE bills (
    id SERIAL PRIMARY KEY,
    payment_date DATE NOT NULL,
    due_date DATE,
    amount NUMERIC(15, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    bill_status VARCHAR(10) NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);