package paymelater.webtests.pages;

/*
 ** DO NOT CHANGE!!
 */


import org.openqa.selenium.By;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;
import java.util.Objects;

public class PaymentRequestsReceivedPage extends AbstractPage {
    private static final By PAID_INDICATOR = By.id("paid_1");
    private final By NO_PAYMENT_REQUESTS_RECEIVED = By.id("no_payment_requests_received");
    private final By GRAND_TOTAL = By.id("grand_total");
    private final By HOME_LINK = By.id("expenses");

    public PaymentRequestsReceivedPage(WebTestRunner testRunner) {
        super(testRunner);
    }

    @Override
    public String path() {
        return "/paymentrequests_received";
    }

    public boolean haveNoPaymentRequestsReceived() {
        return textOf(NO_PAYMENT_REQUESTS_RECEIVED).equals("You don't owe anyone anything!");
    }

    public MonetaryAmount grandTotal() {
        return amountOf(GRAND_TOTAL);
    }

    public PaymentRequestsReceivedPage submitPaymentForm() {
        submit();
        return this;
    }

    public boolean paymentRequestPaid() {
        return !Objects.isNull(textOf(PAID_INDICATOR));
    }

    public ExpensesPage expenses() {
        click(HOME_LINK);
        return new ExpensesPage(testRunner);
    }
}
