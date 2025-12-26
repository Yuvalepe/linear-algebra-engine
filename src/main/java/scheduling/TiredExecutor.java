package scheduling;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads];
        Random rand = new Random();
        
        for (int i = 0; i < numThreads; i++) {
            double fatigueFactor = 0.5 + rand.nextDouble();
            workers[i] = new TiredThread(i, fatigueFactor);
        }

        for (TiredThread worker : workers) {
            idleMinHeap.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) {
        try {
            TiredThread worker = idleMinHeap.take();
            
            inFlight.incrementAndGet();

            // Wrap the task to ensure the worker returns to the heap after finishing
            worker.newTask(() -> {
                try {
                    task.run();
                } finally {
                    inFlight.decrementAndGet();
                    idleMinHeap.add(worker);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void submitAll(Iterable<Runnable> tasks) {
        for (Runnable task : tasks) {
            submit(task);
        }

        // Wait until all submitted tasks have completed
        while (inFlight.get() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void shutdown() throws InterruptedException {
        for (TiredThread worker : workers) {
            worker.shutdown();
        }
        for (TiredThread worker : workers) {
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder report = new StringBuilder();
        for (TiredThread worker : workers) {
            report.append(worker.toString()).append("\n");
        }
        return report.toString();
    }
}
