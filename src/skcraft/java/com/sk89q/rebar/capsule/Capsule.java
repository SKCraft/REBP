/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

/**
 * A self-contained component that can be readily loaded or unloaded, providing
 * for very agile development.
 */
public interface Capsule {

    /**
     * Called when the capsule is being initialized.
     */
    void initialize();

    /**
     * Called when the capsule is being shutdown.
     */
    void shutdown();


}
