/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar.config;

/**
 * Configuration exception.
 * 
 * @author sk89q
 */
public class ConfigurationException extends Exception {
    
    private static final long serialVersionUID = -2442886939908724203L;

    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
    
}
