package condition;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionExp {
    public static void main(String[] args) throws InterruptedException {

        class Queue {
            Lock reentrantLock = new ReentrantLock();

            Condition emptyCondition = reentrantLock.newCondition();
            Condition nonEmptyCondition = reentrantLock.newCondition();

            @Nullable
            Integer val;

            public void push(Integer val) {
                reentrantLock.lock();
                try {
                    while (this.val != null) {
                        emptyCondition.await();
                    }
                    this.val = val;
                    nonEmptyCondition.signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    reentrantLock.unlock();
                }
            }

            public Integer pull() {
                reentrantLock.lock();
                try {
                    while (this.val == null) {
                        nonEmptyCondition.await();
                    }
                    var v = this.val;
                    this.val = null;
                    emptyCondition.signal();
                    return v;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    reentrantLock.unlock();
                }
            }
        }

        var queue = new Queue();

        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                queue.push(i);
            }
        }, "pushThread1").start();

        new Thread(() -> {
            for (int i = 10; i < 20; i++) {
                queue.push(i);
            }
        }, "pushThread2").start();

        Thread.sleep(1_000);

        new Thread(() -> {
            var list = new ArrayList<Integer>();
            for (int i = 0; i < 20; i++) {
                list.add(queue.pull());
            }
            Collections.sort(list);
            System.out.println(list);
        }, "pullThread").start();

    }
}
