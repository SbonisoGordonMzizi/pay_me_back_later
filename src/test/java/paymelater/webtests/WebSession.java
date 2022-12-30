package paymelater.webtests;

/*
 ** DO NOT CHANGE!!
 */

import org.jetbrains.annotations.NotNull;
import paymelater.model.Expense;
import paymelater.model.PaymentRequest;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.server.ServiceRegistry;
import paymelater.webtests.pages.*;

import javax.money.MonetaryAmount;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This has turned out to be an UGLY! monster of a class.
 * It should be refactored!!
 * Anyway, it manages a single web session for the web tests.
 * It also has a whole bunch of convenient methods for checking what's on pages, etc.
 *
 * NOTE: Code folding regions have been created for easier navigation.
 */
class WebSession {
    protected final WebTestRunner testRunner;
    protected final Map<String, ExpenseOnPage> expensesOnPage = new HashMap<>();
    protected final Map<String, PaymentRequestOnPage> paymentRequestsOnPage = new HashMap<>();
    protected LoginPage loginPage;
    protected ExpensesPage expensesPage;
    protected PaymentRequestsSentPage paymentRequestsSentPage;
    protected ExpenseForm expensePage;
    protected String email;
    protected PaymentRequestForm paymentRequestPage;
    private PaymentRequestsReceivedPage paymentRequestsReceivedPage;

    //region General

    /**
     * Setup the session.
     * @param testRunner The test runner for this session
     */
    public WebSession(WebTestRunner testRunner) {
        this.testRunner = testRunner;
        loginPage = new LoginPage(testRunner);
    }

    /**
     * Take a screenshot
     * @param path the filename of the screenshot
     * @return This web session
     * @throws IOException The screenshot could not be saved
     */
    public WebSession takeScreenshot(String path) throws IOException {
        testRunner.takeScreenshot(path);
        return this;
    }

    /**
     * Open a page
     * @param page The page to open
     * @return This web session
     */
    public WebSession open(AbstractPage page) {
        page.open();
        return this;
    }
    //endregion

    //region Login and logout

    /**
     * Open the login page
     * @return this web session
     */
    public WebSession openLoginPage() {
        loginPage.open();
        return this;
    }

    /**
     * Login to the app
     * @param email the email of the user logging in
     * @return this web session
     */
    public WebSession login(String email) {
        this.email = email;
        expensesPage = loginPage.loginUser(email);
        return this;
    }

    /**
     * Check that the browser is on the login page
     * @return this web session
     */
    public WebSession shouldBeOnLoginPage() {
        testRunner.shouldBeOnPage(loginPage);
        return this;
    }

    /**
     * Logout
     * @return this web session
     */
    public WebSession logout() {
        loginPage = expensesPage.logout();
        return this;
    }
    //endregion

    //region PaymentRequestsReceived page

    /**
     * Check that there aren't any payments received
     * @return this web session
     */
    public WebSession shouldHaveNoPaymentsRequestReceived() {
        assertThat(paymentRequestsReceivedPage.haveNoPaymentRequestsReceived()).isTrue();
        return this;
    }
    //endregion

    //region Expense form

    /**
     * Check that the browser is on the page to capture an expense
     * @return this web session
     */
    public WebSession shouldBeOnCaptureExpensePage() {
        testRunner.shouldBeOnPage(expensePage);
        return this;
    }

    /**
     * Fill out the expense form
     * @param description the description of the expense
     * @param amount the amount
     * @param date the date
     * @return this web session
     */
    public WebSession fillExpenseForm(String description, MonetaryAmount amount, LocalDate date) {
        expensePage.fillExpenseForm(description, amount, date);
        return this;
    }

    /**
     * Submit the expense form
     * @return this web session
     */
    public WebSession submitExpenseForm() {
        expensesPage = expensePage.submitExpenseForm();
        return this;
    }
    //endregion

    //region Expenses page
    /**
     * The expenses page should have the email address displayed
     * @return this web session
     */
    public WebSession shouldHaveEmailAddressDisplayed() {
        assertThat(expensesPage.userEmail()).isEqualTo(email);
        return this;
    }

    /**
     * the browser should be on the expenses page
     * @return this web session
     */
    public WebSession shouldBeOnExpensesPage() {
        refreshExpensesOnPage();
        testRunner.shouldBeOnPage(expensesPage);
        return this;
    }

    /**
     * The page should have the logout link displayed
     * @return this web session
     */
    public WebSession shouldHaveLogoutLinkDisplayed() {
        String name = getNameFromEmail(email);
        assertThat(expensesPage.logoutText()).isEqualTo("Logout " + name);
        return this;
    }

    /**
     * There should not be any expenses
     * @return this web session
     */
    public WebSession shouldHaveNoExpenses() {
        assertThat(expensesPage.hasNoExpenses()).isTrue();
        return this;
    }

