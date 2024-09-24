package com.example.demopaymentsystem.transaction;

import java.math.BigDecimal;

public record PgPaymentTransactionRequest(Long userId, String orderId, BigDecimal amount) {
}
