/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.world;

import java.io.File;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.RestrictedIC;
import com.skcraft.reic.State;

public class MidiPlayerIC extends MidiIC {
    
    public MidiPlayerIC(Block block, State state, File file) {
        super(block, state, file);
    }

    public void trigger() {
        State state = getState();
        
        if (state.triggered(0)) {
            if (state.in(0)) {
                stop();
                state.out(0, play());
            }
        } else if (state.triggered(1)) {
            if (state.in(1)) {
                stop();
                state.out(0, false);
            }
        }
    }

    public void unload() {
        stop();
    }

    public static class MidiPlayerICFactory extends MidiICFactory implements RestrictedIC {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            File file = validateFile(sign, lines[1]);
            
            expectNoArg(lines, 2);
            expectNoArg(lines, 3);
            
            return new MidiPlayerIC(sign, family.createState(sign), file);
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Plays a specified MIDI file, with separate start and stop inputs.";
        }
        
        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Plays a specified MIDI file in <ROOT>/midi/*.mid. " +
                            "If the MIDI file doesn't exist, the IC will do nothing.")
                    .param("Filename (without .mid)")
                    .input("Play if HIGH, do nothing if LOW")
                    .input("Stop if HIGH, do nothing if LOW")
                    .output("HIGH if playing, LOW if not playing");
        }
    }

}
