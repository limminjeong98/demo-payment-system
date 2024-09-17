package com.example.demopaymentsystem.wallet

import spock.lang.Specification

import java.time.LocalDateTime

class WalletServiceSpockTest extends Specification {
    WalletService walletService
    WalletRepository walletRepository = Mock()

    def setup() {
        walletService = new WalletService(walletRepository)
    }

    def "지갑을 생성한다 - 지갑을 갖고 있지 않다면 정상적으로 생성된다"() {
        given:
        Long userId = 1L
        CreateWalletRequest request = new CreateWalletRequest(userId)
        walletRepository.findTopByUserId(userId) >> Optional.empty()

        when:
        def createdWallet = walletService.createWallet(request)

        then:
        1 * walletRepository.save(_) >> new Wallet(userId)
        createdWallet != null
        createdWallet.balance() == BigDecimal.ZERO
        print createdWallet
    }

    def "지갑 생성한다 - 지갑을 이미 갖고 있다면 오류를 응답한다"() {
        given:
        Long userId = 1L
        CreateWalletRequest request = new CreateWalletRequest(userId)
        walletRepository.findTopByUserId(userId) >> Optional.of(new Wallet(1L))

        when:
        walletService.createWallet(request)

        then:
        def ex = thrown(RuntimeException)
        ex != null
        // ex.printStackTrace()
    }

    def "지갑을 조회한다 - 생성되어 있는 경우"() {
        given:
        Long userId = 1L
        def wallet = new Wallet(userId)
        wallet.balance = new BigDecimal(1000)
        walletRepository.findTopByUserId(userId) >> Optional.of(wallet)

        when:
        def result = walletService.findWalletByUserId(userId)

        then:
        result != null
        result.balance() == new BigDecimal(1000)
    }

    def "지갑을 조회한다 - 생성되어 있지 않은 경우"() {
        given:
        Long userId = 1L
        walletRepository.findTopByUserId(userId) >> Optional.empty()

        when:
        def result = walletService.findWalletByUserId(userId)

        then:
        result == null
    }

    def "지갑에 잔액을 충전한다 - 지갑이 존재하고, 잔액이 충분하면 업데이트된다"() {
        given:
        Long walletId = 1L
        def initialBalance = new BigDecimal(200)
        def addAmount = new BigDecimal(100)
        var wallet = new Wallet(walletId, 1L, initialBalance, LocalDateTime.now(), LocalDateTime.now())
        walletRepository.findById(walletId) >> Optional.of(wallet)

        when:
        def result = walletService.addBalance(new AddWalletBalanceRequest(walletId, addAmount))

        then:
        result.balance() == new BigDecimal(300)
    }

    def "지갑에 잔액을 충전한다 - 지갑이 존재하지 않으면 예외가 발생한다"() {
        given:
        Long walletId = 1L
        def addAmount = new BigDecimal("100.00")
        walletRepository.findById(walletId) >> Optional.empty()

        when:
        walletService.addBalance(new AddWalletBalanceRequest(walletId, addAmount))

        then:
        def ex = thrown(RuntimeException)
        ex != null
    }

    def "지갑에 잔액을 충전한다 - 충전 후 금액이 0원 미만이면 예외가 발생한다"() {
        given:
        def walletId = 1L
        def addAmount = new BigDecimal("-101.00")
        def initialBalance = new BigDecimal("100.00")
        def wallet = new Wallet(walletId, 1L, initialBalance, LocalDateTime.now(), LocalDateTime.now())
        walletRepository.findById(walletId) >> Optional.of(wallet)

        when:
        walletService.addBalance(new AddWalletBalanceRequest(walletId, addAmount))

        then:
        def ex = thrown(RuntimeException)
        ex != null
        ex.message == "Invalid balance. Balance should be greater than zero"
    }

    def "지갑에 잔액을 충전한다 - 충전 후 금액이 한도를 초과하면 예외가 발생한다"() {
        given:
        def walletId = 1L
        def addAmount = new BigDecimal(10_000_000_000)
        def initialBalance = new BigDecimal(1)
        def wallet = new Wallet(walletId, 1L, initialBalance, LocalDateTime.now(), LocalDateTime.now())
        walletRepository.findById(walletId) >> Optional.of(wallet)

        when:
        walletService.addBalance(new AddWalletBalanceRequest(walletId, addAmount))

        then:
        def ex = thrown(RuntimeException)
        ex != null
        ex.message == "Invalid balance. Balance should be less than 10,000,000,000"
    }
}
