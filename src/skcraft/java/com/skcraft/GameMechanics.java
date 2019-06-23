/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.BlockLoggingInterp;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.InventoryUtil;
import com.skcraft.util.GlowstoneTreeDelegate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class GameMechanics extends AbstractComponent implements Listener {
    private static Random random = new Random();
    @InjectComponent
    private BlockLoggingInterp interp;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;

        Entity ent = event.getEntity();

        // Fall on wool
        if (ent instanceof Player && event.getCause() == DamageCause.FALL) {
            Player player = (Player)ent;
            if (player.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.WOOL) {
                event.setCancelled(true);
                return;
            }
        }

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent pveEvent = (EntityDamageByEntityEvent) event;
            Entity damager = pveEvent.getDamager();

            // No damage in cart
            if ((ent instanceof Player && ((Player) ent).getVehicle() != null)
                    || (ent instanceof Vehicle && !(ent instanceof LivingEntity))) {
                event.setCancelled(true);
                return;

            // Gold sword
            } else if (damager instanceof Player) {
                Player damagerPl = (Player) damager;
                ItemStack held = damagerPl.getItemInHand();

                if (held.getType() == Material.GOLD_SWORD
                        && !(event.getEntity() instanceof Pig)
                        && !(event.getEntity() instanceof Sheep)
                        && !(event.getEntity() instanceof Chicken)
                        && !(event.getEntity() instanceof Cow)
                        && !(event.getEntity() instanceof Player)) {
                    event.getEntity().setFireTicks(20 * 10);
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Location from = event.getFrom().clone();
        Location to = event.getTo().clone();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            Block fromBlock = from.subtract(0, 1, 0).getBlock();
            Material fromType = fromBlock.getType();
            Material toType = to.subtract(0, 1, 0).getBlock().getType();

            if (toType != fromType) {
            } else if (fromType == Material.GOLD_BLOCK) {
                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1), true);
                }
                player.setSprinting(true);
            } else if (fromType == Material.DIAMOND_BLOCK) {
                if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 5), true);
                }
                player.setSprinting(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        final Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack held = player.getItemInHand();

        if ((block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) &&
                held != null && held.getType() == Material.SIGN) {
            PlayerInventory inven = player.getInventory();
            final int slot = inven.getHeldItemSlot();
            Rebar.getInstance().registerTimeout(new Runnable() {
                @Override
                public void run() {
                    ItemStack sign = new ItemStack(Material.SIGN, 1);
                    player.getInventory().setItem(slot, sign);
                }
            }, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Gold pickaxe -> stone
        if (block.getType() == Material.STONE
                && player.getItemInHand().getType() == Material.GOLD_PICKAXE) {
            for (int i = 0; i < 1; i++) {
                player.getWorld().dropItemNaturally(block.getLocation(),
                        new ItemStack(Material.STONE, 1));
            }
            block.setTypeId(0);
            event.setCancelled(true);
            ItemStack held = player.getItemInHand();
            short newDmg = (short) (held.getDurability() + 1);
            if (newDmg >= 33) {
                player.getInventory().removeItem(held);
            } else {
                held.setDurability(newDmg);
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();
        if (item != null && item.getType() == Material.SIGN) {
            if (player.getInventory().contains(Material.SIGN)) {
                event.getItem().remove();
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        Material type = block.getType();
        ItemStack held = player.getItemInHand();

        // Super-cake
        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)
                && type == Material.CAKE_BLOCK) {
            int currentFood = player.getFoodLevel();
            if (currentFood < 20) {
                player.setFoodLevel(19);
                player.setSaturation(6);
                return;
            }
        // Dirt -> grass
        } else if (action == Action.RIGHT_CLICK_BLOCK && type == Material.DIRT
                && (held != null && held.getType() == Material.INK_SACK && held.getDurability() == 15)) {
            InventoryUtil.reduceHeldItemSlot(player);
            block.setType(Material.GRASS);
            event.setCancelled(true);
            return;
        // Glowstone trees
        } else if (action == Action.RIGHT_CLICK_BLOCK && type == Material.NETHER_WARTS
                && (held != null && held.getType() == Material.DIAMOND)) {
            block.setType(Material.AIR);
            if (block.getWorld().getEnvironment() == Environment.NETHER) {
                block.getRelative(0, -1, 0).setType(Material.DIRT);
                if (block.getWorld().generateTree(block.getLocation(), TreeType.BIG_TREE,
                        new GlowstoneTreeDelegate(block.getWorld()))) {
                    InventoryUtil.reduceHeldItemSlot(player);
                } else {
                    block.setType(Material.NETHER_WARTS);
                }
                block.getRelative(0, -1, 0).setType(Material.NETHERRACK);
            }
            event.setCancelled(true);
            return;
        }
    }

}
