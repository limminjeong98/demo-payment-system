package com.example.demopaymentsystem.transaction;

import com.example.demopaymentsystem.wallet.AddWalletBalanceRequest;
import com.example.demopaymentsystem.wallet.AddWalletBalanceResponse;
import com.example.demopaymentsystem.wallet.FindWalletResponse;
import com.example.demopaymentsystem.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public ChargeTransactionResponse charge(ChargeTransactionRequest request) {
        final FindWalletResponse wallet = walletService.findWalletByUserId(request.userId());
        if (wallet == null) {
            throw new RuntimeException("Wallet not found");
        }

        if (transactionRepository.findTransactionByOrderId(request.orderId()).isPresent()) {
            throw new RuntimeException("Transaction already exists");
        }

        final AddWalletBalanceResponse addWalletBalanceResponse = walletService.addBalance(
                new AddWalletBalanceRequest(
                        wallet.id(), request.amount()
                )
        );

        final Transaction transaction = Transaction.createChargeTransaction(
                request.userId(), addWalletBalanceResponse.id(), request.orderId(), request.amount()
        );
        transactionRepository.save(transaction);

        return new ChargeTransactionResponse(addWalletBalanceResponse.id(), addWalletBalanceResponse.balance());
    }

    @Transactional
    public PaymentTransactionResponse payment(PaymentTransactionRequest request) {
        if (transactionRepository.findTransactionByOrderId(request.orderId()).isPresent()) {
            throw new RuntimeException("Transaction already exists");
        }

        final FindWalletResponse wallet = walletService.findWalletByWalletId(request.walletId());

        // 잔액 차감
        final AddWalletBalanceResponse addWalletBalanceResponse = walletService.addBalance(new AddWalletBalanceRequest(
                wallet.id(), request.amount().negate()
        ));

        final Transaction transaction = Transaction.createPaymentTransaction(
                addWalletBalanceResponse.userId(), addWalletBalanceResponse.id(), request.orderId(), request.amount()
        );
        transactionRepository.save(transaction);

        return new PaymentTransactionResponse(addWalletBalanceResponse.id(), addWalletBalanceResponse.balance());
    }

    /**
     * 결제 서비스 : 결제 기록 저장
     *
     * @param request
     */
    @Transactional
    public void pgPayment(PgPaymentTransactionRequest request) {

        final Transaction transaction = Transaction.createPgPaymentTransaction(
                request.userId(), request.orderId(), request.amount()
        );
        transactionRepository.save(transaction);
    }
}
