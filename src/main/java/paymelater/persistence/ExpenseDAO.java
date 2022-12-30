package paymelater.persistence;

/*
 ** DO NOT CHANGE!!
 */


import paymelater.model.Expense;
import paymelater.model.PaymentRequest;
import paymelater.model.Person;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseDAO {
    Collection<Expense> findExpensesForPerson(Person person);

    Expense save(Expense expense);

    Optional<Expense> get(UUID id);

    Collection<PaymentRequest> findPaymentRequestsSent(Person person);

    Collection<PaymentRequest> findPaymentRequestsReceived(Person person);
}
