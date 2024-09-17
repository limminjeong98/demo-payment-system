package com.example.demopaymentsystem.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class WalletService {

    private static final BigDecimal BALANCE_LIMIT = new BigDecimal(10_000_000_000L);

    private final WalletRepository walletRepository;

    @Transactional
    public CreateWalletResponse createWallet(CreateWalletRequest request) {
        boolean isWalletExists = walletRepository.findTopByUserId(request.userId()).isPresent();
        if (isWalletExists) {
            throw new RuntimeException("Wallet already exists");
        }

        final Wallet wallet = walletRepository.save(new Wallet(request.userId()));
        return new CreateWalletResponse(wallet.getId(), wallet.getUserId(), wallet.getBalance());
    }

    @Transactional
    public FindWalletResponse findWalletByUserId(Long userId) {
        return walletRepository.findTopByUserId(userId)
                .map(wallet -> new FindWalletResponse(
                        wallet.getId(),
                        wallet.getUserId(),
                        wallet.getBalance(),
                        wallet.getCreatedAt(),
                        wallet.getUpdatedAt()
                ))
                .orElse(null);
    }

    public FindWalletResponse findWalletByWalletId(Long walletId) {
        return walletRepository.findById(walletId)
                .map(wallet -> new FindWalletResponse(
                        wallet.getId(), wallet.getUserId(), wallet.getBalance(),
                        wallet.getCreatedAt(), wallet.getUpdatedAt()
                ))
                .orElse(null);
    }

    @Transactional
    public AddWalletBalanceResponse addBalance(AddWalletBalanceRequest request) {
        final Wallet wallet = walletRepository.findById(request.walletId()).orElseThrow(
                () -> new RuntimeException("Wallet not found")
        );

        BigDecimal balance = wallet.getBalance();
        balance = balance.add(request.amount());

        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Invalid balance. Balance should be greater than zero");
        }
        if (BALANCE_LIMIT.compareTo(balance) < 0) {
            throw new RuntimeException("Invalid balance. Balance should be less than 10,000,000,000");
        }

        wallet.setBalance(balance);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        return new AddWalletBalanceResponse(
                wallet.getId(), wallet.getUserId(), wallet.getBalance(),
                wallet.getCreatedAt(), wallet.getUpdatedAt()
        );
    }
}
