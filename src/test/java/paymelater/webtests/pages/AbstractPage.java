package paymelater.webtests.pages;

/*
 ** DO NOT CHANGE!!
 */


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import paymelater.model.MoneyHelper;
import paymelater.webtests.WebTestRunner;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static paymelater.model.DateHelper.DD_MM_YYYY;

/**
 * Base class that represents a page in the app that is under test
 */
public abstract class AbstractPage {
    protected final WebTestRunner testRunner;
    protected final WebDriver driver;

    public AbstractPage(WebTestRunner testRunner) {
        this.testRunner = testRunner;
        this.driver = testRunner.driver();
    }

    protected Function<String, Function<String, By>> rowLocator() {
        return (id) -> (row) -> By.id(id + "_" + row);
    }

    public void open() {
        testRunner.navigateTo(this);
    }

    public abstract String path();

    public void fillText(By element, String keys) {
        var field = testRunner.driver().findElement(element);
        field.clear();
        field.sendKeys(keys);
    }

    public void click(By element) {
        testRunner.driver().findElement(element).click();
    }

    public void submit() {
        WebElement button = testRunner.driver().findElement(By.xpath("//input[@type='submit']"));
        button.submit();
    }

    public String textOf(By locator) {
        return driver.findElement(locator).getText();
    }

    protected String hrefOf(By locator) {
        return driver.findElement(locator).getAttribute("href");
    }

    protected MonetaryAmount amountOf(By locator) {
        String s = textOf(locator);
        String amount = s.split(" ")[1].split("\\.")[0];
        return MoneyHelper.amountOf(Long.parseLong(amount));
    }

    protected LocalDate localDateOf(By locator) {
        return LocalDate.parse(textOf(locator), DateTimeFormatter.ISO_DATE);
    }

    protected void fillDate(By field, LocalDate date) {
        fillText(field, DD_MM_YYYY.format(date));
    }

    protected void fillAmount(By field, MonetaryAmount amount) {
        fillText(field, amount.getNumber().toString());
    }
}
