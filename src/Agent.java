import java.util.concurrent.ThreadLocalRandom;

public class Agent implements Runnable {
    private final int ID;

    Agent(int ID) {
        this.ID = ID;
    }

    public void run() {
        int customerPerAgent = CallCenter.totalCustomers / CallCenter.totalAgents;
        for (int i = 0; i < customerPerAgent; i++) {
            try {
                int customerID = CallCenter.takeCall();
                System.out.println("Agent " + ID + " starts serving customer " + customerID);
                // Simulate spending time serving a customer
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 200));
                System.out.println("Agent " + ID + " finished serving customer " + customerID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
