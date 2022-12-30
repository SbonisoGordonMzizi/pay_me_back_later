package paymelater.webtests;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.server.ServiceRegistry;
import paymelater.webtests.pages.ExpensesPage;

import java.io.IOException;

@DisplayName("When using the app")
public class LoginAndLogoutTests extends WebTestRunner {
    private final WebSession session = new WebSession(LoginAndLogoutTests.this);

    @Override
    protected void setupTestData() {
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Person p = new Person("student@wethinkcode.co.za");
        personDAO.savePerson(p);
    }

    @Test
    @DisplayName("I am asked to login before viewing any other page")
    void redirectWhenNotLoggedIn() throws IOException {
        session.open(new ExpensesPage(LoginAndLogoutTests.this))
                .shouldBeOnLoginPage()
                .takeScreenshot("login");
    }

    @Test
    @DisplayName("I logged in and can see the home page")
    void successfulLogin() throws IOException {
        session.openLoginPage()
                .login("student@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .shouldHaveEmailAddressDisplayed()
                .shouldHaveLogoutLinkDisplayed()
                .takeScreenshot("home");
    }

    @Test
    @DisplayName("I can logout after logging in")
    void logout() throws IOException {
        session.openLoginPage()
                .takeScreenshot("login")
                .login("student@wethinkcode.co.za")
                .shouldBeOnExpensesPage()
                .takeScreenshot("home")
                .logout()
                .shouldBeOnLoginPage()
                .takeScreenshot("logged-out");
    }
}
