package paymelater.server;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class WeShareServerTests {

    @Test
    public void ifServerStartsThenItIsProperlyConfigured() {
        WeShareServer server = new WeShareServer();
        server.start(0);
        assertThat(server.port()).isGreaterThan(0);
    }
}
