package com.example.demopaymentsystem.wallet;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(SpringExtension.class)
public class WalletServiceIntegrationTest {
    @Autowired
    WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;

    @Test
    @Order(1)
    @Transactional
    void 지갑_생성() {
        // given
        Long userId = 1L;

        // when
        CreateWalletResponse createWalletResponse = walletService.createWallet(new CreateWalletRequest(userId));

        // then
        Assertions.assertNotNull(createWalletResponse);
    }

    @Test
    @Order(2)
    @Transactional
    void 여러개의_지갑_생성() throws InterruptedException {
        // given
        Long userId = 1L;
        CreateWalletRequest request = new CreateWalletRequest(userId);

        int numOfThread = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThread);
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < numOfThread; i++) {
            executorService.submit(() -> {
                        try {
                            walletService.createWallet(request);
                        } catch (Exception e) {
                            // e.printStackTrace();
                            exceptionCount.incrementAndGet();
                        } finally {
                            completedTasks.incrementAndGet();
                        }
                    }
            );
        }

        // then
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        Assertions.assertTrue(finished);
        Assertions.assertEquals(exceptionCount.get(), 19);
        Assertions.assertNotNull(walletRepository.findAllByUserId(userId));
    }
}
