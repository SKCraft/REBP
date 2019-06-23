/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.concurrent.LinkedBlockingDeque;

public class BoundedRingBuffer<E> extends LinkedBlockingDeque<E> {

    private static final long serialVersionUID = -3509666250777291842L;

    public BoundedRingBuffer() {
        super();
    }

    public BoundedRingBuffer(int capacity) {
        super(capacity);
    }

    @Override
    public synchronized boolean offerFirst(E e) {
        if (remainingCapacity() == 0) {
            removeLast();
        }
        super.offerFirst(e);
        return true;
    }
}