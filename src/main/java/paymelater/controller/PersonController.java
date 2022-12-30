package paymelater.controller;

import io.javalin.http.Handler;
import paymelater.model.Person;
import paymelater.persistence.PersonDAO;
import paymelater.server.Routes;
import paymelater.server.ServiceRegistry;
import paymelater.server.WeShareServer;

import java.util.Objects;

public class PersonController {

    public static final Handler logout = ctx -> {
        ctx.sessionAttribute(WeShareServer.SESSION_USER_KEY, null);
        ctx.redirect(Routes.LOGIN_PAGE);
    };

    private static final PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);

    public static final Handler login = context -> {
        String email = context.formParamAsClass("email", String.class)
                .check(Objects::nonNull, "Email is required")
                .get();

        Person person = personDAO.savePerson(new Person(email));
        context.sessionAttribute(WeShareServer.SESSION_USER_KEY, person);
        context.redirect(Routes.EXPENSES);
    };
}
