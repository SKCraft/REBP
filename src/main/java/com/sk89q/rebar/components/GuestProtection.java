/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar.components;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class GuestProtection extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar rebar = Rebar.getInstance();
        rebar.registerEvents(new EventListener());
    }

    @Override
    public void shutdown() {
    }

    public boolean canModify(Player player) {
        return !Rebar.getInstance().getEventManager().dispatch(
                new GuestProtectedEvent(player)).isCancelled();
    }

    public boolean notifyCanModify(Player player) {
        GuestProtectedEvent event = new GuestProtectedEvent(player);
        boolean val = !Rebar.getInstance().getEventManager().dispatch(event).isCancelled();
        if (!val) {
            player.sendMessage(event.getMessage());
        }
        return val;

    }

    public void sendNotice(Player player, GuestProtectedEvent event) {
        player.sendMessage(event.getMessage());
    }

    private class EventListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityCombust(EntityCombustEvent event) {
            if (event.isCancelled()) return;
            if (event.getEntity() instanceof Player) {
                if (!canModify((Player) event.getEntity())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled()) return;
            if (event.getEntity() != null && event.getEntity() instanceof Player) {
                if (!canModify((Player) event.getEntity())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityTarget(EntityTargetEvent event) {
            if (event.isCancelled()) return;
            if (event.getTarget() instanceof Player) {
                if (!canModify((Player) event.getTarget())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onHangingPlace(HangingPlaceEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPaintingBreak(HangingBreakEvent event) {
            if (event.isCancelled()) return;
            if (!(event instanceof HangingBreakByEntityEvent)) return;
            Entity entity = ((HangingBreakByEntityEvent) event).getRemover();
            if (entity instanceof Player) {
                if (!notifyCanModify((Player) entity)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityTame(EntityTameEvent event) {
            if (event.isCancelled()) return;
            if (event.getOwner() instanceof Player) {
                if (!notifyCanModify((Player) event.getOwner())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockBreak(BlockBreakEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockDamage(BlockDamageEvent event) {
            if (event.isCancelled()) return;
            if (!canModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onSignChange(SignChangeEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockIgnite(BlockIgniteEvent event) {
            if (event.isCancelled()) return;
            if (event.getPlayer() != null && !notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onBlockPlace(BlockPlaceEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.isCancelled()) return;

            Block block = event.getClickedBlock();
            if (block != null
                    && (block.getType() == Material.WOODEN_DOOR
                    || block.getType() == Material.STONE_BUTTON
                    || block.getType() == Material.LEVER
                    || block.getType() == Material.SIGN_POST
                    || block.getType() == Material.WALL_SIGN)) {
                return;
            }

            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        /*@EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerDropItem(PlayerDropItemEvent event) {
            if (event.isCancelled()) return;
            if (!canModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerPickupItem(PlayerPickupItemEvent event) {
            if (event.isCancelled()) return;
            if (!canModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }*/

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerBucketFill(PlayerBucketFillEvent event) {
            if (event.isCancelled()) return;
            if (!notifyCanModify(event.getPlayer())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
