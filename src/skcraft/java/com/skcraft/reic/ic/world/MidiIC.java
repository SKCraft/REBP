/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.world;

import java.io.File;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Sign;

import com.skcraft.reic.AbstractIC;
import com.skcraft.reic.AbstractICFactory;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.RestrictedIC;
import com.skcraft.reic.State;
import com.skcraft.reic.midi.*;

public class MidiIC extends AbstractIC {
    
    private JingleNotePlayer jingleNote;
    private File file;
    
    public MidiIC(Block block, State state, File file) {
        super(block, state);
        this.file = file;
    }
    
    protected boolean play() {
        if (file.exists()) {
            try {
                JingleSequencer seq = new MidiJingleSequencer(file);
                jingleNote = new JingleNotePlayer(getState().getAttachedTo().getRelative(0, 1, 0), seq);
                Thread thread = new Thread(jingleNote, "ReIC MidiPlayerIC for " + file.getName());
                thread.start();
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }
    
    protected boolean stop() {
        if (jingleNote != null) {
            jingleNote.stop();
            jingleNote = null;
            return true;
        }
        
        return false;
    }

    public void trigger() {
        State state = getState();
        
        if (state.triggered(0)) {
            stop();
            
            if (state.in(0)) {
                state.out(0, play());
            } else {
                state.out(0, false);
            }
        }
    }

    public void unload() {
        stop();
    }
    
    public String getSummary() {
        return "Will play " + file.getName() + " when triggered.";
    }

    public String getDebugInfo() {
        if (file.exists()) {
            return file.getName() + " exists";
        } else {
            return file.getName() + " DOES NOT EXIST (check <ROOT>/midi)";
        }
    }

    public static class MidiICFactory extends AbstractICFactory implements RestrictedIC {
        private static final Pattern namePattern = Pattern.compile("^[A-Za-z0-9_\\\\-]+$");
        
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            File file = validateFile(sign, lines[1]);
            
            expectNoArg(lines, 2);
            expectNoArg(lines, 3);
            
            return new MidiIC(sign, family.createState(sign), file);
        }
        
        protected File validateFile(Block sign, String line) throws ICException {
            String filename = line.trim();

            if (!namePattern.matcher(filename).matches()) {
                throw new ICException("MIDI filenames can only contain A-Z, a-z, _ and - characters.");
            }
            
            File file = new File("midi", filename + ".mid");
            
            if (!file.exists()) {
                throw new ICException("<ROOT>/midi/" + file.getName() + " does not exist.");
            }
            
            Sign signMat = (Sign) sign.getState().getData();

            if (sign.getRelative(signMat.getAttachedFace()).getRelative(0, 1, 0).getType() != Material.NOTE_BLOCK) {
                throw new ICException("This IC must be attached to a block that is below a note block.");
            }
            
            return file;
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Plays a specified MIDI file.";
        }
        
        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Plays a specified MIDI file in <ROOT>/midi/*.mid. " +
                            "If the MIDI file doesn't exist, the IC will do nothing.")
            		.param("Filename (without .mid)\nOut #1: High if the file could be played")
                    .input("Play if HIGH, stop if LOW")
                    .output("HIGH if playing, LOW if not playing");
        }
    }

}
