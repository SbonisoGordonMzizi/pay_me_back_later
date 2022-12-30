package paymelater.controller;

import io.javalin.http.Handler;
import paymelater.model.Expense;
import paymelater.model.MoneyHelper;
import paymelater.model.PaymentRequest;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.server.ServiceRegistry;
import paymelater.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest>  paymentRequests = expensesDAO.findPaymentRequestsSent(personLoggedIn);
        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        Map<String, Object> viewModel = Map.of("expenses", expenses,"total",totalExpenses(expenses,paymentRequests));
        context.render("expenses.html", viewModel);
    };


    public static final Handler newExpense = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        Map<String, Object> viewModel = Map.of("expenses", expenses);
        context.render("newexpense.html", viewModel);
    };


    public static final Handler addNewExpense = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String description = context.formParamAsClass("description", String.class)
                .check(Objects::nonNull, "Description  is required")
                .get();
        String date = context.formParamAsClass("date", String.class)
                .check(Objects::nonNull, "Date  is required")
                .get();
        String amount = context.formParamAsClass("amount", String.class)
                .check(Objects::nonNull, "Amount  is required")
                .get();

        Expense expense = new Expense(personLoggedIn,
                description,MoneyHelper.amountOf(Long.parseLong(amount)),
                LocalDate.parse(date,DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        expensesDAO.save(expense);
        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);

        Collection<PaymentRequest> paymentRequests = expensesDAO.findPaymentRequestsSent(personLoggedIn);
        Map<String, Object> viewModel = Map.of("expenses", expenses,"total",totalExpenses(expenses,paymentRequests));
        context.render("expenses.html", viewModel);
    };


    private static MonetaryAmount totalExpenses(Collection<Expense> expenses,Collection<PaymentRequest> paymentRequests){

        MonetaryAmount totalExpenses = MoneyHelper.amountOf(0L);
        MonetaryAmount totalPaymentRequest = MoneyHelper.amountOf(0L);

        for(Expense expense: expenses){
            totalExpenses =totalExpenses.add(expense.getAmount());
        }

        for(PaymentRequest paymentRequest: paymentRequests){
            totalPaymentRequest = totalPaymentRequest.add(paymentRequest.getAmountToPay());
        }
        totalExpenses = totalExpenses.subtract(totalPaymentRequest);
        return totalExpenses;
    }

}
