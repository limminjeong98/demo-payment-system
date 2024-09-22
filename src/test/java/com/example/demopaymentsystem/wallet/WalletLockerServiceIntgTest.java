package com.example.demopaymentsystem.wallet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class WalletLockerServiceIntgTest {

    @Autowired
    WalletLockerService walletLockerService;

    @Test
    void test_acquire_lock() {
        // given
        Long userId = 1L;

        // when & then
        // 락 획득
        WalletLockerService.Lock lock = walletLockerService.acquireLock(userId);
        Assertions.assertNotNull(lock);

        // 락을 획득하고 반환하지 않은 상태에서는 추가 획득 실패
        WalletLockerService.Lock acquireFailLock = walletLockerService.acquireLock(userId);
        Assertions.assertNull(acquireFailLock);

        // 락을 반환한 후 재시도하면 획득 성공
        walletLockerService.releaseLock(lock);
        WalletLockerService.Lock acquireSuccessLock = walletLockerService.acquireLock(userId);
        Assertions.assertNotNull(acquireSuccessLock);
        Assertions.assertEquals(acquireSuccessLock.getKey(), "wallet-lock:" + userId);
        walletLockerService.releaseLock(acquireSuccessLock);

    }
}
