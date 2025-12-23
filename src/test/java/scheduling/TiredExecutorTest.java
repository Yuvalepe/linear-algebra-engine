package scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TiredExecutorTest {

    @Test
    void testSubmitAllWaitsForCompletion() throws Exception {
        TiredExecutor ex = new TiredExecutor(3);
        AtomicInteger counter = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
                counter.incrementAndGet();
            });
        }

        ex.submitAll(tasks);

        // After submitAll returns, all tasks should have completed
        assertEquals(10, counter.get(), "All tasks should have finished after submitAll returns");

        // Shut down to clean up threads if implemented
        ex.shutdown();
    }
}
