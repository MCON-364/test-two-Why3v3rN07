package edu.touro.las.mcon364.test2;

import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.stream.Collectors;

/**
 * ══════════════════════════════════════════════════════════════
 * Problem 3 of 3
 * ══════════════════════════════════════════════════════════════
 * <p>
 * A reporting system receives multiple batches of transactions.
 * The batches can be processed independently, and the results must be combined
 * into a single ReportSummary.
 * <p>
 * Your job is to choose an appropriate concurrency design pattern from the ones
 * we studied and apply it correctly.
 * <p>
 * Each inner list represents one batch of transactions.
 * <p>
 * Requirements:
 * - Process multiple batches concurrently.
 * - Each batch must be processed exactly once.
 * - Do not use parallelStream()
 * - Do not use synchronized keyword on methods or blocks.
 * - Track how many batches were actually processed using a thread-safe mechanism
 * in the integer field numberOfBatchesProcessed
 * - Start all available work before waiting for final results.
 * - Shut down any concurrency resources you create.
 */
public class ParallelReportBuilder {

    /**
     * Simple domain object. Do not modify.
     */
    public record Transaction(String id, int amount) {
    }

    /**
     * Do not modify.
     */
    public record BatchStats(long totalAmount,
                             long transactionCount,
                             int maxTransactionAmount,
                             int minTransactionAmount) {
    }

    /**
     * Do not modify.
     */
    public record ReportSummary(long totalAmount,
                                long totalCount,
                                int globalMax,
                                int globalMin,
                                int batchesProcessed) {
    }


    // TODO 1: declare and initialize private thread-safe progress tracking state called numberOfBatchesProcessed
    private AtomicInteger numberOfBatchesProcessed = new AtomicInteger(0);

    /*
     * TODO 2 — generateReport(List<List<Transaction>> batches, int workers)
     *
     * For each batch, compute:
     * - totalAmount
     * - transactionCount
     * - maxTransactionAmount
     * - minTransactionAmount
     *   (Hint: summaryStatistics())
     * Then combine all BatchStats objects into one ReportSummary containing:
     * - total amount across all batches
     * - total number of transactions
     * - global maximum transaction amount
     * - global minimum transaction amount
     * - number of batches processed
     *
     * Think carefully about:
     * - which concurrency pattern best matches independent tasks
     * - which java.util.concurrent classes support that pattern
     * - how to safely update shared progress
     * - how to avoid waiting too early
     * - how to handle empty batches or an empty input list
     */
    public ReportSummary generateReport(List<List<Transaction>> batches, int workers)
            throws InterruptedException, ExecutionException, IllegalArgumentException {

        // TO-DO 2A: validate inputs where appropriate
        if (batches == null || batches.isEmpty()) {
            throw new IllegalArgumentException("batches must not be null or empty");
        }
        if (workers <= 0) {
            throw new IllegalArgumentException("workers must be positive");
        }

        // TODO 2B: create the concurrency structure needed for the pattern you chose
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        AtomicReference<ArrayList<BatchStats>> allbs = new AtomicReference<>(new ArrayList<>());

        // TODO 2C: submit or assign one unit of work per batch
        // Each unit of work should:
        // - compute BatchStats for that batch
        // - safely record that one more batch has been processed
        // - you have to use streams here

        batches.stream().forEach((batch) -> {
            pool.submit(() -> {
                IntSummaryStatistics hi = batch.stream().collect(Collectors.summarizingInt(Transaction::amount));
                BatchStats bs = new BatchStats(hi.getSum(), hi.getCount(), hi.getMin(), hi.getMax());
                allbs.get().add(bs);
                numberOfBatchesProcessed.incrementAndGet();
            });
        });


        long totalAmount = 0;
        long totalCount = 0;
        int globalMax = Integer.MIN_VALUE;
        int globalMin = Integer.MAX_VALUE;

        // TO-DO 2D: after all work has been started, collect results
        // and combine them into the summary variables above
        // you don't have to use streams here. In this case for loop is acceptable
        for (BatchStats bs : allbs.get()) {
            totalAmount += bs.totalAmount;
            totalCount += bs.transactionCount;
            globalMax = Math.max(globalMax, bs.maxTransactionAmount);
            globalMin = Math.min(globalMin, bs.minTransactionAmount);
        }

        // TO-DO 2E: shut down any concurrency resources you created
        pool.shutdown();

        // TO-DO 2F: return the completed ReportSummary
        return new ReportSummary(totalAmount, totalCount, globalMax, globalMin, globalMax);
    }

    /*
     * TO-DO 3 — getProcessedBatchCount()
     *
     * Return the current number of batches processed.
     */
    public int getProcessedBatchCount() {
        return numberOfBatchesProcessed.get();
    }
}
