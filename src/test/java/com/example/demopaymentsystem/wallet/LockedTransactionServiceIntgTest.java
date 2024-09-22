package com.example.demopaymentsystem.wallet;

import com.example.demopaymentsystem.transaction.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.BDDAssertions.then;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class LockedTransactionServiceIntgTest {

    @Autowired
    LockTransactionService lockTransactionService;

    @Autowired
    WalletService walletService;

    @Autowired
    TransactionRepository transactionRepository;

    @Test
    void 충전을_동시에_실행한다() throws InterruptedException {
        // given
        Long userId = 1L;
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(userId));
        Long walletId = createWalletResponse.id();
        walletService.addBalance(new AddWalletBalanceRequest(walletId, new BigDecimal(1000)));

        int numOfThread = 20;
        ExecutorService service = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numOfThread; i++) {
            String orderId = UUID.randomUUID().toString();
            ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest(userId, orderId, BigDecimal.TEN);
            service.submit(() -> {
                try {
                    ChargeTransactionResponse chargeTransactionResponse = lockTransactionService.charge(chargeTransactionRequest);
                } catch (Exception e) {
                    // e.printStackTrace();
                    exceptionCount.incrementAndGet();
                } finally {
                    completedTasks.incrementAndGet();
                }
            });
        }

        service.shutdown();
        boolean finished = service.awaitTermination(1, TimeUnit.MINUTES);

        // then
        Assertions.assertTrue(finished);

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);
        Assertions.assertEquals(exceptionCount.get() + transactions.size(), numOfThread);
        then(transactions).hasSizeGreaterThan(0);
    }
}
