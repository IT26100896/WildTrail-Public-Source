package com.wildtrail.modules.payments;

import com.wildtrail.exception.ApiException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class CardPayment
extends Payment {
    private final String holderName;
    private final String cardNumber;
    private final String expiry;
    private final String cvv;

    public CardPayment(double amount, String holderName, String cardNumber, String expiry, String cvv) {
        super(amount);
        this.holderName = holderName == null ? "" : holderName.trim();
        this.cardNumber = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        this.expiry = expiry == null ? "" : expiry.trim();
        this.cvv = cvv == null ? "" : cvv.trim();
    }

    @Override
    protected String getMethod() {
        return "CARD";
    }

    @Override
    public PaymentResult process() {
        YearMonth expiryMonth;
        if (this.holderName.isEmpty()) {
            throw ApiException.badRequest("Card holder name is required");
        }
        if (this.cardNumber.length() < 13 || this.cardNumber.length() > 19 || !this.luhn(this.cardNumber)) {
            throw ApiException.badRequest("Card number is invalid");
        }
        try {
            expiryMonth = YearMonth.parse(this.expiry, DateTimeFormatter.ofPattern("MM/yy"));
        }
        catch (DateTimeParseException exception) {
            throw ApiException.badRequest("Expiry must be in MM/YY format");
        }
        if (expiryMonth.isBefore(YearMonth.now())) {
            throw ApiException.badRequest("Card has expired");
        }
        if (!this.cvv.matches("\\d{3,4}")) {
            throw ApiException.badRequest("CVV is invalid");
        }
        throw ApiException.badRequest("Online card payment is unavailable. Please pay on arrival.");
    }

    private boolean luhn(String digits) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int index = digits.length() - 1; index >= 0; --index) {
            int digit = digits.charAt(index) - 48;
            if (doubleDigit && (digit *= 2) > 9) {
                digit -= 9;
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
