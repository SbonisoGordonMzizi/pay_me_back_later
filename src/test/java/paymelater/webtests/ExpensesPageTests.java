package paymelater.webtests;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import paymelater.model.Expense;
import paymelater.model.PaymentRequest;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.server.ServiceRegistry;

import java.io.IOException;
import java.util.stream.Stream;

import static paymelater.model.DateHelper.TODAY;
import static paymelater.model.DateHelper.TOMORROW;
import static paymelater.model.MoneyHelper.amountOf;

@DisplayName("For the expenses page")
public class ExpensesPageTests extends WebTestRunner {
    private final WebSession session = new WebSession(ExpensesPageTests.this);

    @Override
    protected void setupTestData() {
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Person firstTimeUser = new Person("firsttimeuser@wethinkcode.co.za");
        Person p1 = new Person("student1@wethinkcode.co.za");
        Person p2 = new Person("student2@wethinkcode.co.za");
        Person p3 = new Person("student3@wethinkcode.co.za");
        Stream.of(firstTimeUser, p1, p2).forEach(personDAO::savePerson);

        Expense expense1 = new Expense(p1, "Lunch", amountOf(300), TODAY);
        Expense expense2 = new Expense(p1, "Airtime", amountOf(100), TODAY);
        Expense expense3 = new Expense(p2, "Movies", amountOf(100), TODAY.minusDays(1));
        PaymentRequest paymentRequest1 = expense3.requestPayment(p3, amountOf(100), TOMORROW);
        expense3.payPaymentRequest(paymentRequest1.getId(), p3, TODAY);
        Stream.of(expense1, expense2, expense3).forEach(expenseDAO::save);
    }

    @Test
    @DisplayName("when I have no expenses")
    void noExpenses() throws IOException {
        session.openLoginPage()
                .login("firsttimeuser@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .shouldHaveNoExpenses()
                .takeScreenshot("home");
    }

    @Test
    @DisplayName("when I have some expenses")
    public void haveExpenses() throws IOException {
        session.openLoginPage()
                .login("student1@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .shouldHaveExpense("Lunch")
                .shouldHaveExpense("Airtime")
                .expensesGrandTotalShouldBe(amountOf(400))
                .takeScreenshot("home");
    }

    @Test
    @DisplayName("when my expense is paid in full")
    public void expensePaidInFull() throws IOException {
        session.openLoginPage()
                .login("student3@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .shouldHaveNoExpenses()
                .takeScreenshot("home");
    }
}
