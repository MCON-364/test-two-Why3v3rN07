package edu.touro.las.mcon364.test2;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Problem 2 of 3
 *
 * A TaskDispatcher processes strings using multiple threads. Thread number is limited.
 * Each worker upper-cases the task string and records the result.
 * The results list and the completed counter must always be in sync.
 *
 * TODO 1 — pool
 *   Create a thread pool whose size is capped at POOL_SIZE.
 *
 * TODO 2 — lock
 *   Choose a lock that allows you to explicitly acquire and release it,
 *
 *
 * TODO 3 — dispatch(List<String> tasks)
 *   Hand each task off to the pool. The work each thread does is:
 *     (a) upper-case the string
 *     (b) record the result by calling recordResult()
 *     (c) return the result
 *   Give back a handle to each piece of work so the caller can retrieve
 *   the results later. Do not wait for the results here.
 *
 * TODO 4 — recordResult(String result)
 *   The results list and completedCount must never get out of sync.
 *   Make sure no other thread can come in between updating one and the other.
 *   Always release the lock even if something goes wrong.
 *
 * TODO 5 — shutdown()
 *   Stop accepting new work and wait up to 10 seconds for running tasks to finish.
 *
 * TODO 6 — getResults() / getCompletedCount()
 *   Reads must be guarded the same way writes are.
 *   getResults() must return a copy so callers cannot modify internal state.
 *   
 */
public class TaskDispatcher {

    public static final int POOL_SIZE = 4;

    // TO-DO 1: replace null with an appropriate class
    private final ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    // TO-DO 2: replace null — which Lock implementation lets you lock and unlock explicitly?
    private final Lock lock = new ReentrantLock();

    // provided — do not change
    private final List<String> results = new ArrayList<>();
    private int completedCount = 0;

    /*
     *   Hand each task off to the pool. The work each thread does is:
     *     (a) upper-case the string
     *     (b) record the result by calling recordResult()
     *     (c) return the result
     *   Give back a handle to each piece of work so the caller can retrieve
     *   the results later. Do not wait for the results here.
     *   You have to use streams! ...why?
     */
    public List<Future<String>> dispatch(List<String> tasks) {
        //TODO 3
        List<Future<String>> futures = new ArrayList<>();
        tasks.stream().forEach(s -> futures.add(pool.submit(() -> {
            String result = s.toUpperCase();
            recordResult(result);
            System.out.println(result);
            return result;
        })));
        System.out.println(futures);
        return futures;
    }

    public void recordResult(String result) {
        //TO-DO 4
        lock.lock();
        try {
            results.add(result);
            completedCount++;
        } finally {
            lock.unlock();
        }

    }

    public void shutdown() throws InterruptedException {
        //TO-DO 5
        pool.shutdown();
        boolean done = pool.awaitTermination(10, TimeUnit.SECONDS);
    }

    public List<String> getResults() {
        //TO-DO 6
        List<String> result= new ArrayList<>();
        lock.lock();
        try {
            result.addAll(results);
        } finally {
            lock.unlock();
        }
        return result;
    }

    public int getCompletedCount() {
        //TO-DO 6
        int count;
        lock.lock();
        try {
            count = completedCount;
        } finally {
            lock.unlock();
        }
        return count;
    }

}
