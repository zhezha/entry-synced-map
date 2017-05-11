import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhengyang on 5/11/2017.
 */
@RunWith(JUnit4.class)
public class EntrySyncedMapTest {
    private Thread.UncaughtExceptionHandler handler = (t, e) -> {
        System.out.println("Thread " + t.getId() + " throws exception: ");
        e.printStackTrace();
    };
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Test
    public void test() throws InterruptedException {
        EntrySyncedMap map = new EntrySyncedMap();
        List<Runnable> tasks = Arrays.asList(
                () -> map.processEntry("key1", 3000, sdf),
                () -> map.processEntry("key1", 2000, sdf),
                () -> map.processEntry("key2", 0, sdf)
        );

        System.out.println("Test start at: " + sdf.format(new Date()));
        List<Thread> threads = new ArrayList<>();
        int threadId = 0;
        for (Runnable task : tasks) {
            Thread thread = new Thread(task, String.valueOf(threadId++));
            thread.setUncaughtExceptionHandler(handler);
            thread.start();
            threads.add(thread);
        }
        // Thread.join() must be called after all threads have started.
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
