package edu.touro.las.mcon364.test2;

import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════════
 *  Problem 1 of 3
 * ══════════════════════════════════════════════════════════════
 *
 * SCENARIO
 * --------
 * A warehouse system lets many threads update item stock simultaneously.
 * You must make the {@code InventoryManager} thread-safe so that no stock
 * updates are lost and stock never goes negative.
 *
 * REQUIREMENTS  (read every TODO carefully — each is graded)
 *
 * 1. {@code addStock(String item, int qty)}
 *    - Adds {@code qty} units of {@code item} to the inventory.
 *    - {@code qty} must be > 0; throw {@link IllegalArgumentException} otherwise.
 *    - Must be thread-safe under concurrent calls.
 *    - Also increments the running {@code totalUnitsAdded} counter atomically.
 *
 * 2. {@code removeStock(String item, int qty)}
 *    - Removes {@code qty} units of {@code item} if sufficient stock exists.
 *    - Returns {@code true} if the removal succeeded, {@code false} if the
 *      current stock is less than {@code qty} (do NOT go negative).
 *    - {@code qty} must be > 0; throw {@link IllegalArgumentException} otherwise.
 *    - Must be thread-safe: two threads must not both succeed when only one
 *      unit of stock remains and both try to remove one unit.
 *    - Hint: once you have chosen the right Map implementation, its
 *      {@code compute()} method lets you read and write atomically.
 *
 * 3. {@code getStock(String item)}
 *    - Returns the current stock for {@code item}, or {@code 0} if the item
 *      has never been added.
 *
 * 4. {@code getTotalUnitsAdded()}
 *    - Returns the running total of every unit ever added across all items.
 *    - Must reflect concurrent additions accurately but you cannot mark the method as synchronized.
 *
 * 5. {@code getSnapshot()}
 *    - Returns an unmodifiable copy of the current inventory so callers
 *      cannot mutate internal state.
 *    - Hint: {@link Map#copyOf(Map)}.
 *
 *
 * DO NOT use any other concurrency utilities.
 */
public class InventoryManager {

    // TODO: initialise this field with a thread-safe Map implementation
    //       — which Map implementation from the lesson guarantees thread-safe reads and writes?
    private final Map<String, Integer> stock = null; 

    // TODO: declare and initialise a private final field called totalUnitsAdded that tracks the
    //       running total of units ever added, thread-safely, without using synchronized


    /**
     * Adds {@code qty} units of {@code item} to inventory.
     *
     * @param item the item name (non-null, non-blank)
     * @param qty  number of units to add (must be > 0)
     * @throws IllegalArgumentException if qty ≤ 0
     */
    public void addStock(String item, int qty) {
        // TODO: validate qty > 0

        // TODO: atomically add qty to the item's current stock
        //       Hint: the thread-safe Map implementation you chose has a merge() method
        //             that can do this in one atomic step

        // TODO: atomically add qty to totalUnitsAdded

    }

    /**
     * Removes {@code qty} units of {@code item} if sufficient stock exists.
     *
     * @param item the item name
     * @param qty  number of units to remove (must be > 0)
     * @return {@code true} if removal succeeded; {@code false} if insufficient stock
     * @throws IllegalArgumentException if qty ≤ 0
     */
    public boolean removeStock(String item, int qty) {
        // TODO: validate qty > 0


        // TODO: atomically check-and-decrement.
        //       If current stock >= qty, subtract qty.
        //       Otherwise, leave stock unchanged.
        //       Return true if stock was depleted, false if unchanged
        //       Hint: your chosen Map has a compute() method that lets you
        //             read and write in one atomic step.

        return false; //placeholder
    }

    /**
     * Returns the current stock for {@code item}, or 0 if unknown.
     */
    public int getStock(String item) {
       return 0; //placeholder
    }

    /**
     * Returns the cumulative number of units ever added (all items combined).
     */
    public int getTotalUnitsAdded() {
        return 0; //placeholder
    }

    /**
     * Returns an unmodifiable snapshot of the current inventory.
     * Callers cannot use the returned map to change internal state.
     */
    public Map<String, Integer> getSnapshot() {
        // TODO: return a defensive copy
        return null; //placeholder
    }
}

