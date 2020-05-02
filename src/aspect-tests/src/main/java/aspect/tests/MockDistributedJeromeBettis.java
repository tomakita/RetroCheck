package aspect.tests;

public class MockDistributedJeromeBettis {
    public void becomePresident() {
        // JEROME BETTIS IS ALWAYS IN THE GAME
        DistributedJeromeBettis.isInTheGame = true;
    }
}
