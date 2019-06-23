/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.protection;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.util.Materials;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nullable;

public class SignProtection extends AbstractComponent implements Listener {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    public ProtectionQuery query(Block block) {
        return new ProtectionQuery(block);
    }

    public boolean canModify(Block block) {
        ProtectionQuery query = new ProtectionQuery(block);
        return !query.isProtected();
    }

    public boolean canModify(Block block, Player player) {
        if (canOverrideAccess(player)) return true;
        ProtectionQuery query = new ProtectionQuery(block);
        if (!query.isProtected()) return true;
        return query.isListedOwner(player.getName());
    }

    private boolean canOverrideAccess(Player player) {
        return Rebar.getInstance().hasPermission(player, "skcraft.sign-protect.access-all");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (event.getNewCurrent() == event.getOldCurrent()) return;

        Block block = event.getBlock();

        ProtectionQuery query = new ProtectionQuery(event.getBlock());
        if (query.isProtected()) {
            event.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) return;

        if (event.getLine(0).equals("   ")) {
            event.setLine(0, "[Lock]");
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();
        Sign sign = BlockUtil.getState(block, Sign.class);
        Block protectedBlock = null;

        if (!event.getLine(0).equalsIgnoreCase("[Lock]")) {
            return;
        }

        if (material == Material.SIGN_POST) {
            protectedBlock = block.getRelative(0, 1, 0);

            if (ProtectionQuery.isUnsafe(block.getRelative(0, -1, 0).getType())) {
                BlockUtil.drop(block);
                ChatUtil.error(player, "You placed the sign on a block type that is unstable!");
                event.setCancelled(true);
                return;
            }
        } else {
            BlockFace facing = ((org.bukkit.material.Sign) sign.getData()).getFacing();
            protectedBlock = block.getRelative(facing.getOppositeFace());
        }

        ProtectionQuery query = new ProtectionQuery(protectedBlock);

        // Now we need to check if this is already protected
        if (query.isProtected()) {
            BlockUtil.drop(block);
            ChatUtil.error(player, "This block is already protected.");
            event.setCancelled(true);
            return;
        }

        // Check to see if the player's name is on the sign
        if (!event.getLine(1).trim().equalsIgnoreCase(player.getName())) {
            if (event.getLine(3).trim().length() == 0) {
                // Shift the sign down and add the name
                event.setLine(3, event.getLine(2));
                event.setLine(2, event.getLine(1));
                String name = player.getName();
                if (name.length() > 15) {
                    name = name.substring(0, 15);
                }
                event.setLine(1, name);
            } else {
                ChatUtil.error(player, "The second line should be your own name.");
                BlockUtil.drop(block);
                event.setCancelled(true);
            }
        }

        event.setLine(0, "[Lock]");

        ChatUtil.msg(player, ChatColor.YELLOW, "Lock created. Crouch + left click protectable blocks to see if they are protected.");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;
        Block block = event.getClickedBlock();

        ProtectionQuery query = new ProtectionQuery(block);
        if (query.isProtected()) {
            ChatUtil.msg(player, ChatColor.GRAY, "Locked to: " + query.getAccessibleString());
        }
    }

    @EventHandler
    public void onPlaceBlock(PlaceBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        event.filter(target -> mayModify(player, target), true);
    }

    @EventHandler
    public void onBreakBlock(BreakBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        event.filter(target -> mayModify(player, target), true);
    }

    @EventHandler
    public void onUseBlock(UseBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        event.filter(target -> mayModify(player, target), true);
    }

    public boolean mayModify(@Nullable Player player, Location target) {
        Material type = target.getBlock().getType();
        if (Materials.isRailBlock(type) || type == Material.HOPPER) {
            target = target.add(0, 1, 0);
        }
        ProtectionQuery query = new ProtectionQuery(target.getBlock());
        if (query.isProtected()) {
            if (player != null) {
                if (canOverrideAccess(player)) {
                    return true;
                } else if (query.isListedOwner(player.getName())) {
                    return true;
                } else {
                    ChatUtil.msg(player, ChatColor.DARK_RED, "This object has been locked.");
                    ChatUtil.msg(player, ChatColor.GRAY, "Locked to: " + query.getAccessibleString());
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

}
