package com.example.demopaymentsystem.wallet;

import java.math.BigDecimal;

public record AddWalletBalanceRequest(Long walletId, BigDecimal amount) {
}