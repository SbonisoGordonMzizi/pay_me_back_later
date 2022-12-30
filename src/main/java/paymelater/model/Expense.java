package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.function.MonetaryFunctions;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static paymelater.model.MoneyHelper.ZERO_RANDS;

public class Expense {
    private final Person person;
    private final String description;
    private final MonetaryAmount amount;
    private final LocalDate date;
    private final UUID id;
    private final HashMap<UUID, PaymentRequest> paymentRequests;

    public Expense(Person person, String description, MonetaryAmount amount, LocalDate date) {
        checkDate(date);
        this.person = person;
        this.description = Strings.isNullOrEmpty(description) ? "Unspecified" : description;
        this.amount = amount;
        this.date = date;
        this.id = UUID.randomUUID();
        this.paymentRequests = new HashMap<>();
    }

    public PaymentRequest requestPayment(Person personWhoShouldPayBack, MonetaryAmount amountToPay, LocalDate dueDate) {
        PaymentRequest paymentRequest = new PaymentRequest(this, personWhoShouldPayBack, amountToPay, dueDate);
        paymentRequests.put(paymentRequest.getId(), paymentRequest);
        return paymentRequest;
    }

    public Collection<PaymentRequest> listOfPaymentRequests() {
        return paymentRequests.values().stream()
                .sorted(Comparator.comparing(PaymentRequest::daysLeftToPay))
                .collect(Collectors.toUnmodifiableList());
    }

    public MonetaryAmount totalAmountOfPaymentsRequested() {
        var maybeSum = paymentRequests.values().stream()
                .map(PaymentRequest::getAmountToPay)
                .reduce(MonetaryFunctions.sum());
        return maybeSum.orElse(Money.zero(this.amount.getCurrency()));
    }

    public MonetaryAmount totalAmountAvailableForPaymentRequests() {
        return this.amount.subtract(this.totalAmountOfPaymentsRequested());
    }

    public MonetaryAmount totalAmountForPaymentsReceived() {
        return paymentRequests.values().stream()
                .filter(PaymentRequest::isPaid)
                .map(PaymentRequest::getAmountToPay)
                .reduce(MonetaryFunctions.sum())
                .orElse(Money.zero(this.amount.getCurrency()));
    }

    public MonetaryAmount amountLessPaymentsReceived() {
        var sum = totalAmountForPaymentsReceived();
        return this.amount.subtract(sum);
    }

    public Payment payPaymentRequest(UUID paymentRequestId, Person personWhoShouldPayBack, LocalDate date) {
        return this.listOfPaymentRequests().stream()
                .filter(pr -> pr.getId().equals(paymentRequestId))
                .findFirst()
                .orElseThrow(() -> new WeShareException("Cannot find payment request"))
                .pay(personWhoShouldPayBack, date);
    }

    public boolean isFullyPaidByOthers() {
        return amountLessPaymentsReceived().isEqualTo(ZERO_RANDS);
    }

    public Person getPerson() {
        return person;
    }

    public String getDescription() {
        return description;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    private void checkDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) throw new WeShareException("Expense cannot be in the future");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equal(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("person", person)
                .add("description", description)
                .add("amount", amount)
                .add("date", date)
                .add("id", id)
                .toString();
    }
}
