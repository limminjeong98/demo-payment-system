CREATE TABLE wallet
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    user_id    BIGINT                NULL,
    balance    DECIMAL               NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id)
);