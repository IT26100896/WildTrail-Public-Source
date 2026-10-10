package com.wildtrail.modules.payments;

import com.wildtrail.exception.ApiException;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Payment
implements PaymentProcessor {
    private final double amount;

    protected Payment(double amount) {
        if (!Double.isFinite(amount) || amount < 0.0) {
            throw ApiException.badRequest("Invalid payment amount");
        }
        this.amount = (double)Math.round(amount * 100.0) / 100.0;
    }

    public double getAmount() {
        return this.amount;
    }

    protected abstract String getMethod();

    protected String transactionRef() {
        int random = ThreadLocalRandom.current().nextInt(10000, 100000);
        return "TXN-" + this.getMethod() + "-" + random + System.currentTimeMillis() % 1000L;
    }
}
