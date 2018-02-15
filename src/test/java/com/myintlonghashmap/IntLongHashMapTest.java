package com.myintlonghashmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IntLongHashMapTest {

    @Test
    public void get() {
        final IntLongHashMap intLongHashMap = new IntLongHashMap(1, 0.5f);

        intLongHashMap.put(1, 2);
        intLongHashMap.put(1, 3);
        assertEquals(3, intLongHashMap.get(1));
        intLongHashMap.put(2, 4);
        assertEquals(4, intLongHashMap.get(2));
        intLongHashMap.put(3, 5);
        assertEquals(5, intLongHashMap.get(3));
    }

    @Test
    public void size() {
        final IntLongHashMap intLongHashMap = new IntLongHashMap(1, 0.5f);

        intLongHashMap.put(1, 2);
        intLongHashMap.put(1, 3);
        assertEquals(1, intLongHashMap.size());
        intLongHashMap.put(2, 4);
        assertEquals(2, intLongHashMap.size());
        intLongHashMap.put(3, 5);
        assertEquals(3, intLongHashMap.size());
    }
}