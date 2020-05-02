package aspect.tests;

import com.retrocheck.mock.MockWith;

public class DistributedJeromeBettis {
    public static boolean isInTheGame = false;

    @MockWith(MockDistributedJeromeBettis.class)
    public void becomePresident() {
        System.out.println("and rejoice");
    }
}
