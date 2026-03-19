package models.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Person}.
 * <p>
 * Verifies that the Person constructor assigns fields correctly and that
 * getters return expected values.
 * </p>
 *
 * @author Aram Zand
 */
public class PersonTest {

    @Test
    public void testPersonDomain() {
        Person person = new Person(
                42,
                "John Doe",
                "/profile.jpg",
                "Male",
                "Acting",
                12.34
        );

        assertEquals(42, person.getId());
        assertEquals("John Doe", person.getName());
        assertEquals("/profile.jpg", person.getProfilePath());
        assertEquals("Male", person.getGender());
        assertEquals("Acting", person.getKnownForDepartment());
        assertEquals(12.34, person.getPopularity(), 0.0001);
    }
}
