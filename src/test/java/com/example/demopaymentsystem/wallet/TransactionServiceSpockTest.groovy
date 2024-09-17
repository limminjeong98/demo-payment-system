package com.example.demopaymentsystem.wallet

import com.example.demopaymentsystem.transaction.ChargeTransactionRequest
import com.example.demopaymentsystem.transaction.ChargeTransactionResponse
import com.example.demopaymentsystem.transaction.PaymentTransactionRequest
import com.example.demopaymentsystem.transaction.PaymentTransactionResponse
import com.example.demopaymentsystem.transaction.Transaction
import com.example.demopaymentsystem.transaction.TransactionRepository
import com.example.demopaymentsystem.transaction.TransactionService
import spock.lang.Specification

import java.time.LocalDateTime

class TransactionServiceSpockTest extends Specification {
    TransactionService transactionService
    WalletService walletService = Mock()
    TransactionRepository transactionRepository = Mock()

    def setup() {
        transactionService = new TransactionService(walletService, transactionRepository)
    }

    def "충전 거래 성공 - 지갑을 갖고 있다면 정상적으로 충전 거래 생성"() {
        given:
        Long userId = 1L
        String orderId = "order_id"
        ChargeTransactionRequest request = new ChargeTransactionRequest(userId, orderId, BigDecimal.TEN)
        transactionRepository.findTransactionByOrderId(orderId) >> Optional.empty()

        Long walletId = 1L
        def findWalletResponse = new FindWalletResponse(walletId, userId, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByUserId(userId) >> findWalletResponse

        def addWalletBalanceResponse = new AddWalletBalanceResponse(
                walletId, userId, findWalletResponse.balance().add(request.amount()), LocalDateTime.now(), LocalDateTime.now()
        )
        walletService.addBalance(_) >> addWalletBalanceResponse

        when:
        def charge = transactionService.charge(request)

        then:
        1 * transactionRepository.save(_)
        charge != null
        print charge
    }

    def "충전 거래 실패 - 지갑을 갖고 있지 않다면 충전 거래 생성 실패"() {
        given:
        Long userId = 1L
        String orderId = "order_id"
        ChargeTransactionRequest request = new ChargeTransactionRequest(userId, orderId, BigDecimal.TEN)

        walletService.findWalletByUserId(userId) >> null

        when:
        def charge = transactionService.charge(request)

        then:
        def ex = thrown(RuntimeException)
        ex != null
        ex.message == "Wallet not found"
    }

    def "충전 거래 실패 - 이미 충전되었다면 충전 거래 재생성하지 않는다"() {
        given:
        Long userId = 1L
        String orderId = "order_id"
        ChargeTransactionRequest request = new ChargeTransactionRequest(userId, orderId, BigDecimal.TEN)
        transactionRepository.findTransactionByOrderId(orderId) >> Optional.of(new Transaction())

        Long walletId = 1L
        def findWalletResponse = new FindWalletResponse(walletId, userId, BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByUserId(userId) >> findWalletResponse

        when:
        def charge = transactionService.charge(request)

        then:
        def ex = thrown(RuntimeException)
        ex != null
        ex.message == "Transaction already exists"
    }

    def "결제 거래 성공"() {
        given:
        Long userId = 1L
        String orderId = "order_id"
        Long walletId = 1L
        PaymentTransactionRequest paymentTransactionRequest = new PaymentTransactionRequest(walletId, orderId, BigDecimal.TEN)
        transactionRepository.findTransactionByOrderId(orderId) >> Optional.empty()

        def findWalletResponse = new FindWalletResponse(walletId, userId, new BigDecimal(100), LocalDateTime.now(), LocalDateTime.now())
        walletService.findWalletByWalletId(walletId) >> findWalletResponse

        def addWalletBalanceResponse = new AddWalletBalanceResponse(walletId, userId,
                findWalletResponse.balance().add(paymentTransactionRequest.amount().negate()),
                LocalDateTime.now(), LocalDateTime.now()
        )
        walletService.addBalance(_) >> addWalletBalanceResponse

        when:
        def payment = transactionService.payment(paymentTransactionRequest)

        then:
        1 * transactionRepository.save(_)
        payment != null
        payment.balance() == new BigDecimal(90)
        println payment
    }
}
