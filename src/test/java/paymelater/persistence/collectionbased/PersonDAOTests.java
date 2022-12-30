package paymelater.persistence.collectionbased;

/*
 ** DO NOT CHANGE!!
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paymelater.model.Person;
import paymelater.persistence.PersonDAO;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonDAOTests {
    private PersonDAO dao;

    @BeforeEach
    public void newRepository() {
        Set<Person> people = Set.of(new Person("student1@wethinkcode.co.za"));
        this.dao = new PersonDAOImpl(people);
    }

    @Test
    public void findPerson() {
        Optional<Person> retrievedPerson = dao.findPersonByEmail("student1@wethinkcode.co.za");
        assertThat(retrievedPerson.isPresent()).isTrue();
    }

    @Test
    public void savePerson() {
        Person p = new Person("student2@wethinkcode.co.za");
        Person savedPerson = dao.savePerson(p);
        Optional<Person> retrievedPerson = dao.findPersonByEmail(savedPerson.getEmail());
        assertThat(retrievedPerson.isPresent()).isTrue();
    }

    @Test
    public void personNotFound() {
        Optional<Person> retrievedPerson = dao.findPersonByEmail("student@wethinkcode.co.za");
        assertThat(retrievedPerson.isEmpty()).isTrue();
    }
}
