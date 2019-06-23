/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class Listener implements org.bukkit.event.Listener {

    @AttachmentTarget("block-place")
    public RuleSet blockPlace = new RuleSet();
    @AttachmentTarget("block-break")
    public RuleSet blockBreak = new RuleSet();
    @AttachmentTarget("block-hit")
    public RuleSet blockHit = new RuleSet();

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!blockPlace.hasRules()) return;

        BukkitContext context = new BukkitContext(event);
        context.setSource(event.getPlayer());
        context.setBlock(event.getBlock().getState());

        if (blockPlace.apply(context)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!blockBreak.hasRules()) return;

        BukkitContext context = new BukkitContext(event);
        context.setSource(event.getPlayer());
        context.setBlock(event.getBlock().getState());

        if (blockBreak.apply(context)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        switch (event.getAction()) {
        case LEFT_CLICK_BLOCK:
            if (!blockHit.hasRules()) return;

            BukkitContext context = new BukkitContext(event);
            context.setSource(event.getPlayer());
            context.setBlock(event.getClickedBlock().getState());

            if (blockHit.apply(context)) {
                event.setCancelled(true);
            }

            break;
        default:
            break;
        }
    }

}
