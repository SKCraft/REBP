/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class CreativeFlyItem extends AbstractComponent {

    private CreativeFlyConfig config;

    @Override
    public void initialize() {
        config = configure(new CreativeFlyConfig());
        Rebar.getInstance().registerEvents(new Listener());
        Rebar.getInstance().registerInterval(new FlyerMonitor(), 20, 20);
    }

    @Override
    public void shutdown() {
    }

    private void enableFlight(Player player, ItemStack item) {
        updateFlyState(player, item);
    }

    private void disableFlight(Player player) {
        player.setFlying(false);
        player.setAllowFlight(false);
    }

    private void updateFlyState(Player player, ItemStack item) {
        if (item.getDurability() < config.maxData && player.getLocation().getY() > 55) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
        } else {
            if (player.getAllowFlight()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }
    }

    private class FlyerMonitor implements Runnable {
        @Override
        public void run() {
            for (Player player : Rebar.server().getOnlinePlayers()) {
                ItemStack chestPlate = player.getInventory().getChestplate();

                if (chestPlate != null && chestPlate.getTypeId() == config.itemId) {
                    updateFlyState(player, chestPlate);

                    if (player.isFlying()) {
                        short data = chestPlate.getDurability();
                        if (data < config.maxData) {
                            short newData = (short) Math.min(config.maxData, data + config.dischargeRate);
                            chestPlate.setDurability(newData);
                        }
                    }
                }
            }
        }
    }

    private class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            ItemStack chestPlate = player.getInventory().getChestplate();

            if (chestPlate != null && chestPlate.getTypeId() == config.itemId) {
                enableFlight(player, chestPlate);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            Inventory inven = event.getInventory();
            if (event.getSlot() == 38) {
                InventoryHolder ownerHuman = inven.getHolder();
                if (!(ownerHuman instanceof Player)) return;
                Player owner = (Player) ownerHuman;

                ItemStack current = event.getCurrentItem();
                ItemStack cursor = event.getCursor();

                // Removing item
                if (current != null && current.getTypeId() == config.itemId) {
                    disableFlight(owner);
                } else if (cursor != null && cursor.getTypeId() == config.itemId) {
                    enableFlight(owner, cursor);
                }
            }
        }
    }

    @SettingBase("creative-fly")
    private static class CreativeFlyConfig extends ConfigurationBase {
        @Setting("item-id") @DefaultInt(-1)
        private Integer itemId;
        @Setting("item-max-data") @DefaultInt(18000)
        private Integer maxData;
        @Setting("discharge-rate") @DefaultInt(5)
        private Integer dischargeRate;
    }

}
