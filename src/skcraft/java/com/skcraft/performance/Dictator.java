/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.sidechannel.SideChannel;
import com.skcraft.sidechannel.ThinkListener;
import com.skcraft.sidechannel.events.EntityBenchmarkEvent;
import com.skcraft.sidechannel.events.EntityThinkEvent;
import com.skcraft.sidechannel.events.TileEntityBenchmarkEvent;
import com.skcraft.sidechannel.events.TileEntityThinkEvent;
import groovy.lang.GroovyClassLoader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dictator extends AbstractComponent {

    private final Logger logger = createLogger(Dictator.class);
    private File handlerFile;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(DictatorCommands.class, this);

        handlerFile = new File(Rebar.getInstance().getDataFolder(), "handlers/CustomDictator.groovy");
        loadHandler();
    }

    @Override
    public void shutdown() {
    }

    /**
     * Reload the Groovy packet monitor script.
     */
    public void loadHandler() {
        if (handlerFile.exists()) {
            GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader());
            try {
                Class clazz = gcl.parseClass(handlerFile);
                Object obj = clazz.newInstance();
                ThinkListener handler = (ThinkListener) obj;
                SideChannel.getInstance().setThinkListener(handler);
                logger.info("Dictator: Loaded dictator script!");
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Failed to load dictator script", e);
            }
        } else {
            logger.info("Dictator: Removed dictator script");
            SideChannel.getInstance().setThinkListener(new ThinkListener() {
                @Override public void onEntityThink(EntityThinkEvent entityThinkEvent) {}
                @Override public void onEntityBenchmark(EntityBenchmarkEvent entityBenchmarkEvent) {}
                @Override public void onTileEntityThink(TileEntityThinkEvent tileEntityThinkEvent) {}
                @Override public void onTileEntityBenchmark(TileEntityBenchmarkEvent tileEntityBenchmarkEvent) {}
            });
        }
    }

    public static class DictatorCommands {
        private Dictator dict;

        public DictatorCommands(Dictator dict) {
            this.dict = dict;
        }

        @Command(aliases = {"reloaddict"}, min = 0, max = 0, desc = "Reload the dictator script")
        @CommandPermissions("skcraft.performance.reload-script")
        public void reloadScript(CommandContext context, CommandSender sender) throws CommandException {
            dict.loadHandler();
            ChatUtil.msg(sender, ChatColor.YELLOW + "Reloaded!");
        }
    }

}
