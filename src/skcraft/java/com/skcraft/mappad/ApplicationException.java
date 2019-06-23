/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

public class ApplicationException extends Exception {

    private static final long serialVersionUID = 6556928856614575575L;

    public ApplicationException() {
    }

    public ApplicationException(String msg) {
        super(msg);
    }

    public ApplicationException(Throwable t) {
        super(t);
    }

    public ApplicationException(String msg, Throwable t) {
        super(msg, t);
    }

}
