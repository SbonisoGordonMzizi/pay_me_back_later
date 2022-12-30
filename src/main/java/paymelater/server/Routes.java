package paymelater.server;

import paymelater.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";
    public static final String EXPENSES = "/expenses";
    public static final String NEWEXPENSE = "/newexpense";
    public static final String PAYMENTREQUEST = "/paymentrequest";
    public static final String PAYMENT_REQUESTS_RECEIVED = "/paymentrequests_received";
    public static final String PAYMENT_REQUESTS_SENT = "/paymentrequests_sent";


    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,  PersonController.login);
            get(LOGOUT,         PersonController.logout);
            get(EXPENSES,       ExpensesController.view);
            get(NEWEXPENSE,     ExpensesController.newExpense);
            post(EXPENSES,       ExpensesController.addNewExpense);
            get(PAYMENTREQUEST, PaymentRequestController.paymentRequest);
            post(PAYMENTREQUEST, PaymentRequestController.paymentRequest);
            get(PAYMENT_REQUESTS_RECEIVED, PaymentRequestController.receivePay);
            get(PAYMENT_REQUESTS_SENT, PaymentRequestController.sentPay);
            post(PAYMENT_REQUESTS_RECEIVED, PaymentRequestController.receivePay);

        });
    }
}
