package com.example.demopaymentsystem.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FindWalletResponse(
        Long id, Long userId, BigDecimal balance,
        LocalDateTime createdAt, LocalDateTime updatedAt
) {
}