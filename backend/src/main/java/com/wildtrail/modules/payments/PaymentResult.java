package com.wildtrail.modules.payments;

import java.time.Instant;

public record PaymentResult(String method, String status, double amount, String transactionRef, String cardLast4, Instant paidAt, String message) {
}
