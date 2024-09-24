package com.example.demopaymentsystem.pg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Controller
public class WidgetController {

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

    @RequestMapping(value = "/confirm")
    public ResponseEntity<Object> confirmPayment(@RequestBody String jsonBody) throws Exception {

        // JSONParser parser = new JSONParser();
        final JsonNode jsonNode = new ObjectMapper().readTree(jsonBody);
        final ConfirmRequest request = new ConfirmRequest(
                jsonNode.get("paymentKey").asText(),
                jsonNode.get("orderId").asText(),
                jsonNode.get("amount").asText()
        );

        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        RestClient defaultClient = RestClient.create();
        final Object object = defaultClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .headers(httpHeaders -> {
                    httpHeaders.add("Authorization", authorizations);
                    httpHeaders.add("Content-Type", "application/json");
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toEntity(Object.class);

        return ResponseEntity.ok(object);
    }

    public record ConfirmRequest(String paymentKey, String orderId, String amount) {

    }
}