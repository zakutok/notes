package phaser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PhaserExample {

    public static void main(String[] args) throws InterruptedException {

        alternativeToCountDownLatch();
        recurrentBatchExecution();
        buildTreeOfPhasers();

    }

    private static void alternativeToCountDownLatch() throws InterruptedException {
        printHeading();

        Runnable runnable = () -> {
            System.out.print(Thread.currentThread().getName() + " ");
        };

        var runnables = Collections.nCopies(10, runnable);

        var phaser = new Phaser(1);

        for (var i = 0; i < runnables.size(); i++) {
            var task = runnables.get(i);
            phaser.register();
            new Thread(() -> {
                phaser.arriveAndAwaitAdvance(); // should be before task running
                task.run();
            }, "t" + i).start();
        }

        System.out.println("before sleep");
        Thread.sleep(4_000);
        System.out.println("after sleep");
        phaser.arrive();

        Thread.sleep(1_000);
    }

    private static void recurrentBatchExecution() {
        printHeading();

        Runnable runnable = () -> {
            System.out.print(Thread.currentThread().getName() + " ");
        };

        var runnables = Collections.nCopies(10, runnable);

        System.out.print("0 : ");

        var phaser = new Phaser(1) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.print("\n" + (phase + 1) + " : ");
                return phase == 4 || registeredParties == 0;
            }
        };

        for (var i = 0; i < runnables.size(); i++) {
            var task = runnables.get(i);
            phaser.register();
            new Thread(() -> {
                do {
                    task.run();
                    phaser.arriveAndAwaitAdvance(); // should be after task running
                } while (!phaser.isTerminated());
            }, "t" + i).start();
        }

        while (!phaser.isTerminated()) {
            phaser.arriveAndAwaitAdvance();
        }
        System.out.println("end");
    }

    private static void buildTreeOfPhasers() throws InterruptedException {
        printHeading();

        var tasks = new ArrayList<Callable<Void>>();

        var root = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("phase advancing " + phase);
                return phase == 2;
            }
        };

        var resource = new AtomicInteger();

        buildTreeOfPhasers(root, 0, 5, 2, tasks, resource, 0);

        var executorService = Executors.newFixedThreadPool(tasks.size());
        executorService.invokeAll(tasks);

        executorService.shutdown();
    }

    private static void buildTreeOfPhasers(Phaser parrent,
                                           int lo,
                                           int hi,
                                           int tasksPerPhaser,
                                           List<Callable<Void>> tasks,
                                           AtomicInteger resource,
                                           int level) {

        if (hi - lo > tasksPerPhaser) {
            for (var i = lo; i < hi; i += tasksPerPhaser) {
                var j = Math.min(i + tasksPerPhaser, hi);
                var l = i / tasksPerPhaser;
                var phaser = new Phaser(parrent) {
                    @Override
                    protected boolean onAdvance(int phase, int registeredParties) {
                        System.out.println("isn't calling upon Phaser tiering");
                        return super.onAdvance(phase, registeredParties);
                    }

                    @Override
                    public String toString() {
                        return l + " " + super.toString();
                    }
                };
                buildTreeOfPhasers(phaser, i, j, tasksPerPhaser, tasks, resource, l);
            }
        } else {
            for (int i = lo; i < hi; i++) {
                parrent.register();
                tasks.add(
                        () -> {
                            do {
                                var phase = parrent.arriveAndAwaitAdvance();
                                if (level == 1) {
                                    Thread.sleep(4_000);
                                }

                                System.out.println(parrent + " " + phase + " " + resource.incrementAndGet());
//                                System.out.print(parrent + " " + resource.incrementAndGet() + " ");
                            } while (!parrent.isTerminated());

                            return null;
                        }
                );
            }
//            System.out.println(parrent);
        }
    }

    static void printHeading() {
        var heading = StackWalker.getInstance().walk(frames -> frames
                .skip(1)
                .findFirst()
                .map(StackWalker.StackFrame::getMethodName)
                .orElseThrow());

        System.out.println("\n-----" + heading + "------");
    }
}
