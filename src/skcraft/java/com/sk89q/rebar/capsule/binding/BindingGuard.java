/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule.binding;

import lombok.NonNull;
import lombok.extern.java.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.logging.Level;

@Log
public class BindingGuard {

    private final Set<Entry> bound = new HashSet<>();

    public synchronized Entry add(@NonNull Runnable runnable) {
        Entry entry = new Entry(runnable);
        bound.add(entry);
        return entry;
    }

    public synchronized Timer add(@NonNull final Timer timer) {
        add(new Runnable() {
            @Override
            public void run() {
                timer.cancel();
            }
        });
        return timer;
    }

    public synchronized void unbind() {
        for (Entry entry : bound) {
            try {
                entry.unbind();
            } catch (Throwable t) {
                BindingGuard.log.log(Level.WARNING, "Failed to unbind", t);
            }
        }
        bound.clear();
    }

    public class Entry {
        private final Runnable runnable;

        public Entry(Runnable runnable) {
            this.runnable = runnable;
        }

        private void unbind() {
            runnable.run();
        }

        public synchronized void remove() {
            bound.remove(this);
        }
    }


}
