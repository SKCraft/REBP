/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultBoolean;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.management.AutomaticLagFixerMXBean;
import com.sk89q.rebar.management.ManagementUtils;
import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.Shutdown;

public class AutomatedLagFixer extends AbstractComponent implements AutomaticLagFixerMXBean {

    private final Logger logger = createLogger(AutomatedLagFixer.class);
    private enum EscalationLevel { NONE, SLOW, VERY_SLOW, EMERGENCY };

    @InjectComponent
    private ClockMonitor clockMonitor;
    @InjectComponent
    private ChunkGC chunkGC;
    @InjectComponent
    private EntityReducer entReducer;
    @InjectComponent
    private Shutdown shutdown;

    private long startTime = System.currentTimeMillis();
    private LagFixerConfiguration config;
    private Timer timer;
    private EscalationLevel level = EscalationLevel.NONE;
    private int severityMinutes = 0;
    private long lastShutdown = System.currentTimeMillis();

    @Override
	public void initialize() {
        config = configure(new LagFixerConfiguration());

        Rebar.getInstance().registerCommands(StatusCommands.class, this);

        timer = new Timer("Rebar AutomatedLagFixer", true);
        timer.schedule(new LagFixerMonitor(), 1000 * 60, 1000 * 60);

        Rebar.getInstance().registerTimeout(new Runnable() {
            @Override
			public void run() {
                if (config.chunkGCOnStart) {
                    chunkGC.gcChunks(true);
                }
            }
        }, 20 * 1);

        ManagementUtils.register(this, "com.sk89q.skcraft.performance:type=AutomatedLagFixer");
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void reloadConfiguration() {
        super.reloadConfiguration();
    }

    /**
     * Remove monsters from a world.
     *
     * @param world world
     */
    private void removeMonsters(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Monster) {
                entity.remove();
            }
        }
    }

    /**
     * Set the lag escalation level.
     *
     * @param level level
     */
    public void setLevel(EscalationLevel level) {
        if (this.level == level) {
            return; // Do nothing unless the level changes
        }

        //logger.info("AutomatedLagFixer: Setting level: " + level.name());

        this.level = level;

        for (World world : Rebar.server().getWorlds()) {
            if (world.getEnvironment() == Environment.THE_END) {
                continue;
            }

            boolean removeMonsters = false;

            switch (level) {
            case NONE:
                world.setSpawnFlags(true, true);
                world.setMonsterSpawnLimit(-1);
                world.setAnimalSpawnLimit(-1);
                world.setAmbientSpawnLimit(-1);
                world.setWaterAnimalSpawnLimit(-1);
                world.setTicksPerAnimalSpawns(-1);
                world.setTicksPerMonsterSpawns(-1);
                break;
            case SLOW:
                world.setSpawnFlags(true, true);
                world.setMonsterSpawnLimit(40);
                world.setAnimalSpawnLimit(5);
                world.setAmbientSpawnLimit(5);
                world.setWaterAnimalSpawnLimit(5);
                world.setTicksPerAnimalSpawns(-1);
                world.setTicksPerMonsterSpawns(-1);
                break;
            case VERY_SLOW:
                world.setSpawnFlags(true, true);
                world.setMonsterSpawnLimit(5);
                world.setAnimalSpawnLimit(5);
                world.setAmbientSpawnLimit(0);
                world.setWaterAnimalSpawnLimit(5);
                world.setTicksPerAnimalSpawns(-1);
                world.setTicksPerMonsterSpawns(-1);
                break;
            case EMERGENCY:
                world.setSpawnFlags(true, true);
                world.setMonsterSpawnLimit(5);
                world.setAnimalSpawnLimit(5);
                world.setAmbientSpawnLimit(0);
                world.setWaterAnimalSpawnLimit(5);
                world.setTicksPerAnimalSpawns(-1);
                world.setTicksPerMonsterSpawns(-1);
                removeMonsters = true;
                break;
            }

            if (removeMonsters) {
                removeMonsters(world);
            }
        }
    }

    /**
     * Complete the periodical activities of the given level.
     *
     * @param level level
     */
    public void processLevel(EscalationLevel level) {
        logger.info("AutomatedLagFixer: Setting level: " + level.name());

        for (World world : Rebar.server().getWorlds()) {
            if (world.getEnvironment() == Environment.THE_END) {
                continue;
            }
            switch (level) {
            case NONE:
                break;
            case SLOW:
                entReducer.removeEntities(false);
                break;
            case VERY_SLOW:
                entReducer.removeEntities(false);
                chunkGC.gcChunks(true);
                break;
            case EMERGENCY:
                entReducer.removeEntities(false);
                chunkGC.gcChunks(true);
                break;
            }
        }
    }

    /**
     * Start an emergency shutdown.
     */
    private void emergencyShutdown() {
        if (System.currentTimeMillis() - lastShutdown < 1000 * 60 * 30) {
            return;
        }

        Rebar.server().broadcastMessage(Shutdown.makeVisible("Hey! The server is now automatically going to be restarted."));

        lastShutdown = System.currentTimeMillis();
        shutdown.initiateShutdown(60, false);
    }

    private class LagFixerMonitor extends TimerTask {
        @Override
        public void run() {
            // Are we active?
            if (!config.active) return;

            // Get the tick rate
            TimingSnapshot snapshot = clockMonitor.getSnapshot();
            final double tickRate = snapshot.getTicksPerSecond()[2];

            Rebar.getInstance().registerTimeout(new Runnable() {
                @Override
                public void run() {
                    long now = System.currentTimeMillis();

                    if ((now - startTime) > config.hoursUntilAutoRestart * 1000 * 60 * 60) {
                        startTime += 1000 * 60 * 60 * 2;

                        emergencyShutdown();
                        return;
                    }

                    if (tickRate >= 17) {
                        setLevel(EscalationLevel.NONE);
                        severityMinutes = 0;
                    } else if (tickRate >= 15) {
                        setLevel(EscalationLevel.SLOW);
                        severityMinutes--;
                        if (severityMinutes < 0) severityMinutes = 0;
                    } else if (severityMinutes < 10) {
                        setLevel(EscalationLevel.VERY_SLOW);
                        severityMinutes++;
                    } else {
                        setLevel(EscalationLevel.EMERGENCY);
                        severityMinutes++;

                        if (severityMinutes >= 60 * config.severityHoursUntilRestart) {
                            emergencyShutdown();
                            return;
                        }
                    }

                    processLevel(level);
                }
            }, 0);
        }
    }

    public static class StatusCommands {
        private AutomatedLagFixer comp;

        public StatusCommands(AutomatedLagFixer component) {
            this.comp = component;
        }

        @Command(aliases = {"alfstatus"}, min = 0, max = 0, desc = "Status of the AutomatedLagFixer")
        @CommandPermissions("skcraft.perf.alf-status")
        public void algStatus(CommandContext c, CommandSender s) {
            ChatUtil.msg(s, ChatColor.GRAY + "Active?: " + (comp.config.active ? "ENABLED" : "disabled"));
            ChatUtil.msg(s, ChatColor.GRAY + "Severity threshold: " + comp.config.severityHoursUntilRestart + " hours");
            ChatUtil.msg(s, ChatColor.GRAY + "Escalation level: " + comp.level.name());
            ChatUtil.msg(s, ChatColor.GRAY + "Severity minutes: " + comp.severityMinutes);
            for (World world : Rebar.server().getWorlds()) {
                ChatUtil.msg(s, ChatColor.BLUE + world.getName() +
                        ": " + world.getLoadedChunks().length + " chunks, " +
                        world.getEntities().size() + " entities");
            }
        }

        @Command(aliases = {"alfbutcher"}, min = 0, max = 0, desc = "Kill all monsters")
        @CommandPermissions("skcraft.perf.alg-butcher")
        public void algButcher(CommandContext c, CommandSender s) {
            for (World world : Rebar.server().getWorlds()) {
                comp.removeMonsters(world);
            }
            ChatUtil.msg(s, "Monsters removed.");
        }
    }

    @SettingBase("automated-lag-fixer")
    public static class LagFixerConfiguration extends ConfigurationBase {

        @Setting("active") @DefaultBoolean(false)
        public Boolean active;

        @Setting("chunk-gc-on-start") @DefaultBoolean(false)
        public Boolean chunkGCOnStart;

        @Setting("restart-severity-hours") @DefaultInt(3)
        public Integer severityHoursUntilRestart;

        @Setting("auto-restart-hours") @DefaultInt(12)
        public Integer hoursUntilAutoRestart;

    }

    @Override
    public String getLevel() {
        return level.name();
    }

    @Override
    public int getSeverityCount() {
        return severityMinutes;
    }

    @Override
    public int getSeverityLimit() {
        return config.severityHoursUntilRestart;
    }

}
