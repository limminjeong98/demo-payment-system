package com.example.demopaymentsystem.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OrderController {

    private final OrderRepository orderRepository;

    @GetMapping(value = "/order")
    public String order(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") Long amount,
            @RequestParam("courseId") Long courseId,
            @RequestParam("courseName") String courseName,
            Model model
    ) {

        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(new BigDecimal(amount));
        order.setRequestId(UUID.randomUUID().toString());
        order.setCourseId(courseId);
        order.setCourseName(courseName);
        order.setStatus(Order.Status.WAIT);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        model.addAttribute("courseName", courseName);
        model.addAttribute("requestId", order.getRequestId());
        model.addAttribute("amount", order.getAmount().toString());
        model.addAttribute("customerKey", "customerKey-" + userId);
        return "/order";
    }
}
