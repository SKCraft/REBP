/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

public class LoaderException extends Exception {
    private static final long serialVersionUID = -6063237875649018450L;

    public LoaderException() {
        super();
    }

    public LoaderException(String message, Throwable t) {
        super(message, t);
    }

    public LoaderException(String message) {
        super(message);
    }

    public LoaderException(Throwable t) {
        super(t);
    }
}