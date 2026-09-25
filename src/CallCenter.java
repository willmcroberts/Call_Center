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
    private final static Condition queueNotEmpty = agentLock.newCondition();

    public static void addCall(int customerID) {
        agentLock.lock();
        try {
            // Critical section
            agentQueue.add(customerID);
            queueNotEmpty.signal();
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
                queueNotEmpty.await();
            }
            customerID = agentQueue.remove();
        } finally {
            agentLock.unlock();
        }
        return customerID;
    }

    static void main() throws InterruptedException {
        // For long-lived tasks
        ExecutorService agentPool = Executors.newFixedThreadPool(4);

        // For short-lived. come-and-go tasks
        ExecutorService customerPool = Executors.newCachedThreadPool();

        for (int i = 1; i <= totalAgents; i++) {
            agentPool.submit(new Agent(i));
        }

        for (int i = 1; i <= totalCustomers; i++) {
            customerPool.submit(new Customer(i));
            Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
        }
        agentPool.shutdown();
        customerPool.shutdown();
    }
}
