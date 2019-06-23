/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.Collection;

public class IdleTracker extends AbstractComponent implements Listener {

    private IdlePlayerManager manager = new IdlePlayerManager();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
        Rebar.getInstance().registerCommands(IdleCommands.class, this);
        Rebar.getInstance().registerInterval(manager, 0, 40);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (event.getFoodLevel() < player.getFoodLevel() && manager.isIdle(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(EntityTargetEvent event) {
        Entity target = event.getTarget();

        if (target instanceof Player) {
            Player player = (Player) target;
            if (manager.isIdle(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        manager.update(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.forget(event.getPlayer());
    }

    public static class IdleCommands {
        public IdleCommands(IdleTracker component) {
        }

        @Command(aliases = { "bed" }, desc = "See who is neither in bed nor idle", min = 0, max = 0)
        @CommandPermissions({ "skcraft.idle" })
        public void bed(CommandContext context, CommandSender sender) throws CommandException {
            Collection<? extends Player> online = BukkitUtil.getOnlinePlayers();
            World world = CommandUtil.checkPlayer(sender).getWorld();

            StringBuilder out = new StringBuilder();
            boolean first = true;

            out.append(ChatColor.GRAY + "Not in bed (and not idle): ");
            out.append(ChatColor.WHITE);

            // Now go through the list of players and find any matching players
            for (Player pl : online) {
                if (!pl.getWorld().equals(world)) {
                    continue;
                }

                if (pl.isSleeping() || pl.isSleepingIgnored()) {
                    continue;
                }

                if (!first) {
                    out.append(", ");
                }

                out.append(pl.getName());

                first = false;
            }

            if (first) {
                out.append("(no one)");
            }

            sender.sendMessage(ChatColor.YELLOW + out.toString());
        }

    }

}
