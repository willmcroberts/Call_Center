import java.util.concurrent.ThreadLocalRandom;

public class Greeter implements Runnable{
    private final int ID;

    Greeter(int ID){
        this.ID = ID;
    }

    public void run() {
        for (int i = 0; i < CallCenter.totalCustomers; i++) {
            try {
                int customerID = CallCenter.acceptGreeting();
                System.out.println("Greeter greets Customer" + customerID);
                // Simulate spending time serving a customer
                Thread.sleep(ThreadLocalRandom.current().nextInt(20, 200));
                System.out.println("Greeter " + ID + " passes on " + customerID + " to Agents");
                CallCenter.addCall(customerID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
