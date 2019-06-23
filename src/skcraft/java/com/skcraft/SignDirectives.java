/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.BlockInteractEvent;
import com.sk89q.rebar.event.RegisteredEvent;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.ChatUtil;

public class SignDirectives extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new ProtectionListener());
        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @Override
    public void shutdown() {
    }

    private boolean hasPermission(Player player) {
        return Rebar.getInstance().hasPermission(player, "skcraft.sign-directive");
    }

    public class BlockListener implements Listener {
        @EventHandler(priority = EventPriority.LOW)
        public void onSignChange(SignChangeEvent event) {
            if (event.isCancelled()) return;
            if (!event.getLine(0).startsWith(":=")) return;

            Block block = event.getBlock();
            Player player = event.getPlayer();

            if (!hasPermission(player)) {
                BlockUtil.drop(block);
                ChatUtil.error(player, "You are not permitted to do that.");
                event.setCancelled(true);
                return;
            }

            event.setLine(0, event.getLine(0).toUpperCase());

            ChatUtil.msg(player,  ChatColor.YELLOW, "Sign directive created!");
        }
    }

    private class ProtectionListener extends com.sk89q.rebar.components.ProtectionListener {
        @Override
        @RegisteredEvent(type = BlockInteractEvent.class)
        public void onEvent(BlockInteractEvent event) {
            Block block = event.getBlock();
            if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                Sign sign = BlockUtil.getState(block, Sign.class);
                if (sign.getLine(0).startsWith(":=")) {
                    if (event.isPlayerCaused()) {
                        if (hasPermission((Player) event.getCauser())) {
                            return;
                        }

                        ChatUtil.error((Player) event.getCauser(), "You are not permitted to do that.");
                    }

                    event.cancel();
                    return;
                }
            }
        }
    }

}
