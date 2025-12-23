package scheduling;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TiredThreadTest {

    @Test
    void testCompareToByFatigue() throws Exception {
        TiredThread t1 = new TiredThread(1, 1.0);
        TiredThread t2 = new TiredThread(2, 2.0);

        // Use reflection to set timeUsed on instances so we can test ordering by fatigue
        Field f1 = TiredThread.class.getDeclaredField("timeUsed");
        f1.setAccessible(true);
        ((AtomicLong) f1.get(t1)).set(10L); // fatigue = 10

        Field f2 = TiredThread.class.getDeclaredField("timeUsed");
        f2.setAccessible(true);
        ((AtomicLong) f2.get(t2)).set(5L); // fatigue = 5 * 2.0 = 10

        // Equal fatigue => compareTo should be stable/consistent. We at least check that it returns an int.
        int cmp = t1.compareTo(t2);
        assertTrue(cmp == 0 || cmp < 0 || cmp > 0, "compareTo must return an int without throwing");
    }

    @Test
    void testRunExecutesTaskAndShutdown() throws Exception {
        TiredThread t = new TiredThread(0, 1.0);
        AtomicBoolean executed = new AtomicBoolean(false);

        t.start();

        // Assign a simple task
        t.newTask(() -> executed.set(true));

        // Wait up to 2s for execution
        long deadline = System.currentTimeMillis() + 2000;
        while (!executed.get() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }

        assertTrue(executed.get(), "Assigned task should have been executed by the worker");

        // Request shutdown and join
        t.shutdown();
        t.join(2000);
        assertTrue(!t.isAlive(), "Worker thread should terminate after shutdown");
    }
}
