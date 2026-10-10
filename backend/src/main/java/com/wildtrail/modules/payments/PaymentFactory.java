package com.wildtrail.modules.payments;

import com.wildtrail.exception.ApiException;
import java.util.Map;

public final class PaymentFactory {
    private PaymentFactory() {
    }

    public static Payment create(String method, double amount, Map<String, Object> details) {
        if ("CASH".equals(method)) {
            return new CashPayment(amount);
        }
        if ("CARD".equals(method)) {
            if (Boolean.TRUE.equals(details.get("cardDemo"))) {
                for (String key : new String[]{"cardHolder", "cardNumber", "cardExpiry", "cardCvv"}) {
                    if (!value(details, key).isBlank()) throw ApiException.badRequest("Card demo does not accept card details");
                }
                return new DemoCardPayment(amount);
            }
            return new CardPayment(amount, PaymentFactory.value(details, "cardHolder"), PaymentFactory.value(details, "cardNumber"), PaymentFactory.value(details, "cardExpiry"), PaymentFactory.value(details, "cardCvv"));
        }
        throw ApiException.badRequest("Unsupported payment method: " + method);
    }

    private static String value(Map<String, Object> details, String key) {
        Object value = details.get(key);
        return value == null ? "" : value.toString();
    }
}
