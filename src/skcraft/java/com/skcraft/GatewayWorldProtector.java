/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.BlockInteractEvent;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultString;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.event.RegisteredEvent;
import com.sk89q.rebar.util.ChatUtil;

public class GatewayWorldProtector extends AbstractComponent {

    private LocalConfiguration config;

    @Override
    public void initialize() {
        config = configure(new LocalConfiguration());

        Rebar.getInstance().registerEvents(new ProtectionListener());
        Rebar.getInstance().registerEvents(new BlockListener());
        Rebar.getInstance().registerEvents(new EntityListener());
        Rebar.getInstance().registerEvents(new PlayerListener());
    }

    @Override
    public void shutdown() {
    }

    @SettingBase("gateway")
    private static class LocalConfiguration extends ConfigurationBase {
        @Setting("world") @DefaultString("gateway") public String world;
    }

    private boolean isGateway(World world) {
        return world.getName().equalsIgnoreCase(config.world);
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            if (event.isCancelled()) return;
            Location to = event.getTo();
            if (!isGateway(to.getWorld())) return;
            Player player = event.getPlayer();

            if (player.getGameMode() != GameMode.CREATIVE && (to.getY() < 10 || player.getFallDistance() > 20)) {
                event.setTo(to.getWorld().getSpawnLocation());
                player.setFallDistance(0);
                ChatUtil.msg(player, ChatColor.YELLOW, "Whoops! Put you back at spawn.");
            }
        }
    }

    public class EntityListener implements Listener {
        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (event.isCancelled()) return;
            if (!isGateway(event.getLocation().getWorld())) return;

            if (!(event.getEntity() instanceof Animals)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onBlockCanBuild(BlockCanBuildEvent event) {
            if (event.isBuildable()) return;
            if (!isGateway(event.getBlock().getWorld())) return;
            if (event.getBlock().getType() == Material.PORTAL) {
                event.setBuildable(true);
                return;
            }
        }

        @EventHandler
        public void onBlockPhysics(BlockPhysicsEvent event) {
            if (event.isCancelled()) return;
            if (!isGateway(event.getBlock().getWorld())) return;
            if (event.getBlock().getType() == Material.PORTAL) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler
        public void onBlockBurn(BlockBurnEvent event) {
            if (event.isCancelled()) return;
            if (isGateway(event.getBlock().getWorld())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler
        public void onBlockIgnite(BlockIgniteEvent event) {
            if (event.isCancelled()) return;
            if (isGateway(event.getBlock().getWorld())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private class ProtectionListener extends com.sk89q.rebar.components.ProtectionListener {
        @Override
        @RegisteredEvent(type = BlockInteractEvent.class)
        public void onEvent(BlockInteractEvent event) {
            if (event.isPlayerCaused() && isGateway(event.getBlock().getWorld())) {
                if (!Rebar.getInstance().hasPermission((Player) event.getCauser(), "skcraft.gateway")) {
                    event.cancel();
                    return;
                }
            }
        }
    }

}
