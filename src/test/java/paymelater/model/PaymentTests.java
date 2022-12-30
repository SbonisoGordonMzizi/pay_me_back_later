package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.Test;

import javax.money.MonetaryAmount;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static paymelater.model.DateHelper.TODAY;
import static paymelater.model.DateHelper.TOMORROW;
import static paymelater.model.MoneyHelper.ZERO_RANDS;
import static paymelater.model.MoneyHelper.amountOf;

public class PaymentTests {
    private final LocalDate IN_FIVE_DAYS = TODAY.plusDays(5);
    private final MonetaryAmount R100 = amountOf(100);
    private final Person personWhoPaidForSomeone = new Person("ipaidforyou@wethinkcode.co.za");
    private final Person personWhoShouldPayBack = new Person("ioweyou@wethinkcode.co.za");

    @Test
    public void payThePaymentRequest() {
        // Expense was incurred yesterday
        Expense airtime = new Expense(personWhoPaidForSomeone, "Airtime", R100, TODAY.minusDays(1));

        // Payment requested for today
        PaymentRequest paymentRequest = airtime.requestPayment(personWhoShouldPayBack, R100, TODAY);

        // Payment is for today
        Payment payment = paymentRequest.pay(personWhoShouldPayBack, TODAY);

        // payment should have been made by personWhoShouldPayBack
        assertThat(payment.getPersonPaying()).isEqualTo(personWhoShouldPayBack);

        // payment amount should be for the full amount of the payment request
        assertThat(payment.getAmountPaid()).isEqualTo(paymentRequest.getAmountToPay());

        // payment request should be paid
        assertThat(paymentRequest.isPaid()).isTrue();

        // should have an expense for person paying
        Expense expenseForPersonPaying = payment.getExpenseForPersonPaying();
        assertThat(expenseForPersonPaying).isNotNull();
        assertThat(expenseForPersonPaying.getPerson()).isEqualTo(personWhoShouldPayBack);
        assertThat(expenseForPersonPaying.getDescription()).isEqualTo(paymentRequest.getDescription());
        assertThat(expenseForPersonPaying.getAmount()).isEqualTo(paymentRequest.getAmountToPay());
        assertThat(expenseForPersonPaying.getDate()).isEqualTo(payment.getPaymentDate());

        // check original expense totals
        assertThat(airtime.getAmount()).isEqualTo(amountOf(100));
        assertThat(airtime.totalAmountForPaymentsReceived()).isEqualTo(amountOf(100));
        assertThat(airtime.amountLessPaymentsReceived()).isEqualTo(ZERO_RANDS);
        assertThat(airtime.isFullyPaidByOthers()).isTrue();
    }

    @Test
    public void payById() {
        Expense airtime = new Expense(personWhoPaidForSomeone, "Airtime", R100, TODAY.minusDays(1));
        PaymentRequest paymentRequest = airtime.requestPayment(personWhoShouldPayBack, R100, TODAY);
        Payment payment = airtime.payPaymentRequest(paymentRequest.getId(), personWhoShouldPayBack, TODAY);
        Expense expenseForPersonPaying = payment.getExpenseForPersonPaying();

        assertThat(paymentRequest.isPaid()).isTrue();
        assertThat(expenseForPersonPaying).isNotNull();
        assertThat(expenseForPersonPaying.getPerson()).isEqualTo(personWhoShouldPayBack);
        assertThat(expenseForPersonPaying.getDescription()).isEqualTo(paymentRequest.getDescription());
        assertThat(expenseForPersonPaying.getAmount()).isEqualTo(paymentRequest.getAmountToPay());
        assertThat(expenseForPersonPaying.getDate()).isEqualTo(payment.getPaymentDate());
    }

    @Test
    public void wrongPersonPaysTheRequest() {
        Expense airtime = new Expense(personWhoPaidForSomeone, "Airtime", R100, TODAY);
        PaymentRequest paymentRequest = airtime.requestPayment(personWhoShouldPayBack, R100, IN_FIVE_DAYS);

        Person anotherPerson = new Person("another@wethinkcode.co.za");
        assertThatThrownBy(() -> paymentRequest.pay(anotherPerson, TODAY)).isInstanceOf(WeShareException.class).hasMessageContaining("Wrong person is trying to pay the payment request");
    }

    @Test
    public void cannotPayInTheFuture() {
        Expense airtime = new Expense(personWhoPaidForSomeone, "Airtime", R100, TODAY);
        PaymentRequest paymentRequest = airtime.requestPayment(personWhoShouldPayBack, R100, IN_FIVE_DAYS);

        assertThatThrownBy(() -> paymentRequest.pay(personWhoShouldPayBack, TOMORROW)).isInstanceOf(WeShareException.class).hasMessageContaining("Cannot make a payment in the future");
    }
}
