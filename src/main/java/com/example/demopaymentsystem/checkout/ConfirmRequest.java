package com.example.demopaymentsystem.checkout;

public record ConfirmRequest(String paymentKey, String orderId, String amount) {

}