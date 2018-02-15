package com.myintlonghashmap;

public class Main {
    public static void main(String[] args) {

        IntLongHashMap intLongHashMap = new IntLongHashMap(1, 0.5f);

        intLongHashMap.put(1, 2);
        intLongHashMap.put(1, 3);
        intLongHashMap.put(2, 4);
        intLongHashMap.put(3, 5);

        System.out.println(intLongHashMap.get(1));
        System.out.println(intLongHashMap.get(2));
        System.out.println(intLongHashMap.get(3));

        System.out.println("intLongHashMap.size: " + intLongHashMap.size());
    }
}