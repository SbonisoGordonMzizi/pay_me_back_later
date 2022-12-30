package paymelater.webtests.pages;

/*
 ** DO NOT CHANGE!!
 */


import org.openqa.selenium.By;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;

public class PaymentRequestsSentPage extends AbstractPage {
    private final By NO_PAYMENT_REQUESTS_SENT = By.id("no_payment_requests_sent");
    private final By GRAND_TOTAL = By.id("grand_total");

    public PaymentRequestsSentPage(WebTestRunner testRunner) {
        super(testRunner);
    }

    @Override
    public String path() {
        return "/paymentrequests_sent";
    }

    public boolean haveNoPaymentRequestsSent() {
        return textOf(NO_PAYMENT_REQUESTS_SENT).equals("Nobody owes you anything!");
    }

    public MonetaryAmount grandTotal() {
        return amountOf(GRAND_TOTAL);
    }

}
