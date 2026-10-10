package com.wildtrail.modules.payments;

public final class CashPayment
extends Payment {
    public CashPayment(double amount) {
        super(amount);
    }

    @Override
    protected String getMethod() {
        return "CASH";
    }

    @Override
    public PaymentResult process() {
        return new PaymentResult("CASH", "PENDING", this.getAmount(), this.transactionRef(), null, null, "Pay in cash to your driver on the safari day");
    }
}
