package examblock.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeskTest {

    @Test
    public void basicTestDeskNumber() {
        Desk testDesk = new Desk(1);
        assertEquals(1, testDesk.deskNumber());
        testDesk = new Desk(10);
        assertEquals(10, testDesk.deskNumber());
        testDesk = new Desk(1111);
        assertEquals(1111, testDesk.deskNumber());
    }

    @Test
    public void basicTestDeskNames() {
        //runs tests to confirm inits are all bounded correctly and assigned at the right times etc
        Registry reg = new RegistryImpl();

        Desk testDesk = new Desk(25);
        Student who = new Student (9999L, "Winston", "Smith", 1, 2, 1900, "Blue", reg);
        testDesk.setStudent(who);
        assertEquals("Smith", testDesk.deskFamilyName());
        testDesk = new Desk(33);
        Student who2 = new Student (9998L, "Sir Samual", "Hyphonated-Surname", 2, 3, 1901, "Blue", reg);
        testDesk.setStudent(who2);
        assertEquals("Hyphonated-Surname", testDesk.deskFamilyName());
        testDesk = new Desk(42);
        Student who3 = new Student (9997L, "Lady Jane", "van Barle", 3, 4, 1902, "Pink", reg);
        testDesk.setStudent(who3);
        assertEquals("van Barle", testDesk.deskFamilyName());
        testDesk = new Desk(49);
        testDesk.setGivenAndInit("David X.");
        assertEquals("David X.", testDesk.deskGivenAndInit());
        testDesk = new Desk(49);
        testDesk.setGivenAndInit("John");
        assertEquals("John", testDesk.deskGivenAndInit());
        testDesk = new Desk(49);
        testDesk.setGivenAndInit("Savannah G.");
        assertEquals("Savannah G.", testDesk.deskGivenAndInit());
    }
}