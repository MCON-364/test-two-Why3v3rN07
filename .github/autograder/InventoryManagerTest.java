package edu.touro.las.mcon364.test2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    // ---- addStock / getStock ------------------------------------------------

    @Test
    void addStockIncreasesStockForItem() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("widget", 10);
        assertEquals(10, mgr.getStock("widget"));
    }

    @Test
    void addStockAccumulatesAcrossMultipleCalls() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("bolt", 5);
        mgr.addStock("bolt", 3);
        assertEquals(8, mgr.getStock("bolt"));
    }

    @Test
    void getStockReturnsZeroForUnknownItem() {
        InventoryManager mgr = new InventoryManager();
        assertEquals(0, mgr.getStock("ghost"));
    }

    @Test
    void addStockThrowsOnZeroQty() {
        InventoryManager mgr = new InventoryManager();
        assertThrows(IllegalArgumentException.class, () -> mgr.addStock("x", 0));
    }

    @Test
    void addStockThrowsOnNegativeQty() {
        InventoryManager mgr = new InventoryManager();
        assertThrows(IllegalArgumentException.class, () -> mgr.addStock("x", -5));
    }

    // ---- removeStock --------------------------------------------------------

    @Test
    void removeStockReturnsTrueWhenStockSufficient() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("gear", 10);
        assertTrue(mgr.removeStock("gear", 4));
        assertEquals(6, mgr.getStock("gear"));
    }

    @Test
    void removeStockReturnsFalseWhenInsufficientStock() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("gear", 2);
        assertFalse(mgr.removeStock("gear", 5));
        assertEquals(2, mgr.getStock("gear"), "Stock must remain unchanged on failed removal");
    }

    @Test
    void removeStockDoesNotGoBelowZero() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("pin", 1);
        boolean first  = mgr.removeStock("pin", 1);
        boolean second = mgr.removeStock("pin", 1);
        assertTrue(first,  "First removal should succeed");
        assertFalse(second, "Second removal should fail - stock is 0");
        assertEquals(0, mgr.getStock("pin"));
    }

    @Test
    void removeStockThrowsOnZeroQty() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("x", 5);
        assertThrows(IllegalArgumentException.class, () -> mgr.removeStock("x", 0));
    }

    // ---- getTotalUnitsAdded -------------------------------------------------

    @Test
    void totalUnitsAddedReflectsAllAdditions() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("a", 10);
        mgr.addStock("b", 20);
        mgr.addStock("a", 5);
        assertEquals(35, mgr.getTotalUnitsAdded());
    }

    @Test
    void totalUnitsAddedNotAffectedByRemovals() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("c", 50);
        mgr.removeStock("c", 20);
        assertEquals(50, mgr.getTotalUnitsAdded(),
                "getTotalUnitsAdded counts units ADDED, not current stock");
    }

    // ---- getSnapshot --------------------------------------------------------

    @Test
    void getSnapshotIsUnmodifiable() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("item", 3);
        Map<String, Integer> snap = mgr.getSnapshot();
        assertThrows(UnsupportedOperationException.class,
                () -> snap.put("item", 999));
    }

    @Test
    void getSnapshotContainsCurrentInventory() {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("alpha", 7);
        mgr.addStock("beta", 13);
        Map<String, Integer> snap = mgr.getSnapshot();
        assertEquals(7,  snap.get("alpha"));
        assertEquals(13, snap.get("beta"));
    }

    // ---- concurrency --------------------------------------------------------

    @Test
    void concurrentAddStockDoesNotLoseUpdates() throws InterruptedException {
        InventoryManager mgr = new InventoryManager();
        int threads = 20;
        int addsPerThread = 100;
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                for (int j = 0; j < addsPerThread; j++) {
                    mgr.addStock("screw", 1);
                }
            }));
        }
        workers.forEach(Thread::start);
        for (Thread t : workers) t.join();
        assertEquals(threads * addsPerThread, mgr.getStock("screw"),
                "No stock updates should be lost under concurrent addStock");
    }

    @Test
    void concurrentRemoveNeverGoesNegative() throws InterruptedException {
        InventoryManager mgr = new InventoryManager();
        mgr.addStock("token", 50);
        int threads = 20;
        AtomicInteger successCount = new AtomicInteger(0);
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                if (mgr.removeStock("token", 1)) successCount.incrementAndGet();
            }));
        }
        workers.forEach(Thread::start);
        for (Thread t : workers) t.join();
        int remaining = mgr.getStock("token");
        assertTrue(remaining >= 0, "Stock must never go negative");
        assertEquals(50 - successCount.get(), remaining,
                "Remaining stock = initial stock - successful removals");
    }

    @Test
    void totalUnitsAddedIsAccurateUnderConcurrency() throws InterruptedException {
        InventoryManager mgr = new InventoryManager();
        int threads = 10;
        int addsPerThread = 200;
        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            workers.add(new Thread(() -> {
                for (int j = 0; j < addsPerThread; j++) {
                    mgr.addStock("nail", 1);
                }
            }));
        }
        workers.forEach(Thread::start);
        for (Thread t : workers) t.join();
        assertEquals(threads * addsPerThread, mgr.getTotalUnitsAdded());
    }
}

