/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.world;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.TimeUtil;
import com.skcraft.reic.AbstractIC;
import com.skcraft.reic.AbstractICFactory;
import com.skcraft.reic.CreatedOnChunkLoad;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.State;

public class TimeTriggerIC extends AbstractIC {
    
    private int time;
    private long nextTime = 0;
    private boolean wasHigh = false;
    
    public TimeTriggerIC(Block block, State state, int time) {
        super(block, state);
        if (time < 0) {
            time += 24000;
        }
        this.time = time;
    }
    
    private void resetNext() {
        long now = getBlock().getWorld().getFullTime();
        long nowRel = now % 24000;
        if (nowRel < 0) nowRel += 24000;
        if (time > nowRel) {
            nextTime = now - nowRel + time;
        } else {
            nextTime = now - nowRel + 24000 + time;
        }
        setNext();
    }
    
    private void setNext() {
        long now = getBlock().getWorld().getFullTime();
        int delay = 5;
        if (nextTime - now > 20 * 30) {
            delay = 19 * 30;
        } else if (nextTime - now > 20 * 10) {
            delay = 19 * 9;
        }
        getState().setNextTick(delay);
    }

    public void initialize() {
        resetNext();
    }

    public void trigger() {
    }

    public void tick() {
        long now = getBlock().getWorld().getFullTime();
        if (wasHigh) {
            getState().out(0, false);
            wasHigh = false;
            resetNext();
        } else if (now >= nextTime) {
            wasHigh = true;
            getState().out(0, true);
            getState().setNextTick(4);
        } else {
            setNext();
        }
    }

    public void unload() {
    }
    
    public String getSummary() {
        return "Will trigger at around " + TimeUtil.getTimeString(time) + " for 4 ticks.";
    }

    public String getDebugInfo() {
        long now = getBlock().getWorld().getFullTime();
        return "Next trigger in " + (nextTime - now) + " ticks";
    }

    public static class TimeTriggerICFactory extends AbstractICFactory implements CreatedOnChunkLoad {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            int time;
            try {
                time = CommandUtil.matchTime(lines[1].trim());
            } catch (CommandException e) {
                throw new ICException(e.getMessage());
            }
            
            expectNoArg(lines, 2);
            expectNoArg(lines, 3);
            
            return new TimeTriggerIC(sign, family.createState(sign), time);
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Outputs a high for 4 ticks when the specified time of day is reached.";
        }

        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Outputs a high for 4 ticks when the specified time of day is reached.")
                    .param("Time string (3pm, 15:30, etc.)")
                    .output("HIGH when the time of day is reached");
        }
    }

}
