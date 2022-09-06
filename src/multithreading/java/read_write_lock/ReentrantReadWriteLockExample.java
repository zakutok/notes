package read_write_lock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockExample {

    public static void main(String[] args) throws InterruptedException {

        var res = new ArrayList<>(List.of(1));

        var reentrantReadWriteLock = new ReentrantReadWriteLock();

        var readLock = reentrantReadWriteLock.readLock();
        var writeLock = reentrantReadWriteLock.writeLock();

        Callable<Void> readableTask = () -> {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1_000, 4_000));
            readLock.lock();
            System.out.println("in read lock: readLockCount = " + reentrantReadWriteLock.getReadLockCount() + " isWriteLocked = " + reentrantReadWriteLock.isWriteLocked() + " " + res);
            Thread.sleep(ThreadLocalRandom.current().nextLong(1_000));
            readLock.unlock();
            return null;
        };

        Callable<Void> writableTask = () -> {
            Thread.sleep(ThreadLocalRandom.current().nextLong(1_000, 3_000));
            writeLock.lock();
            res.add(res.get(res.size() - 1) + 1);
            System.out.println("in write lock: readLockCount = " + reentrantReadWriteLock.getReadLockCount() + " isWriteLocked = " + reentrantReadWriteLock.isWriteLocked() + " " + res);
            Thread.sleep(ThreadLocalRandom.current().nextLong(3_000));
            writeLock.unlock();
            return null;
        };

        var readThreadsNumber = 10;
        var writeThreadsNumber = 3;
        var nThreads = readThreadsNumber + writeThreadsNumber;
        var executorService = Executors.newFixedThreadPool(nThreads);

        var tasks = new ArrayList<>(Collections.nCopies(readThreadsNumber, readableTask));
        tasks.addAll(Collections.nCopies(writeThreadsNumber, writableTask));

        executorService.invokeAll(tasks);
        executorService.shutdown();
    }
}