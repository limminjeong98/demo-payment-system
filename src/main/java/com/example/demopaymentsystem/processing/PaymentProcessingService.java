package com.example.demopaymentsystem.processing;

import com.example.demopaymentsystem.checkout.ConfirmRequest;
import com.example.demopaymentsystem.external.PaymentGatewayService;
import com.example.demopaymentsystem.order.Order;
import com.example.demopaymentsystem.order.OrderRepository;
import com.example.demopaymentsystem.transaction.ChargeTransactionRequest;
import com.example.demopaymentsystem.transaction.PgPaymentTransactionRequest;
import com.example.demopaymentsystem.transaction.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class PaymentProcessingService {

    private final PaymentGatewayService paymentGatewayService;
    private final TransactionService transactionService;
    private final OrderRepository orderRepository;

    public void createPayment(ConfirmRequest confirmRequest) {
        paymentGatewayService.confirm(confirmRequest);

        final Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        transactionService.pgPayment(new PgPaymentTransactionRequest(
                order.getUserId(), confirmRequest.orderId(), new BigDecimal(confirmRequest.amount())
        ));

        // 결제 서비스 -> 주문 서비스 응답
        approveOrder(confirmRequest.orderId());
    }

    /**
     * 주문 서비스 : 주문의 상태 변경 (REQUESTED -> APPROVED)
     *
     * @param orderId
     */
    public void approveOrder(String orderId) {
        final Order order = orderRepository.findByRequestId(orderId);
        order.setStatus(Order.Status.APPROVED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
