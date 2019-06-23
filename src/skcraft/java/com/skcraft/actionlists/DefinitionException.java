/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import com.sk89q.rebar.config.LoaderBuilderException;

public class DefinitionException extends LoaderBuilderException {

    private static final long serialVersionUID = -5429457695711163632L;

    public DefinitionException() {
    }

    public DefinitionException(String message) {
        super(message);
    }

    public DefinitionException(Throwable cause) {
        super(cause);
    }

    public DefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

}
