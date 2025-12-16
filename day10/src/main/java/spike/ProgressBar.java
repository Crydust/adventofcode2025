package spike;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class ProgressBar {

    private final AtomicInteger max = new AtomicInteger(0);
    private final AtomicInteger done = new AtomicInteger(0);
    private long startTime;
    private Timer timer;

    static ProgressBar startProgressBar() {
        ProgressBar pb = new ProgressBar();
        pb.start();
        return pb;
    }

    void start() {
        startTime = System.nanoTime();
        timer = new Timer("pb", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                printProgress();
            }
        }, 100, 250);
    }

    private void printProgress() {
        int d = done.get();
        int m = max.get();
        if (d == 0 || m == 0) {
            return;
        }
        System.out.printf(
                "\r[%-50s] %3d/%3d %,9d elapsed",
                "#".repeat((int) Math.floor((d * 50.0) / m)),
                d,
                m,
                NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }

    void incrementMax() {
        max.incrementAndGet();
    }

    void incrementDone() {
        done.incrementAndGet();
    }

    void stop() {
        timer.cancel();
        printProgress();
        System.out.println();
    }
}
