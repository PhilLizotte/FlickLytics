package models.dto;

import org.junit.Test;
import static org.junit.Assert.*;

public class MovieDTOTest {

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
