import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zhengyang on 5/11/2017.
 */
public class EntrySyncedMap {
    private HashMap<String, List<String>> contentMap = new HashMap<>();
    private HashMap<String, ReentrantLock> lockMap = new HashMap<>();

    public EntrySyncedMap() {    }

    public void processEntry(String key, long duration, SimpleDateFormat sdf) {
        lockEntry(key);
        try {
            List<String> list = contentMap.get(key);
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
            contentMap.put(key, list);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unlockEntry(key);
        }
    }

    protected void lockEntry(String key) {
        printMessage("acquiring lockMap");
        synchronized (lockMap) {
            printMessage("acquired lockMap");
            ReentrantLock entryLock = lockMap.get(key);
            if (entryLock == null) {
                entryLock = new ReentrantLock();
                lockMap.put(key, entryLock);
                printMessage("acquiring entry " + key);
                entryLock.lock();
                printMessage("acquired entry " + key);
            } else {
                printMessage("acquiring entry " + key);
                while (!entryLock.tryLock()) {
                    try {
                        printMessage("waiting for " + key + " meanwhile release lockMap");
                        lockMap.wait();
                        printMessage("acquired lockMap again");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                printMessage("acquired entry " + key);
            }
            lockMap.notifyAll();
            printMessage("release lockMap and notifyAll waiting threads");
        }
    }

    protected void unlockEntry(String key) {
        synchronized (lockMap) {
            ReentrantLock entryLock = lockMap.get(key);
            if (entryLock != null && entryLock.isHeldByCurrentThread()) {
                entryLock.unlock();
                printMessage("release entry " + key);
            }
            printMessage("notifyAll threads waiting for lockMap");
            lockMap.notifyAll();
        }
    }

    private void printMessage(String msg) {
        System.out.println("Thread " + Thread.currentThread().getName() + ": " + msg);
    }
}
