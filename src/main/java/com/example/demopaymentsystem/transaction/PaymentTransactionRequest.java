package com.example.demopaymentsystem.transaction;

import java.math.BigDecimal;

public record PaymentTransactionRequest(
        Long walletId, String orderId, BigDecimal amount) {
}