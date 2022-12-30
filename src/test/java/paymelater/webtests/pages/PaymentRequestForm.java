package paymelater.webtests.pages;

/*
 ** DO NOT CHANGE!!
 */


import org.openqa.selenium.By;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.function.Function;

public class PaymentRequestForm extends AbstractPage {
    private final String path;
    private final By EXPENSE_DESCRIPTION = By.id("expense_description");
    private final By EXPENSE_DATE = By.id("expense_date");
    private final By EXPENSE_AMOUNT = By.id("expense_amount");
    private final By HOME_LINK = By.id("expenses");
    private final By EMAIL_FIELD = By.id("email");
    private final By AMOUNT_FIELD = By.id("amount");
    private final By DATE_FIELD = By.id("due_date");

    private final Function<String, By> priorPaymentRequestNameRow = rowLocator().apply("paymentrequest_who");
    private final Function<String, By> priorPaymentRequestAmountRow = rowLocator().apply("paymentrequest_amount");
    private final Function<String, By> priorPaymentRequestDateRow = rowLocator().apply("paymentrequest_date");

    public PaymentRequestForm(WebTestRunner testRunner, String path) {
        super(testRunner);
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }

    public String expenseDescription() {
        return textOf(EXPENSE_DESCRIPTION);
    }

    public LocalDate expenseDate() {
        return localDateOf(EXPENSE_DATE);
    }

    public MonetaryAmount expenseAmount() {
        return amountOf(EXPENSE_AMOUNT);
    }

    public ExpensesPage expenses() {
        click(HOME_LINK);
        return new ExpensesPage(testRunner);
    }

    public String priorPaymentRequestName(String row) {
        return textOf(priorPaymentRequestNameRow.apply(row));
    }

    public MonetaryAmount priorPaymentRequestAmount(String row) {
        return amountOf(priorPaymentRequestAmountRow.apply(row));
    }

    public LocalDate priorPaymentRequestDate(String row) {
        return localDateOf(priorPaymentRequestDateRow.apply(row));
    }

    public void fillForm(String email, MonetaryAmount amount, LocalDate date) {
        fillText(EMAIL_FIELD, email);
        fillAmount(AMOUNT_FIELD, amount);
        fillDate(DATE_FIELD, date);
    }

    public PaymentRequestForm submitForm() {
        submit();
        return this;
    }
}
