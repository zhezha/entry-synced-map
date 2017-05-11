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

    public List<String> getList(String key) {
        return contentMap.get(key);
    }

    public void putList(String key, List<String> list) {
        contentMap.put(key, list);
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