    /**
     * There should be an expense with a specific description
     * @param expenseDescription the description of the expense
     * @return this web session
     */
    public WebSession shouldHaveExpense(String expenseDescription) {
        ExpenseOnPage e = findExpenseOnPage(expenseDescription);
        verifyExpense(e.row, e.description, e.amount, e.date);
        return this;
    }

    /**
     * This method is used to refresh expenses for the person logged in.
     * It is used to manage state between page transitions.
     * Bit ugly, but it works.
     */
    public void refreshExpensesOnPage() {
        Person p = ServiceRegistry.lookup(PersonDAO.class).findPersonByEmail(email).orElseThrow();
        Collection<Expense> expenses = ServiceRegistry.lookup(ExpenseDAO.class).findExpensesForPerson(p);
        this.expensesOnPage.clear();
        for (Expense e : expenses) {
            addAnExpenseOnPage(new ExpenseOnPage(e));
        }
    }

    /**
     * chekc the grand total on the expenses page
     * @param amount The amount expected
     * @return this web session
     */
    public WebSession expensesGrandTotalShouldBe(MonetaryAmount amount) {
        assertThat(expensesPage.grandTotal()).isEqualTo(amount);
        return this;
    }

    /**
     * Click on the link to capture a new expense
     * @return this web session
     */
    public WebSession clickOnCaptureExpense() {
        expensePage = expensesPage.captureExpense();
        return this;
    }

    /**
     * Click on the link for a specific expense
     * @param description the description of the expense
     * @return this session
     */
    public WebSession clickOnExpense(String description) {
        ExpenseOnPage e = findExpenseOnPage(description);
        paymentRequestPage = expensesPage.clickOnExpense(e.row);
        return this;
    }

    private void verifyExpense(String row, String description, MonetaryAmount amount, LocalDate date) {
        assertThat(expensesPage.expenseDescription(row)).isEqualTo(description);
        assertThat(expensesPage.expenseAmount(row)).isEqualTo(amount.toString());
        assertThat(expensesPage.expenseDate(row)).isEqualTo(DateTimeFormatter.ISO_LOCAL_DATE.format(date));
    }

    @NotNull
    private String getNameFromEmail(String email) {
        String namePart = email.split("@")[0];
        return namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
    }

    private ExpenseOnPage findExpenseOnPage(String description) {
        return expensesOnPage.get(description);
    }

    private void addAnExpenseOnPage(ExpenseOnPage e) {
        expensesOnPage.put(e.description, e);
    }

    private void refreshPaymentRequestsOnPage(ExpenseOnPage expenseOnPage) {
        Person p = ServiceRegistry.lookup(PersonDAO.class)
                .findPersonByEmail(email)
                .orElseThrow();
        Collection<Expense> expenses = ServiceRegistry.lookup(ExpenseDAO.class).findExpensesForPerson(p);
        Expense lunch = expenses.stream()
                .filter(e -> e.getDescription().equals(expenseOnPage.description))
                .findFirst()
                .orElseThrow();
        paymentRequestsOnPage.clear();
        lunch.listOfPaymentRequests().stream().forEach(this::addAPaymentRequestOnPage);
    }
    //endregion

    //region PaymentRequest form

    /**
     * Click on the link for the payment requests page
     * @return this web session
     */
    public WebSession clickOnExpensesLinkOnPaymentRequestPage() {
        expensesPage = paymentRequestPage.expenses();
        return this;
    }

    /**
     * there should be a payment request on the page
     * @param name the name of the person
     * @return this web session
     */
    public WebSession shouldHavePaymentRequest(String name) {
        PaymentRequestOnPage p = findPaymentRequestOnPage(name);
        verifyPaymentRequest(p);
        return this;
    }

    /**
     * Fill the payment request form
     * @param email The email of the person
     * @param amount The amount
     * @param date The due date
     * @return this web session
     */
    public WebSession fillPaymentRequestForm(String email, MonetaryAmount amount, LocalDate date) {
        paymentRequestPage.fillForm(email, amount, date);
        return this;
    }

    /**
     * Submit the payment request form
     * @return this web session
     */
    public WebSession submitPaymentRequestForm() {
        paymentRequestPage = paymentRequestPage.submitForm();
        return this;
    }

    /**
     * The browser should be on the payment request page for a specific expense
     * @param description the description of the expense
     * @return this web session
     */
    public WebSession shouldBeOnPaymentRequestPage(String description) {
        testRunner.shouldBeOnPage(paymentRequestPage);
        ExpenseOnPage e = findExpenseOnPage(description);

        assertThat(paymentRequestPage.expenseDescription()).isEqualTo(e.description);
        assertThat(paymentRequestPage.expenseDate()).isEqualTo(e.date);
        assertThat(paymentRequestPage.expenseAmount()).isEqualTo(e.amount);
        refreshPaymentRequestsOnPage(e);
        return this;
    }

