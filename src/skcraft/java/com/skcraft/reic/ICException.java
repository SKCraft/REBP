/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

public class ICException extends Exception {

    private static final long serialVersionUID = -5352562254778392182L;

    public ICException(String message) {
        super(message);
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }

}
