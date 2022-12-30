package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */


import org.javamoney.moneta.Money;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

public class MoneyHelper {
    public static final MonetaryAmount ZERO_RANDS = amountOf(0);

    public static MonetaryAmount amountOf(long amount) {
        return Money.of(amount, Monetary.getCurrency("ZAR"));
    }
}
