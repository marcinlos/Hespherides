package hesp.gui;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

/**
 * @author marcinlos
 * 
 * Utility class for synchronously invoking methods in event dispatch thread 
 * without the hassle of directly dealing with synchronization necessary to
 * handle timeouts.
 */
public class Synchronous {
    
    private Synchronous() { 
        // private constructor to ensure non-constructibility
    }
    
    /**
     * Auxilary class implementing synchronous invocation with timeout.
     */
    private static class SwingRunner {
        private CountDownLatch lock = new CountDownLatch(1);
        private Runnable runnable;
        private long timeout;
        private TimeUnit unit;
        
        public SwingRunner(Runnable runnable) {
            this.runnable = runnable;
            this.timeout = Long.MAX_VALUE;
            this.unit = TimeUnit.MILLISECONDS;
        }
        
        public SwingRunner(Runnable runnable, long timeout, TimeUnit unit) {
            this.runnable = runnable;
            this.timeout = timeout;
            this.unit = unit;
        }
        
        public void execute() {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            runnable.run();
                        } finally {
                            lock.countDown();
                        }
                    }
                });
                boolean done = lock.await(timeout, unit);
                if (! done) {
                    // TimeoutException is checked :/
                    throw new RuntimeException("Timeout while waiting for" + 
                            " gui setup completion");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Schedules invocation of {@code runnable} in event dispatch thread, and
     * waits indefinitely for its execution. 
     * 
     * @param runnable Action to execute in event dispatch thread
     */
    public static void invoke(Runnable runnable) {
        SwingRunner runner = new SwingRunner(runnable);
        runner.execute();
    }
    
    /**
     * Schedules invocation of {@code runnable} in event dispatch thread, and
     * waits for its execution as specified by {@code timeout} and {@code unit}
     * arguments.
     * 
     * If a timeout occurs, {@code RuntimeException} with appropriate message 
     * is thrown.
     * 
     * <p>TODO: Replace it with more specific exception</p>
     * 
     * @param runnable Action to execute in event dispatch thread
     * @param timeout Maximum time to wait
     * @param unit The time unit of {@code timeout} argument
     */
    public static void invoke(Runnable runnable, long timeout, TimeUnit unit) {
        SwingRunner runner = new SwingRunner(runnable, timeout, unit);
        runner.execute();
    }
}
