package com.example.demopaymentsystem.checkout;

import com.example.demopaymentsystem.order.Order;
import com.example.demopaymentsystem.order.OrderRepository;
import com.example.demopaymentsystem.processing.PaymentProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class ChargeController {

    private final OrderRepository orderRepository;
    private final PaymentProcessingService paymentProcessingService;

    @GetMapping("/charge-order")
    public String chargeOrder(@RequestParam("userId") Long userId,
                              @RequestParam("amount") String amount,
                              Model model) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(new BigDecimal(amount));
        order.setRequestId(UUID.randomUUID().toString());
        order.setStatus(Order.Status.WAIT);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        model.addAttribute("requestId", order.getRequestId());
        model.addAttribute("amount", amount);
        model.addAttribute("customerKey", "customerKey-" + userId);

        return "/charge-order";
    }

    @GetMapping(value = "/charge-success")
    public String success() {
        return "/success";
    }

    @GetMapping(value = "/charge-fail")
    public String fail() {
        return "/fail";
    }

    @GetMapping(value = "/charge-order-requested")
    public String chargeOrderRequested() {
        return "/charge-order-requested";
    }

    @PostMapping(value = "/charge-confirm")
    public ResponseEntity<Object> confirmChargePayment(@RequestBody ConfirmRequest confirmRequest) {

        Order order = orderRepository.findByRequestId(confirmRequest.orderId());
        order.setUpdatedAt(LocalDateTime.now());
        order.setStatus(Order.Status.REQUESTED);
        orderRepository.save(order);

        paymentProcessingService.createChargePayment(confirmRequest);

        return ResponseEntity.ok(order);
    }
}
