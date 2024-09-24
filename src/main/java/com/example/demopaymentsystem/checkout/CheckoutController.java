package com.example.demopaymentsystem.checkout;

import com.example.demopaymentsystem.order.Order;
import com.example.demopaymentsystem.order.OrderRepository;
import com.example.demopaymentsystem.processing.PaymentProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

@Slf4j
@Controller
public class CheckoutController {

    private final OrderRepository orderRepository;
    private final PaymentProcessingService paymentProcessingService;

    public CheckoutController(OrderRepository orderRepository, PaymentProcessingService paymentProcessingService) {
        this.orderRepository = orderRepository;
        this.paymentProcessingService = paymentProcessingService;
    }

    @GetMapping(value = "/checkout")
    public String checkout() {
        return "/checkout";
    }

    @GetMapping(value = "/success")
    public String success() {
        return "/success";
    }

    @GetMapping(value = "/fail")
    public String fail() {
        return "/fail";
    }

    @PostMapping(value = "/confirm")
    public ResponseEntity<Object> confirmPayment(@RequestBody ConfirmRequest confirmRequest) throws Exception {

        // 주문 서비스
        // 생성한 주문의 상태 변경 (WAIT -> REQUESTED)
        Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(Order.Status.REQUESTED);
        orderRepository.save(order);

        // 주문 서비스 -> 결제 서비스
        // 해당 주문에 대한 결제 승인 요청
        paymentProcessingService.createPayment(confirmRequest);

        // 결제 서비스 -> PG
        // PG 승인 요청

        // 결제 서비스
        // 결제 기록 저장

        // 결제 서비스 -> 주문 서비스
        // 응답

        // 주문 서비스
        // 주문의 상태 변경 (REQUESTED -> APPROVED)

        return ResponseEntity.ok(order);
    }


}