package paymelater.server;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.Test;
import paymelater.persistence.PersonDAO;
import paymelater.persistence.collectionbased.PersonDAOImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ServiceRegistryTests {

    @Test
    public void configureService() {
        PersonDAO dao = new PersonDAOImpl();
        ServiceRegistry.configure(PersonDAO.class, dao);
        assertThat(ServiceRegistry.lookup(PersonDAO.class)).isEqualTo(dao);
    }

    @Test
    public void registryNotInitialised() {
        assertThatThrownBy(() -> ServiceRegistry.lookup(WeShareServer.class))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("No service configured for");
    }
}
