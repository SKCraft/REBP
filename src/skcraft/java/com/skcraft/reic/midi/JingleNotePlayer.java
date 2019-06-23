/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.midi;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class JingleNotePlayer implements Runnable {
    private Block block;
    private JingleSequencer sequencer;
    private final int playDistance = 4096;
    
    public JingleNotePlayer(Block block, JingleSequencer seq) {
        this.block = block;
        this.sequencer = seq;
    }
    
    public void run() {
        try {
            sequencer.run(this);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            sequencer.stop();
            sequencer = null;
        }
    }

    public void stop() {
        if (sequencer != null) {
            sequencer.stop();
        }
    }
    
    public void play(byte instrument, byte note) {
        for (Player player : block.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(block.getLocation()) <= playDistance) {
                player.playNote(block.getLocation(), instrument, note);
            }
        }
    }
}
