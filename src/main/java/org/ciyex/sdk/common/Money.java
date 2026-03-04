package org.ciyex.sdk.common;

import java.math.BigDecimal;

/**
 * Immutable monetary amount with currency.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null) amount = BigDecimal.ZERO;
        if (currency == null || currency.isBlank()) currency = "USD";
    }

    public static Money usd(BigDecimal amount) {
        return new Money(amount, "USD");
    }

    public static Money usd(String amount) {
        return new Money(new BigDecimal(amount), "USD");
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, "USD");
    }
}
