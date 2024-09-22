package com.example.demopaymentsystem.transaction;

import com.example.demopaymentsystem.wallet.FindWalletResponse;
import com.example.demopaymentsystem.wallet.WalletLockerService;
import com.example.demopaymentsystem.wallet.WalletService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
@Service
public class LockTransactionService {
    private final WalletLockerService walletLockerService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        final FindWalletResponse findWalletResponse = walletService.findWalletByUserId(request.userId());
        WalletLockerService.Lock lock = walletLockerService.acquireLock(findWalletResponse.id());
        if (lock == null) {
            log.info("락 취득 실패");
            throw new IllegalStateException("cannot acquire lock");
        }

        try {
            log.info("락 취득 성공");
            return transactionService.charge(request);
        } finally {
            walletLockerService.releaseLock(lock);
        }
    }
}
