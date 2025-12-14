package spike;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

final class Stopwatch {
    private final long startTime;
    private final long endTime;
    private final boolean stopped;

    private Stopwatch(long startTime, long endTime, boolean stopped) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.stopped = stopped;
    }

    public static Stopwatch start() {
        return new Stopwatch(System.nanoTime(), -1L, false);
    }

    public Stopwatch stop() {
        return new Stopwatch(this.startTime, System.nanoTime(), true);
    }

    @Override
    public String toString() {
        return "Total time: " + TimeUnit.NANOSECONDS.toMillis(currentEndTime() - startTime) + " ms";
    }

    public boolean hasExceeded(Duration duration) {
        return currentDuration().compareTo(duration) > 0;
    }

    private Duration currentDuration() {
        return Duration.ofNanos(currentEndTime() - startTime);
    }

    private long currentEndTime() {
        return stopped ? endTime : System.nanoTime();
    }

}
