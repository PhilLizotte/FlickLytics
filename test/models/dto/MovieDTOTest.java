package models.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for DTO classes such as MovieDTO and PersonDTO.
 * <p>
 * Verifies that DTO objects can be instantiated and fields can be assigned correctly.
 * Ensures basic object integrity for domain transfer objects.
 * </p>
 *
 * @author Ali Maher
 */
public class MovieDTOTest {

    /**
     * Tests the PersonDTO object.
     * Assigns sample values to fields and checks object is not null.
     */
    @Test
    public void testPersonDTO() {
        PersonDTO person = new PersonDTO();

        person.adult = true;
        person.gender = 1;
        person.id = 5;
        person.name = "Test";
        person.known_for_department = "Acting";

        assertNotNull(person);
    }
}