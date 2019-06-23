/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.management.ClockMonitorMXBean;
import com.sk89q.rebar.management.ManagementUtils;
import com.sk89q.rebar.util.BoundedRingBuffer;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.StringUtil;

public class ClockMonitor extends AbstractComponent implements ClockMonitorMXBean {

    public static final int[] INTERVALS = new int[] {5, 30, 300};

    private long lastTime = -1;
    private Object timingLock = new Object();
    private BoundedRingBuffer<Integer> timings = new BoundedRingBuffer<Integer>(INTERVALS[INTERVALS.length - 1]);

    @Override
    public void initialize() {
        Rebar.getInstance().registerInterval(new TickTimer(), 0, 20);
        Rebar.getInstance().registerCommands(ClockCommands.class, this);
        ManagementUtils.register(this, "com.sk89q.skcraft.performance:type=ClockMonitor");
    }

    @Override
    public void shutdown() {
    }

    public TimingSnapshot getSnapshot() {
        synchronized (timingLock) {
            int[] collected = new int[INTERVALS.length];
            int collectedIndex = 0;
            int total = 0;
            int count = 0;

            for (Integer time : timings) {
                count++;
                total += time;

                if (count == INTERVALS[collectedIndex]) {
                    collected[collectedIndex] = total;
                    collectedIndex++;
                }
            }

            return new TimingSnapshot(collected, INTERVALS);
        }
    }

    public class TickTimer implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();

            synchronized (timingLock) {
                if (lastTime != -1) {
                    int diff = (int) (now - lastTime);
                    timings.offerFirst(diff);
                }

                lastTime = now;
            }
        }
    }

    public static class ClockCommands {
        private ClockMonitor component;

        public ClockCommands(ClockMonitor component) {
            this.component = component;
        }

        @Command(aliases = {"clock"}, min = 0, max = 0, desc = "Get clock status", flags = "a")
        @CommandPermissions("skcraft.perf.clock")
        public void clock(CommandContext context, CommandSender sender)
                throws CommandException {
            TimingSnapshot snap = component.getSnapshot();

            if (snap == null) {
                throw new CommandException("Timing information is not yet available.");
            }

            if (context.hasFlag('a')) {
                ChatUtil.msg(sender, ChatColor.GRAY, "Intervals: ", StringUtil.joinString(
                        snap.getMeasureIntervals(TimeUnit.SECONDS), "%ds", ", ", 0));
                ChatUtil.msg(sender, ChatColor.GRAY, "Second lengths: ", StringUtil.joinString(
                        snap.getActualSecondTimes(TimeUnit.MILLISECONDS), "%dms", ", ", 0));
                ChatUtil.msg(sender, ChatColor.GRAY, "Tick lengths: ", StringUtil.joinString(
                        snap.getTickTimes(TimeUnit.MILLISECONDS), "%dms", ", ", 0));
                ChatUtil.msg(sender, ChatColor.GRAY, "Rates: ", StringUtil.joinString(
                        snap.getTicksPerSecond(), "%g", ", ", 0));
            } else {
                ChatUtil.msg(sender, ChatColor.GRAY, StringUtil.joinString(
                        snap.getTicksPerSecond(), "%g", ", ", 0));
            }

        }
    }

    @Override
    public double[] getTicksPerSecond() {
        TimingSnapshot snapshot = getSnapshot();
        return snapshot.getTicksPerSecond();
    }

    @Override
    public long[] getTickTimes() {
        TimingSnapshot snapshot = getSnapshot();
        return snapshot.getTickTimes(TimeUnit.MICROSECONDS);
    }

    @Override
    public double getTickRate() {
        return getTicksPerSecond()[0];
    }

}
