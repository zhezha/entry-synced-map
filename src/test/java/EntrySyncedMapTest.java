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
        List<Task> tasks = Arrays.asList(
                new Task("key1", 3000, map),
                new Task("key1", 2000, map),
                new Task("key2", 0, map)
        );

        System.out.println("Test start at: " + sdf.format(new Date()));
        List<Thread> threads = new ArrayList<>();
        int threadId = 0;
        for (Task task : tasks) {
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

    private class Task implements Runnable {
        private String key;
        private long duration;
        private EntrySyncedMap map;

        public Task(String key, long duration, EntrySyncedMap entrySyncedMap) {
            this.key = key;
            this.duration = duration;
            this.map = entrySyncedMap;
        }

        @Override
        public void run() {
            map.lockEntry(key);
            try {
                List<String> list = map.getList(key);
                if (list == null) {
                    list = new ArrayList<>();
                }
                if (duration > 0) {
                    printMessage("go to sleep for " + duration + " ms");
                    Thread.sleep(duration);
                    printMessage("wake up");
                }
                String value = "Thread " + Thread.currentThread().getName() + ": " + sdf.format(new Date());
                list.add(value);
                printMessage("write to entry " + key + ": " + list.toString());
                map.putList(key, list);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                map.unlockEntry(key);
            }
        }
    }

    private void printMessage(String msg) {
        System.out.println("Thread " + Thread.currentThread().getName() + ": " + msg);
    }
}
