package cla33ic.casefetcher.util;

/**
 * A simple rate limiter that ensures a minimum interval between consecutive acquisitions.
 */
public class RateLimiter {
    private final long intervalMillis;
    private long lastRequestTime = 0;

    /**
     * Constructs a RateLimiter with the specified interval in milliseconds.
     * @param intervalMillis the minimum interval between requests in milliseconds.
     */
    public RateLimiter(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    /**
     * Acquires permission to proceed. If the time since the last acquisition is less than
     * the specified interval, this method will block until the interval has passed.
     */
    public synchronized void acquire() {
        long now = System.currentTimeMillis();
        long waitTime = intervalMillis - (now - lastRequestTime);
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
