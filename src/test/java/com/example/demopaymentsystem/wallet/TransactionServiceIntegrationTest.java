package com.example.demopaymentsystem.wallet;

import com.example.demopaymentsystem.transaction.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
public class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Order(1)
    @Transactional
    void 충전_결제_생성() {
        // given
        Long userId = 1L;
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(userId));
        Long walletId = createWalletResponse.id();
        walletService.addBalance(new AddWalletBalanceRequest(walletId, new BigDecimal(1000)));
        String orderId = "orderId";
        PaymentTransactionRequest request = new PaymentTransactionRequest(
                walletId, orderId, new BigDecimal(10)
        );

        // when
        PaymentTransactionResponse response = transactionService.payment(request);

        // then
        Assertions.assertNotNull(response);
    }

    @Test
    @Order(2)
    @Transactional
    void 충전_진행() {
        // given
        Long userId = 1L;
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(userId));
        Long walletId = createWalletResponse.id();
        walletService.addBalance(new AddWalletBalanceRequest(walletId, new BigDecimal(1000)));
        String orderId = "orderId";
        ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest(
                userId, orderId, BigDecimal.TEN
        );

        // when
        ChargeTransactionResponse response = transactionService.charge(chargeTransactionRequest);

        // then
        Assertions.assertNotNull(response);
    }

    @Disabled
    @DisplayName("실패하는 테스트")
    @Test
    @Order(3)
    @Transactional
    void 여러개의_충전_결제_생성() throws InterruptedException {
        // given
        Long userId = 1L;
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(userId));
        Long walletId = createWalletResponse.id();
        System.out.println("walletId = " + walletId);
        walletService.addBalance(new AddWalletBalanceRequest(walletId, new BigDecimal(1000)));
        String orderId = "orderId";
        ChargeTransactionRequest chargeTransactionRequest = new ChargeTransactionRequest(
                userId, orderId, BigDecimal.TEN
        );

        int numOfThread = 1; // 20
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numOfThread; i++) {
            executorService.submit(() -> {
                try {
                    ChargeTransactionResponse response = transactionService.charge(chargeTransactionRequest);
                    System.out.println(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionCount.incrementAndGet();
                } finally {
                    completedTasks.incrementAndGet();
                }
            });
        }

        // then
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        Assertions.assertTrue(finished);
        Assertions.assertEquals(exceptionCount.get(), numOfThread - 1);
        Assertions.assertNotNull(transactionRepository.findTransactionByOrderId(orderId));
    }
}
