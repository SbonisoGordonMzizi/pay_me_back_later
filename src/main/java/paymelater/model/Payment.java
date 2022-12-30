package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */


import javax.money.MonetaryAmount;
import java.time.LocalDate;

public class Payment {
    private final PaymentRequest paymentRequest;
    private final Person personPaying;
    private final LocalDate paymentDate;
    private final Expense expenseForPersonPaying;

    public Payment(PaymentRequest paymentRequest, Person personPaying, LocalDate paymentDate) {
        this.paymentRequest = paymentRequest;
        this.personPaying = personPaying;
        this.paymentDate = paymentDate;
        this.expenseForPersonPaying = new Expense(personPaying, paymentRequest.getDescription(), paymentRequest.getAmountToPay(), paymentDate);
    }

    public Person getPersonPaying() {
        return personPaying;
    }

    public MonetaryAmount getAmountPaid() {
        return paymentRequest.getAmountToPay();
    }

    public Expense getExpenseForPersonPaying() {
        return expenseForPersonPaying;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}
