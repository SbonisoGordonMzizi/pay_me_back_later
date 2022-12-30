package paymelater.webtests.pages;

import org.openqa.selenium.By;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.function.Function;

public class ExpensesPage extends AbstractPage {
    public static final String PAGE_PATH = "/expenses";
    private final By LOGGED_IN_USER = By.id("user");
    private final By LOGOUT_LINK = By.id("logout");
    private final By NO_EXPENSES_SECTION = By.id("no_expenses");
    private final By GRAND_TOTAL = By.id("grand_total");
    private final By ADD_EXPENSE_LINK = By.id("add_expense");
    private final By PAYMENT_REQUESTS_SENT_LINK = By.id("paymentrequests_sent");
    private final By PAYMENT_REQUESTS_RECEIVED_LINK = By.id("paymentrequests_received");
    private final Function<String, By> EXPENSE_DESCRIPTION = rowLocator().apply("payment_request");
    private final Function<String, By> EXPENSE_AMOUNT = rowLocator().apply("nett");
    private final Function<String, By> EXPENSE_DATE = rowLocator().apply("date");

    public ExpensesPage(WebTestRunner testRunner) {
        super(testRunner);
    }

    @Override
    public String path() {
        return PAGE_PATH;
    }

    public String userEmail() {
        return driver.findElement(LOGGED_IN_USER).getText();
    }

    public String logoutText() {
        return driver.findElement(LOGOUT_LINK).getText();
    }

    public LoginPage logout() {
        click(LOGOUT_LINK);
        return new LoginPage(testRunner);
    }

    public boolean hasNoExpenses() {
        return textOf(NO_EXPENSES_SECTION).equals("You don't have any expenses!");
    }

    public String expenseDescription(String row) {
        return textOf(EXPENSE_DESCRIPTION.apply(row));
    }

    public String expenseAmount(String row) {
        return textOf(EXPENSE_AMOUNT.apply(row));
    }

    public LocalDate expenseDate(String row) {
        return localDateOf(EXPENSE_DATE.apply(row));
    }

    public MonetaryAmount grandTotal() {
        return amountOf(GRAND_TOTAL);
    }

    public ExpenseForm captureExpense() {
        click(ADD_EXPENSE_LINK);
        return new ExpenseForm(testRunner);
    }

    public PaymentRequestsSentPage viewPaymentRequestsSent() {
        click(PAYMENT_REQUESTS_SENT_LINK);
        return new PaymentRequestsSentPage(testRunner);
    }

    public PaymentRequestsReceivedPage viewPaymentRequestsReceived() {
        click(PAYMENT_REQUESTS_RECEIVED_LINK);
        return new PaymentRequestsReceivedPage(testRunner);
    }

    public PaymentRequestForm clickOnExpense(String row) {
        By paymentRequestLink = EXPENSE_DESCRIPTION.apply(row);
        String href = hrefOf(paymentRequestLink);
        String[] parts = href.split("/");
        String path = "/" + parts[parts.length - 1];
        click(paymentRequestLink);
        return new PaymentRequestForm(testRunner, path);
    }
}
