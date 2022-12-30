package paymelater.webtests;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import paymelater.model.Expense;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.server.ServiceRegistry;

import java.io.IOException;
import java.util.stream.Stream;

import static paymelater.model.DateHelper.TODAY;
import static paymelater.model.DateHelper.TOMORROW;
import static paymelater.model.MoneyHelper.amountOf;


@DisplayName("For an expense")
public class PaymentRequestFormTests extends WebTestRunner {

    private final WebSession session = new WebSession(PaymentRequestFormTests.this);

    @Override
    protected void setupTestData() {
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Person p1 = new Person("student1@wethinkcode.co.za");
        Person p2 = new Person("student2@wethinkcode.co.za");
        Person p3 = new Person("student3@wethinkcode.co.za");
        Stream.of(p1, p2, p3).forEach(personDAO::savePerson);

        Expense expense1 = new Expense(p1, "Lunch", amountOf(300), TODAY);
        Expense expense2 = new Expense(p1, "Airtime", amountOf(100), TODAY);
        expense1.requestPayment(p2, amountOf(100), TOMORROW);
        Stream.of(expense1, expense2).forEach(expenseDAO::save);
    }

    @Test
    @DisplayName("I can view the page to capture a payment request")
    public void noPaymentRequests() throws IOException {
        session.openLoginPage()
                .login("student1@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .clickOnExpense("Airtime")
                .shouldBeOnPaymentRequestPage("Airtime")
                .takeScreenshot("payment-request")
                .clickOnExpensesLinkOnPaymentRequestPage()
                .shouldBeOnExpensesPage();
    }

    @Test
    @DisplayName("I can see prior payment requests")
    public void hasPriorPaymentRequests() throws IOException {
        session.openLoginPage()
                .login("student1@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .clickOnExpense("Lunch")
                .shouldBeOnPaymentRequestPage("Lunch")
                .takeScreenshot("payment-request")
                .shouldHavePaymentRequest("Student2")
                .clickOnExpensesLinkOnPaymentRequestPage()
                .shouldBeOnExpensesPage();
    }

    @Test
    @DisplayName("I can submit a payment request")
    public void capturePaymentRequest() throws IOException {
        session.openLoginPage()
                .login("student1@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .clickOnExpense("Lunch")
                .shouldBeOnPaymentRequestPage("Lunch")
                .takeScreenshot("payment-request-before-capture")
                .shouldHavePaymentRequest("Student2")
                .fillPaymentRequestForm("student3@wethinkcode.co.za", amountOf(100), TOMORROW)
                .submitPaymentRequestForm()
                .takeScreenshot("payment-request-form-filled")
                .shouldBeOnPaymentRequestPage("Lunch")
                .shouldHavePaymentRequest("Student2")
                .shouldHavePaymentRequest("Student3")
                .takeScreenshot("payment-request-after-capture");
    }
}
