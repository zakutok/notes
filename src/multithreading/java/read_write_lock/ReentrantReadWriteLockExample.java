package read_write_lock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockExample {
    public static void main(String[] args) {
        var reentrantReadWriteLock = new ReentrantReadWriteLock();

        var readLock = reentrantReadWriteLock.readLock();
        var writeLock = reentrantReadWriteLock.writeLock();

        readLock.lock();
        readLock.unlock();
    }
}
