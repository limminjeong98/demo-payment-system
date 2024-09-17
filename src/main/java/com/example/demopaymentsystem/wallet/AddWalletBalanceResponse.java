package com.example.demopaymentsystem.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddWalletBalanceResponse(
        Long id, Long userId, BigDecimal balance,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {
}