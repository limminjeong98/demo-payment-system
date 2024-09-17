CREATE TABLE transaction
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    user_id          BIGINT NULL,
    wallet_id        BIGINT NULL,
    order_id         VARCHAR(255) NULL,
    transaction_type VARCHAR(255) NULL,
    amount           DECIMAL NULL,
    `description`    VARCHAR(255) NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id)
);