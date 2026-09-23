import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CallCenter {
    public final static int totalCustomers = 20;
    public final static int totalAgents = 2;
    private final static Queue<Integer> queue = new LinkedList<>(); // Shared data
    private final static ReentrantLock qLock = new ReentrantLock();
    private final static Condition queueNotEmpty = qLock.newCondition();

    public static void addCall(int customerID) {
        qLock.lock();
        try {
            // Critical section
            queue.add(customerID);
            queueNotEmpty.signal();
        } finally {
            qLock.unlock();
        }
    }

    public static int takeCall() throws InterruptedException {
        int customerID;
        qLock.lock();
        try {
            while (queue.isEmpty()) {
                // await() releases the qLock and puts the thread to sleep.
                queueNotEmpty.await();
            }
            customerID = queue.remove();
        } finally {
            qLock.unlock();
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