    private void addAPaymentRequestOnPage(PaymentRequest p) {
        PaymentRequestOnPage paymentRequestOnPage = new PaymentRequestOnPage(p);
        paymentRequestsOnPage.put(paymentRequestOnPage.name, paymentRequestOnPage);
    }

    private void verifyPaymentRequest(PaymentRequestOnPage p) {
        assertThat(paymentRequestPage.priorPaymentRequestName(p.row)).isEqualTo(p.name);
        assertThat(paymentRequestPage.priorPaymentRequestAmount(p.row)).isEqualTo(p.amount);
        assertThat(paymentRequestPage.priorPaymentRequestDate(p.row)).isEqualTo(p.date);
    }

    private PaymentRequestOnPage findPaymentRequestOnPage(String name) {
        return paymentRequestsOnPage.get(name);
    }
    //endregion

    //region PaymentRequestsSent page
    /**
     * Check that the browser is on the PaymentRequests sent page
     * @return this web session
     */
    public WebSession shouldBeOnPaymentRequestsSentPage() {
        testRunner.shouldBeOnPage(paymentRequestsSentPage);
        return this;
    }

    /**
     * There should be no payment requests sent
     * @return this web session
     */
    public WebSession shouldHaveNoPaymentRequestsSent() {
        assertThat(paymentRequestsSentPage.haveNoPaymentRequestsSent()).isTrue();
        return this;
    }

    /**
     * Click on the payment requests sent link
     * @return this web session
     */
    public WebSession clickOnPaymentRequestsSent() {
        paymentRequestsSentPage = expensesPage.viewPaymentRequestsSent();
        return this;
    }

    /**
     * Check the grand total on the payment requests sent page
     * @param amount the expected amount
     * @return this web session
     */
    public WebSession paymentRequestsSentGrandTotalShouldBe(MonetaryAmount amount) {
        assertThat(paymentRequestsSentPage.grandTotal()).isEqualTo(amount);
        return this;
    }
    //endregion

    //region PaymentRequestsReceived page
    /**
     * The browser should be on the payment requests received page
     * @return this web session
     */
    public WebSession shouldBeOnPaymentRequestsReceivedPage() {
        testRunner.shouldBeOnPage(paymentRequestsReceivedPage);
        return this;
    }

    /**
     * There should be no payment requests received
     * @return this web session
     */
    public WebSession shouldHaveNoPaymentRequestsReceived() {
        assertThat(paymentRequestsReceivedPage.haveNoPaymentRequestsReceived()).isTrue();
        return this;
    }

    /**
     * Click on the payment requests received link
     * @return this web session
     */
    public WebSession clickOnPaymentRequestsReceived() {
        paymentRequestsReceivedPage = expensesPage.viewPaymentRequestsReceived();
        return this;
    }

    /**
     * Check the grand total on the payment requests received page
     * @param amount the expected amount
     * @return this web session
     */
    public WebSession paymentRequestsReceivedGrandTotalShouldBe(MonetaryAmount amount) {
        assertThat(paymentRequestsReceivedPage.grandTotal()).isEqualTo(amount);
        return this;
    }

    /**
     * Pay the payment request
     * @return this web session
     */
    public WebSession payPaymentRequest() {
        paymentRequestsReceivedPage = paymentRequestsReceivedPage.submitPaymentForm();
        return this;
    }

    /**
     * Check that the payment request has been paid
     * @return this web session
     */
    public WebSession paymentRequestsReceivedShouldBePaid() {
        assertThat(paymentRequestsReceivedPage.paymentRequestPaid()).isTrue();
        return this;
    }

    /**
     * Click on the payment requests received link
     * @return this web session
     */
    public WebSession clickOnExpensesLinkOnPaymentRequestsReceivedPage() {
        expensesPage = paymentRequestsReceivedPage.expenses();
        return this;
    }

    //endregion

    //region Rudimentary view model classes
    private class ExpenseOnPage {
        public final String row;
        public final String description;
        public final MonetaryAmount amount;
        public final LocalDate date;

        public ExpenseOnPage(Expense expense) {
            this.row = expense.getId().toString();
            this.description = expense.getDescription();
            this.date = expense.getDate();
            this.amount = expense.getAmount();
        }
    }

    private class PaymentRequestOnPage {
        public final String row;
        public final String name;
        public final MonetaryAmount amount;
        public final LocalDate date;

        public PaymentRequestOnPage(PaymentRequest p) {
            this.row = p.getId().toString();
            this.name = p.getPersonWhoShouldPayBack().getName();
            this.amount = p.getAmountToPay();
            this.date = p.getDueDate();
        }
    }
    //endregion
}
