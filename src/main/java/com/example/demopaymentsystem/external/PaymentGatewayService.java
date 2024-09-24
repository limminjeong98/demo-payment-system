package com.example.demopaymentsystem.external;

import com.example.demopaymentsystem.checkout.ConfirmRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@AllArgsConstructor
@Service
public class PaymentGatewayService {

    public static final String CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final Base64.Encoder encoder = Base64.getEncoder();

    /**
     * 주문 서비스 -> 결제 서비스 : 해당 주문에 대한 결제 승인 요청
     * <p>
     * 결제 서비스 -> PG : PG 승인 요청
     *
     * @param confirmRequest
     */
    public void confirm(ConfirmRequest confirmRequest) {
        // 토스페이먼츠 API는 시크릿 키를 사용자 ID로 사용하고, 비밀번호는 사용하지 않습니다.
        // 비밀번호가 없다는 것을 알리기 위해 시크릿 키 뒤에 콜론을 추가합니다.

        byte[] encodedBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        String authorizations = "Basic " + new String(encodedBytes);

        // 결제를 승인하면 결제수단에서 금액이 차감돼요.
        RestClient defaultClient = RestClient.create();
        final ResponseEntity<Object> object = defaultClient.post()
                .uri(CONFIRM_URL)
                .headers(httpHeaders -> {
                    httpHeaders.add("Authorization", authorizations);
                    httpHeaders.add("Content-Type", "application/json");
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(confirmRequest)
                .retrieve()
                .toEntity(Object.class);

        if (object.getStatusCode().isError()) {
            throw new IllegalStateException("결제 요청이 실패했습니다.");
        }

    }
}
