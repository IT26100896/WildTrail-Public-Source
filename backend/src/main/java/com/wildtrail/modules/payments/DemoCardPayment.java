package com.wildtrail.modules.payments;

/** Campus simulation that never collects card data or claims a captured payment. */
public final class DemoCardPayment extends Payment {
    public DemoCardPayment(double amount) { super(amount); }
    @Override protected String getMethod() { return "CARD"; }
    @Override public PaymentResult process() {
        return new PaymentResult("CARD", "PENDING", getAmount(), "DEMO-" + java.util.UUID.randomUUID().toString().replace("-", ""), null, null,
                "Card demonstration only. No money charged; payment remains pending.");
    }
}

