package com.example.demopaymentsystem.processing;

import com.example.demopaymentsystem.checkout.ConfirmRequest;
import com.example.demopaymentsystem.external.PaymentGatewayService;
import com.example.demopaymentsystem.order.Order;
import com.example.demopaymentsystem.order.OrderRepository;
import com.example.demopaymentsystem.transaction.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@AllArgsConstructor
@Service
public class PaymentProcessingService {

    private final PaymentGatewayService paymentGatewayService;
    private final TransactionService transactionService;
    private final OrderRepository orderRepository;

    public void createPayment(ConfirmRequest confirmRequest) {
        paymentGatewayService.confirm(confirmRequest);
        transactionService.pgPayment();

        final Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        order.setStatus(Order.Status.APPROVED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
