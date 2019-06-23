/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultBoolean;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.PlayerUtil;
import com.skcraft.adventure.worlds.MiningWorld;

public class AdventureGates extends AbstractComponent {

    private AdventureWorldManager worldManager = new AdventureWorldManager();
    private GateConfiguration config;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());
        Rebar.getInstance().registerEvents(new EntityListener());
        worldManager.register(new MiningWorld());

        config = configure(new GateConfiguration());

        // Force create
        worldManager.getActive();
    }

    @Override
    public void shutdown() {
    }

    private World getMainWorld() {
        return Rebar.getInstance().getServer().getWorld("world");
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerPortal(PlayerPortalEvent event) {
            World world = event.getPlayer().getWorld();
            Player player = event.getPlayer();
            Location loc = player.getLocation();
            if (worldManager.isAdventureWorld(world)) {
                if (config.usePortalAgent) {
                    event.useTravelAgent(true);
                    event.setTo(loc);
                } else {
                    event.useTravelAgent(false);
                    event.setTo(PlayerUtil.findFreePosition(new Location(
                            getMainWorld(), loc.getX(), loc.getY(), loc.getZ())));
                }

                ChatUtil.msg(player, ChatColor.GOLD,
                        "You've arrived back in the main world!");
            } else if (event.getTo().getWorld().getEnvironment() == Environment.NETHER) {
                ItemStack held = player.getItemInHand();
                if (held != null && held.getType() == Material.ENDER_PEARL) {
                    ChatUtil.msg(player, ChatColor.GOLD,
                            "The gods grant your request! Keep your pearl; you may need it to return.");

                    /*
                     * if (held.getAmount() > 1) {
                     * held.setAmount(held.getAmount() - 1);
                     * player.setItemInHand(held); } else {
                     * player.setItemInHand(null); }
                     */

                    World target = worldManager.getActive();
                    if (config.usePortalAgent) {
                        event.useTravelAgent(true);
                        event.setTo(loc);
                    } else {
                        event.useTravelAgent(false);
                        event.setTo(PlayerUtil.findFreePosition(new Location(
                                target, loc.getX(), loc.getY(), loc.getZ())));
                    }
                }
            }
        }
    }

    public class EntityListener implements Listener {
        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            World world = event.getEntity().getWorld();
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player) event.getEntity();
            if (worldManager.isAdventureWorld(world)) {
                ChatUtil.msg(player, ChatColor.GOLD, "The gods have returned you back to the mainland.");
                player.teleport(getMainWorld().getSpawnLocation());
            }
        }
    }

    @SettingBase("adventure-gates")
    private static class GateConfiguration extends ConfigurationBase {

        @Setting("use-portal-agent") @DefaultBoolean(true)
        public boolean usePortalAgent;

    }

}
