/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.BlockUtil;
import com.skcraft.redispense.amplifiers.Cutter;
import com.skcraft.redispense.amplifiers.SnowBlast;
import com.skcraft.redispense.amplifiers.WindTunnel;
import com.skcraft.redispense.amplifiers.XPBottler;

public class ReDispense extends AbstractComponent {

    private AmplifierManager amplifiers = new AmplifierManager();

    @Override
    public void initialize() {
        amplifiers.register(new SnowBlast());
        //amplifiers.register(new HeatBlast());
        amplifiers.register(new WindTunnel());
        amplifiers.register(new XPBottler());
        amplifiers.register(new Cutter());

        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @Override
    public void shutdown() {
    }

    public boolean isReDispenser(Inventory inven) {
        ItemStack item;

        item = inven.getItem(1);
        if (item == null) return false;
        if (item.getType() != Material.REDSTONE || item.getAmount() != 1) return false;
        item = inven.getItem(3);
        if (item == null) return false;
        if (item.getType() != Material.REDSTONE || item.getAmount() != 1) return false;
        item = inven.getItem(5);
        if (item == null) return false;
        if (item.getType() != Material.REDSTONE || item.getAmount() != 1) return false;
        item = inven.getItem(7);
        if (item == null) return false;
        if (item.getType() != Material.REDSTONE || item.getAmount() != 1) return false;

        return true;
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onBlockDispense(BlockDispenseEvent event) {
            if (event.isCancelled()) return;

            Block block = event.getBlock();
            Dispenser dispenser = BlockUtil.getState(block, Dispenser.class);
            Inventory inven = dispenser.getInventory();
            if (!isReDispenser(inven)) return;

            event.setCancelled(true);

            Amplifier amp = amplifiers.find(inven);

            if (amp == null) {
                block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
            } else {
                if (!amp.activate(block, dispenser, inven, event.getVelocity())) {
                    block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, Material.GLOWSTONE.getId());
                }
            }

        }
    }

}
