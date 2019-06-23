
/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.midi;

public interface JingleSequencer {
    public void run(JingleNotePlayer player) throws InterruptedException;
    public void stop();
}
