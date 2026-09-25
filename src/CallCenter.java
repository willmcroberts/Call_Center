import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CallCenter {
    public final static int totalCustomers = 30;
    public final static int totalAgents = 3;
    public final static int totalGreeters = 1;
    private final static Queue<Integer> greeterQueue = new LinkedList<>(); //Shared data
    private final static Queue<Integer> agentQueue = new LinkedList<>(); // Shared data
    private final static ReentrantLock agentLock = new ReentrantLock();
    private final static ReentrantLock greeterLock = new ReentrantLock();
    private final static Condition greeterQueueNotEmpty = greeterLock.newCondition();
    private final static Condition agentQueueNotEmpty = agentLock.newCondition();

    public static void greeting(int customerID){
        greeterLock.lock();
        try {
            greeterQueue.add(customerID);
            greeterQueueNotEmpty.signal();
        } finally {
            greeterLock.unlock();
        }
    }

    public static int acceptGreeting() throws InterruptedException{
        int customerID;
        greeterLock.lock();
        try{
            while (greeterQueue.isEmpty()){
                greeterQueueNotEmpty.await();
            }
            customerID = greeterQueue.remove();
        } finally {
            greeterLock.unlock();
        }
        return customerID;
    }

    public static void addCall(int customerID) {
        agentLock.lock();
        try {
            // Critical section
            agentQueue.add(customerID);
            agentQueueNotEmpty.signal();
        } finally {
            agentLock.unlock();
        }
    }

    public static int takeCall() throws InterruptedException {
        int customerID;
        agentLock.lock();
        try {
            while (agentQueue.isEmpty()) {
                // await() releases the agentLock and puts the thread to sleep.
                agentQueueNotEmpty.await();
            }
            customerID = agentQueue.remove();
        } finally {
            agentLock.unlock();
        }
        return customerID;
    }

    public static void main(String[] args) throws InterruptedException{
        // For long-lived tasks
        ExecutorService staffPool = Executors.newFixedThreadPool(4);

        // For short-lived. come-and-go tasks
        ExecutorService customerPool = Executors.newCachedThreadPool();

        for (int i = 1; i <= totalGreeters; i++){
            staffPool.submit(new Greeter(i));
        }

        for (int i = 1; i <= totalAgents; i++) {
            staffPool.submit(new Agent(i));
        }

        for (int i = 1; i <= totalCustomers; i++) {
            customerPool.submit(new Customer(i));
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
        }
        staffPool.shutdown();
        customerPool.shutdown();
    }
}
