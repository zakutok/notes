package aba_problem;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ABAProblem {

    static final class ABANode {
        private final String val;
        private ABANode next;

        ABANode(String val, ABANode next) {
            this.val = val;
            this.next = next;
        }

        @Override
        public String toString() {
            return "ABANode[" +
                    "val=" + val + ", " +
                    "next=" + next + ']';
        }

    }

    public static void main(String[] args) throws InterruptedException {
        var head = new ABANode("A", new ABANode("B", new ABANode("C", new ABANode("D", null))));

        System.out.println("Given list: " + head);

        var headAtomicReference = new AtomicReference<>(head);

        var t2 = new Thread(() -> {
            ABANode t2Head = null;
            var updated = false;
            while (!updated) {
                t2Head = headAtomicReference.get();
                var second = t2Head.next;

                updated = headAtomicReference.compareAndSet(t2Head, second);
                System.out.println("t2: updated = " + updated + ", list: " + headAtomicReference.get());
            }
            t2Head.next = null;
            var originalHead = t2Head;

            updated = false;
            while (!updated) {
                t2Head = headAtomicReference.get();
                var second = t2Head.next;

                updated = headAtomicReference.compareAndSet(t2Head, second);
                System.out.println("t2: updated = " + updated + ", list: " + headAtomicReference.get());
            }
            t2Head.next = null;

            updated = false;
            while (!updated) {
                var first = headAtomicReference.get();
                originalHead.next = first;

                updated = headAtomicReference.compareAndSet(first, originalHead);
                System.out.println("t2: updated = " + updated + ", list: " + headAtomicReference.get());
            }
        });

        var t1 = new Thread(() -> {
            var updated = false;
            ABANode t1Head = null;
            while (!updated) {
                t1Head = headAtomicReference.get();
                var second = t1Head.next;

                imitateABAProblem(t2); // just to guarantee thread order to reproduce ABA problem

                updated = headAtomicReference.compareAndSet(t1Head, second);
                System.out.println("t1: updated = " + updated + ", list: " + headAtomicReference.get());
            }
            t1Head.next = null;
        });

        t1.start();

        t1.join();
        t2.join();

        System.out.println("Result list: " + headAtomicReference.get());
    }

    private static void imitateABAProblem(Thread t2) {
        try {
            t2.start();
            t2.join(); // just to imitate ABA problem
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
