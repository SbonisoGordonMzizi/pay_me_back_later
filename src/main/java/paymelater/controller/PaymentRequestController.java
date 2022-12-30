package paymelater.controller;

import io.javalin.http.Handler;
import paymelater.model.*;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.server.ServiceRegistry;
import paymelater.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static paymelater.model.MoneyHelper.ZERO_RANDS;

public class PaymentRequestController {


    public static final Handler receivePay = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> paymentRequestsReceived = expensesDAO.findPaymentRequestsReceived(personLoggedIn);

        String methodType = context.method();
        if(methodType.equals("POST")){
            String expenseId = context.formParamAsClass("expenseId", String.class)
                    .check(Objects::nonNull, "Expense ID is required")
                    .get();
            for(PaymentRequest pay : paymentRequestsReceived){
                if(pay.getId().toString().equals(expenseId)){
                    Payment payment = pay.pay(personLoggedIn,LocalDate.now());
                    expensesDAO.save(payment.getExpenseForPersonPaying());
                }
            }

        }
        MonetaryAmount moneyOwed = ZERO_RANDS;
        for(PaymentRequest request: paymentRequestsReceived){
            if(!request.isPaid()) {
                moneyOwed = moneyOwed.add(request.getAmountToPay());
            }
        }

        Map<String, Object> viewModel = Map.of("paymentrequests_received", paymentRequestsReceived, "moneyOwed", moneyOwed);
        context.render("paymentrequests_received.html", viewModel);
    };


    public static final Handler paymentRequest = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        String queryString = context.queryString();
        String expenseId = queryString.toString().split("=")[1];
        Optional<Expense> optionalExpense = expensesDAO.get(UUID.fromString(expenseId));
        Expense expense = optionalExpense.get();

        String methodType = context.method();
        if(methodType.equals("POST")){
            String email = context.formParamAsClass("email", String.class)
                    .check(Objects::nonNull, "Description  is required")
                    .get();
            String date = context.formParamAsClass("date", String.class)
                    .check(Objects::nonNull, "Date  is required")
                    .get();
            String amount = context.formParamAsClass("amount", String.class)
                    .check(Objects::nonNull, "Amount  is required")
                    .get();

            Double money = Double.parseDouble(amount);
            expense.requestPayment(personDAO.findPersonByEmail(email).get(),
                    MoneyHelper.amountOf(money.longValue()),
                    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            expensesDAO.save(expense);
        }

        MonetaryAmount monetaryAmount = MoneyHelper.amountOf(0);
        for(PaymentRequest paymentR : expense.listOfPaymentRequests()){
            monetaryAmount = monetaryAmount.add(paymentR.getAmountToPay());
        }


        MonetaryAmount expenseToPay = expense.getAmount().subtract(monetaryAmount);
        Map<String, Object> viewModel = Map.of("expense", expense,
                "maxAmount",processMonetaryAmount(expense),"totalToPay",expenseToPay);
        context.render("paymentrequest.html", viewModel);
    };

    private  static int processMonetaryAmount(Expense expense){
        MonetaryAmount expenseAmount = expense.getAmount();
        Double amount =  Double.parseDouble(expenseAmount.toString().split(" ")[1]);
        return  amount.intValue();
    }


    public static final Handler sentPay = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> paymentRequestsSent = expensesDAO.findPaymentRequestsSent(personLoggedIn);

        MonetaryAmount totalSent = ZERO_RANDS;

        for(PaymentRequest sent: paymentRequestsSent){
            totalSent = totalSent.add(sent.getAmountToPay());
        }

        Map<String, Object> viewModel = Map.of("paymentrequests_sent", paymentRequestsSent, "totalSent", totalSent);
        context.render("paymentrequests_sent.html", viewModel);

    };


}
