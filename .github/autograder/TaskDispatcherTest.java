package edu.touro.las.mcon364.test2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class TaskDispatcherTest {

    private final TaskDispatcher dispatcher = new TaskDispatcher();

    @AfterEach
    void tearDown() throws InterruptedException {
        dispatcher.shutdown();
    }

    // ---- dispatch -----------------------------------------------------------

    @Test
    void dispatchReturnsOneFuturePerTask() throws Exception {
        List<Future<String>> futures = dispatcher.dispatch(List.of("a", "b", "c"));
        assertNotNull(futures);
        assertEquals(3, futures.size());
    }

    @Test
    void dispatchUpperCasesEachTask() throws Exception {
        List<Future<String>> futures = dispatcher.dispatch(List.of("hello", "world"));
        for (Future<String> f : futures) {
            String result = f.get();
            assertEquals(result.toUpperCase(), result);
        }
    }

    @Test
    void dispatchDoesNotBlockCaller() {
        List<String> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) tasks.add("task-" + i);
        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5),
                () -> dispatcher.dispatch(tasks),
                "dispatch() blocked — did you call get() inside it?");
    }

    // ---- recordResult / consistency -----------------------------------------

    @Test
    void completedCountMatchesResultsSize() throws Exception {
        List<Future<String>> futures = dispatcher.dispatch(List.of("x", "y", "z"));
        for (Future<String> f : futures) f.get();
        Thread.sleep(50);
        assertEquals(dispatcher.getCompletedCount(), dispatcher.getResults().size(),
                "completedCount and results.size() must always match");
    }

    @Test
    void resultsContainAllProcessedValues() throws Exception {
        List<Future<String>> futures = dispatcher.dispatch(List.of("alpha", "beta", "gamma"));
        for (Future<String> f : futures) f.get();
        Thread.sleep(50);
        List<String> results = dispatcher.getResults();
        assertTrue(results.contains("ALPHA"));
        assertTrue(results.contains("BETA"));
        assertTrue(results.contains("GAMMA"));
    }

    @Test
    void getResultsReturnsUnmodifiableCopy() throws Exception {
        dispatcher.dispatch(List.of("one")).get(0).get(1, java.util.concurrent.TimeUnit.SECONDS);
        Thread.sleep(50);
        List<String> copy = dispatcher.getResults();
        assertThrows(UnsupportedOperationException.class, () -> copy.add("injected"),
                "getResults() must return an unmodifiable copy");
    }

    // ---- concurrency --------------------------------------------------------

    @Test
    void noResultsLostUnderConcurrency() throws Exception {
        int taskCount = 40;
        List<String> tasks = new ArrayList<>();
        for (int i = 0; i < taskCount; i++) tasks.add("item-" + i);

        List<Future<String>> futures = dispatcher.dispatch(tasks);
        for (Future<String> f : futures) f.get();
        Thread.sleep(100);

        assertEquals(taskCount, dispatcher.getCompletedCount());
        assertEquals(taskCount, dispatcher.getResults().size());
    }

    @Test
    void threadPoolIsBoundedByPoolSize() throws Exception {
        // Submit enough tasks that reuse of threads is certain if the pool is fixed-size.
        // If the pool were unbounded, this would not necessarily fail, but a correct
        // fixed pool must complete all tasks within POOL_SIZE threads.
        int taskCount = 20;
        List<Future<String>> futures = dispatcher.dispatch(Collections.nCopies(taskCount, "t"));
        for (Future<String> f : futures) f.get();
        Thread.sleep(50);
        assertEquals(taskCount, dispatcher.getCompletedCount(),
                "All tasks must complete — check your ExecutorService choice");
    }
}

