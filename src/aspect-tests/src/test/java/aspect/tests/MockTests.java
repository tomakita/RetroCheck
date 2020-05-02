package aspect.tests;

import org.junit.Test;

import static org.junit.Assert.*;

public class MockTests {

    @Test
    public void distributedJeromeBettis_becomePresident_mockIsCalled() {
        DistributedJeromeBettis theBus = new DistributedJeromeBettis();
        theBus.becomePresident();
        assertTrue(DistributedJeromeBettis.isInTheGame);
    }
}
