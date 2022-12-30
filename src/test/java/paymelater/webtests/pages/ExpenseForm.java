package paymelater.webtests.pages;

/*
 ** DO NOT CHANGE!!
 */


import org.openqa.selenium.By;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

public class ExpenseForm extends AbstractPage {
    public static final String PAGE_PATH = "/newexpense";
    private final By DESCRIPTION_FIELD = By.id("description");
    private final By AMOUNT_FIELD = By.id("amount");
    private final By DATE_FIELD = By.id("date");

    public ExpenseForm(WebTestRunner testRunner) {
        super(testRunner);
    }

    @Override
    public String path() {
        return PAGE_PATH;
    }

    public void fillExpenseForm(String description, MonetaryAmount amount, LocalDate date) {
        fillText(DESCRIPTION_FIELD, description);
        fillAmount(AMOUNT_FIELD, amount);
        fillDate(DATE_FIELD, date);
    }

    public ExpensesPage submitExpenseForm() {
        submit();
        return new ExpensesPage(testRunner);
    }
}
