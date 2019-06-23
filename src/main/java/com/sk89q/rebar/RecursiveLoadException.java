/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar;

public class RecursiveLoadException extends LoaderException {
    
    private static final long serialVersionUID = -8156168786071923024L;

    public RecursiveLoadException() {
        super();
    }

    public RecursiveLoadException(String message, Throwable t) {
        super(message, t);
    }

    public RecursiveLoadException(String message) {
    }

    public RecursiveLoadException(Throwable t) {
        super(t);
    }

}
