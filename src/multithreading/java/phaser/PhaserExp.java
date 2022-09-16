package phaser;

import java.util.Collections;
import java.util.concurrent.Phaser;

public class PhaserExp {

    public static void main(String[] args) throws InterruptedException {

        alternativeToCountDownLatch();
        recurrentBatchExecution();
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

    private static void recurrentBatchExecution() throws InterruptedException {
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

    static void printHeading() {
        var heading = StackWalker.getInstance().walk(frames -> frames
                .skip(1)
                .findFirst()
                .map(StackWalker.StackFrame::getMethodName)
                .orElseThrow());

        System.out.println("\n-----" + heading + "------");
    }
}
