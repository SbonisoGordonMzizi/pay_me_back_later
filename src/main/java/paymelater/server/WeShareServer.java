package paymelater.server;

/*
 ** DO NOT CHANGE!!
 */

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.NullSessionDataStore;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import paymelater.model.Expense;
import paymelater.model.Person;
import paymelater.persistence.ExpenseDAO;
import paymelater.persistence.PersonDAO;
import paymelater.persistence.collectionbased.ExpenseDAOImpl;
import paymelater.persistence.collectionbased.PersonDAOImpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static paymelater.model.DateHelper.TODAY;
import static paymelater.model.DateHelper.TOMORROW;
import static paymelater.model.MoneyHelper.amountOf;

public class WeShareServer {
    public static final String SESSION_USER_KEY = "user";
    private static final String PAGES_DIR = "/html";
    private static final String TEMPLATES_DIR = "/templates/";

    private final Javalin appServer;

    public WeShareServer() {
        JavalinThymeleaf.configure(templateEngine());

        appServer = Javalin.create(config -> {
            config.addStaticFiles(PAGES_DIR, Location.CLASSPATH);
            config.accessManager(accessManager());
            config.sessionHandler(sessionHandler());
        });

        ServiceRegistry.configure(PersonDAO.class, new PersonDAOImpl());
        ServiceRegistry.configure(ExpenseDAO.class, new ExpenseDAOImpl());
        Routes.configure(this);
        configureExceptionsPage();
    }

    public static void main(String[] args) {
        WeShareServer server = new WeShareServer();
        seedDemoData();
        server.start(5050);
    }

    @Nullable
    public static Person getPersonLoggedIn(Context context) {
        return context.sessionAttribute(SESSION_USER_KEY);
    }

    private static Supplier<SessionHandler> sessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
        sessionCache.setSessionDataStore(new NullSessionDataStore());
        sessionHandler.setSessionCache(sessionCache);
        sessionHandler.setHttpOnly(true);
        return () -> sessionHandler;
    }

    private static void seedDemoData() {
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);
        ExpenseDAO expenseDAO = ServiceRegistry.lookup(ExpenseDAO.class);

        Person student1 = new Person("student1@wethinkcode.co.za");
        Person student2 = new Person("student2@wethinkcode.co.za");
        Person student3 = new Person("student3@wethinkcode.co.za");
        Stream.of(student1, student2, student3).forEach(personDAO::savePerson);

        Expense expense1 = new Expense(student1, "Lunch", amountOf(300), TODAY);
        expense1.requestPayment(student2, amountOf(100), TOMORROW);
        expense1.requestPayment(student3, amountOf(100), TOMORROW);
        Expense expense2 = new Expense(student1, "Airtime", amountOf(100), TODAY);
        Expense expense3 = new Expense(student2, "Movies", amountOf(150), TODAY.minusWeeks(1));
        Expense expense4 = new Expense(student3, "Ice cream", amountOf(50), TODAY.minusDays(3));
        Stream.of(expense1, expense2, expense3, expense4).forEach(expenseDAO::save);
    }

    private Javalin configureExceptionsPage() {
        return appServer.exception(Exception.class, (e, context) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString().replace(System.getProperty("line.separator"), "<br/>\n");
            context.render("exception.html",
                    Map.of("exception", e,
                            "stacktrace", stackTrace));
        });
    }

    public void routes(EndpointGroup group) {
        appServer.routes(group);
    }

    public void start(int port) {
        this.appServer.start(port);
    }

    public void stop() {
        this.appServer.stop();
    }

    public int port() {
        return appServer.port();
    }

    private AccessManager accessManager() {
        return new AccessManager() {
            @Override
            public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<RouteRole> set) throws Exception {
                if (hasNoSession(context)) {
                    context.redirect(Routes.LOGIN_PAGE);
                } else {
                    handler.handle(context);
                }
            }

            private boolean hasNoSession(@NotNull Context context) {
                Person loggedInPerson = context.sessionAttribute(SESSION_USER_KEY);
                return Objects.isNull(loggedInPerson) && !context.path().equals(Routes.LOGIN_ACTION);
            }
        };
    }

    private TemplateEngine templateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATES_DIR);
        templateEngine.setTemplateResolver(resolver);
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }
}
