package org.suricsun.util.dsa;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author: SuricSun
 * @date: 2021/8/28
 */
public class StackS<T> {

    private int capacity = 100;
    private T[] array;

    private int capacityPosZeroBased = 0;
    private int topElemPosZeroBased = -1;

    public StackS(Class<T> clazz) {

        this.capacityPosZeroBased = this.capacity - 1;
        this.array = (T[]) Array.newInstance(clazz, this.capacity);
    }

    public StackS(Class<T> clazz, int initialCapacity) {

        this.capacity = initialCapacity;
        this.capacityPosZeroBased = this.capacity - 1;
        this.array = (T[]) Array.newInstance(clazz, this.capacity);
    }

    public void push(T value) {

        this.topElemPosZeroBased++;
        if (this.topElemPosZeroBased > this.capacityPosZeroBased) {

            this.makeThisCapacityBigger(0.5f);
        }
        this.array[this.topElemPosZeroBased] = value;
    }

    public T pop() {

        if (this.topElemPosZeroBased == 0) {

            //没得pop了返回null
            return null;
        }

        this.topElemPosZeroBased--;
        T toBeReturned = this.array[this.topElemPosZeroBased];
        this.array[this.topElemPosZeroBased] = null;
        this.topElemPosZeroBased--;
        return toBeReturned;
    }

    public void pop(int cnt) {

        for (int i = 0; i < cnt; i++) {

            this.array[this.topElemPosZeroBased] = null;
            this.topElemPosZeroBased--;
            if (this.topElemPosZeroBased <= -1) {

                //没得push了
                return;
            }
        }
    }

    public void makeThisCapacityBigger(float factor) {

        int newAddedCapacity = (int) (factor * (float) this.capacity);
        if (newAddedCapacity <= 0) {

            //防止factor或capacity太小取整为0
            newAddedCapacity = 1;
        }
        this.capacity += newAddedCapacity;
        this.capacityPosZeroBased = this.capacity - 1;
        this.array = Arrays.copyOf(this.array, this.capacity);
    }

    public T get(int index) {

        return this.array[index];
    }

    public void set(int index, T value) {

        this.array[index] = value;
    }

    public T getTopElem() {

        return this.array[this.topElemPosZeroBased];
    }

    public int getCapacityPosZeroBased() {
        return capacityPosZeroBased;
    }

    public int getTopElemPosZeroBased() {
        return topElemPosZeroBased;
    }

    public int size() {

        return this.topElemPosZeroBased + 1;
    }
}
