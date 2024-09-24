package com.example.demopaymentsystem.processing

import com.example.demopaymentsystem.checkout.ConfirmRequest
import com.example.demopaymentsystem.external.PaymentGatewayService
import com.example.demopaymentsystem.order.Order
import com.example.demopaymentsystem.order.OrderRepository
import com.example.demopaymentsystem.transaction.TransactionService
import spock.lang.Specification

class PaymentProcessingServiceSpockTest extends Specification {
    PaymentProcessingService paymentProcessingService
    PaymentGatewayService paymentGatewayService = Mock()
    TransactionService transactionService = Mock()
    OrderRepository orderRepository = Mock()

    def setup() {
        paymentProcessingService = new PaymentProcessingService(
                paymentGatewayService, transactionService, orderRepository
        )
    }

    def "PG 결제 성공 시 결제 기록이 잘 생성된다"() {
        given:
        def confirmRequest = new ConfirmRequest(
                "paymentKey",
                "orderId",
                "1000"
        )

        // mock
        Order order = new Order()
        orderRepository.findByRequestId(confirmRequest.orderId()) >> order

        when:
        paymentProcessingService.createPayment(confirmRequest)

        then:
        1 * paymentGatewayService.confirm(confirmRequest)
        1 * transactionService.pgPayment()
        1 * orderRepository.save(order)
    }
}
