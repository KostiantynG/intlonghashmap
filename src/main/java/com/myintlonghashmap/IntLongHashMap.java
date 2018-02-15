package com.myintlonghashmap;

public class IntLongHashMap {

    /**
     * flag indicating that a slot in the hashtable is available
     */
    private static final byte FREE = 0;

    /**
     * flag indicating that a slot in the hashtable is occupied
     */
    private static final byte FULL = 1;

    /**
     * flag indicating that the value of a slot in the hashtable
     * was deleted
     */
    private static final byte REMOVED = 2;

    /**
     * flags indicating whether each position in the hash is
     * FREE, FULL, or REMOVED
     */
    private transient byte[] states;

    /**
     * the set of ints
     */
    private transient int[] set;

    /**
     * constants used for state flags
     */
    private boolean consumeFreeSlot;

    /**
     * the current number of free slots in the hash.
     */
    private transient int free;

    /**
     * the current number of occupied slots in the hash.
     */
    private transient int size;

    /**
     * The maximum number of elements allowed without allocating more
     * space.
     */
    private int maxSize;

    private int capacity;
    private float loadFactor;
    /**
     * the values of the map
     */
    private transient long[] values;

    private long no_entry_value = (long) 0;


    /**
     * Creates a new <code>IntLongHashMap</code> instance with a prime
     * capacity equal <tt>initialCapacity</tt> and
     * with the load factor equal <tt>loadFactor</tt>.
     *
     * @param initialCapacity an <code>int</code> value
     */
    public IntLongHashMap(int initialCapacity, float loadFactor) {
        capacity = initialCapacity;
        states = new byte[capacity];
        set = new int[capacity];
        values = new long[capacity];
    }

    public long put(int key, long value) {
        int index = insertKey(key);
        return doPut(value, index);
    }

    public long get(int key) {
        int index = index(key);
        return index < 0 ? no_entry_value : values[index];
    }

    public int size() {
        return size;
    }

    private int insertKey(int val) {
        int hash, index;

        hash = val & 0x7fffffff;
        index = hash % states.length;
        byte state = states[index];

        consumeFreeSlot = false;

        if (state == FREE) {
            consumeFreeSlot = true;
            insertKeyAt(index, val);

            return index;       // empty, all done
        }

        if (state == FULL && set[index] == val) {
            return -index - 1;   // already stored
        }

        // already FULL or REMOVED, must probe
        return insertKeyRehash(val, index, hash, state);
    }

    private int insertKeyRehash(int val, int index, int hash, byte state) {
        // compute the double hash
        final int length = set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;
        int firstRemoved = -1;

        /*
         * Look until FREE slot or we start to loop
         */
        do {
            // Identify first removed slot
            if (state == REMOVED && firstRemoved == -1)
                firstRemoved = index;

            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = states[index];

            // A FREE slot stops the search
            if (state == FREE) {
                if (firstRemoved != -1) {
                    insertKeyAt(firstRemoved, val);
                    return firstRemoved;
                } else {
                    consumeFreeSlot = true;
                    insertKeyAt(index, val);
                    return index;
                }
            }

            if (state == FULL && set[index] == val) {
                return -index - 1;
            }

            // Detect loop
        } while (index != loopIndex);

        // We inspected all reachable slots and did not find a FREE one
        // If we found a REMOVED slot we return the first one found
        if (firstRemoved != -1) {
            insertKeyAt(firstRemoved, val);
            return firstRemoved;
        }
        else postInsertHook(false);

        throw new IllegalStateException("No free or removed slots available. Key set full?!!");
    }
    private void insertKeyAt(int index, int val) {
        set[index] = val;  // insert value
        states[index] = FULL;
    }

    private long doPut(long value, int index) {
        long previous = no_entry_value;
        boolean isNewMapping = true;
        if (index < 0) {
            index = -index - 1;
            previous = values[index];
            isNewMapping = false;
        }
        values[index] = value;

        if (isNewMapping) {
            postInsertHook(consumeFreeSlot);
        }

        return previous;
    }

    private int capacity() {
        return set.length;
    }

    private void postInsertHook(boolean usedFreeSlot) {
        if ( usedFreeSlot ) {
            free--;
        }

        // rehash whenever we exhaust the available space in the table
        if ( ++size > maxSize || free == 0 ) {
            // choose a new capacity suited to the new state of the table
            // if we've grown beyond our maximum size, double capacity;
            // if we've exhausted the free spots, rehash to the same capacity,
            // which will free up any stale removed slots for reuse.
            int newCapacity = size > maxSize ? PrimeFinder.nextPrime( capacity() << 1 ) : capacity();
            rehash( newCapacity );
            computeMaxSize( capacity() );
        }
    }

    private void computeMaxSize(int capacity) {
        // need at least one free slot for open addressing
        maxSize = Math.min(capacity - 1, (int) (capacity * loadFactor));
        free = capacity - size; // reset the free element count
    }

    private void rehash(int newCapacity) {
        int oldCapacity = set.length;

        int oldKeys[] = set;
        long oldValues[] = values;
        byte oldStates[] = states;

        set = new int[newCapacity];
        values = new long[newCapacity];
        states = new byte[newCapacity];

        for (int i = oldCapacity; i-- > 0; ) {
            if (oldStates[i] == FULL) {
                int o = oldKeys[i];
                int index = insertKey(o);
                values[index] = oldValues[i];
            }
        }
    }

    private int index(int key) {
        int hash, index, length;

        final byte[] states = this.states;
        final int[] set = this.set;
        length = states.length;
        hash = key & 0x7fffffff;
        index = hash % length;
        byte state = states[index];

        if (state == FREE)
            return -1;

        if (state == FULL && set[index] == key)
            return index;

        return indexRehashed(key, index, hash);
    }

    private int indexRehashed(int key, int index, int hash) {
        int length = set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;

        do {
            index -= probe;
            if (index < 0) {
                index += length;
            }
            byte state = states[index];
            //
            if (state == FREE)
                return -1;

            //
            if (key == set[index] && state != REMOVED)
                return index;
        } while (index != loopIndex);

        return -1;
    }
}