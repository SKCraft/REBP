/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.event;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class ListenerRegistrationComparator implements Comparator<ListenerRegistration> {

    @Override
    public int compare(ListenerRegistration o1, ListenerRegistration o2) {
        if (o2.getPriority() == o1.getPriority()) {
            return 0;
        } else if (o1.getPriority() > o2.getPriority()) {
            return -1;
        } else {
            return 1;
        }
    }

}
