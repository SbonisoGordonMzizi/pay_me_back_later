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


@DisplayName("For the payment requests sent page")
public class PaymentRequestsSentPageTests extends WebTestRunner {
    private final WebSession session = new WebSession(PaymentRequestsSentPageTests.this);

    @Override
    protected void setupTestData() {
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Person firstTimeUser = new Person("firsttimeuser@wethinkcode.co.za");
        Person student1 = new Person("student1@wethinkcode.co.za");
        Person student2 = new Person("student2@wethinkcode.co.za");
        Person student3 = new Person("student3@wethinkcode.co.za");
        Stream.of(firstTimeUser, student1, student2, student3).forEach(personDAO::savePerson);

        Expense expense1 = new Expense(student1, "Lunch", amountOf(300), TODAY);
        expense1.requestPayment(student2, amountOf(100), TOMORROW);
        expense1.requestPayment(student3, amountOf(100), TOMORROW);
        expenseDAO.save(expense1);
    }

    @Test
    @DisplayName("when I sent no payment requests")
    void noPaymentRequests() throws IOException {
        session.openLoginPage()
                .login("firsttimeuser@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .clickOnPaymentRequestsSent()
                .shouldBeOnPaymentRequestsSentPage()
                .shouldHaveNoPaymentRequestsSent()
                .takeScreenshot("paymentrequests_sent");
    }

    @Test
    @DisplayName("when I have a few payment requests sent")
    public void havePaymentRequestsSent() throws IOException {
        session.openLoginPage()
                .login("student1@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .takeScreenshot("expenses-before")
                .clickOnPaymentRequestsSent()
                .shouldBeOnPaymentRequestsSentPage()
                .paymentRequestsSentGrandTotalShouldBe(amountOf(200))
                .takeScreenshot("paymentrequests_sent");
    }
}
