package com.example.demopaymentsystem.wallet;

import java.math.BigDecimal;

public record CreateWalletResponse(
        Long id, Long userId, BigDecimal balance
) {
}