package paymelater.model;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PersonTests {
    @Test
    public void invalidEmailAddressFails() {
        assertThatThrownBy(() -> new Person("not an email"))
                .isInstanceOf(WeShareException.class)
                .hasMessageContaining("Bad email address");
    }

    @Test
    public void nameFromEmailAddress() {
        Person p = new Person("student@wethinkcode.co.za");
        assertThat(p.getName()).isEqualTo("Student");
    }
}
