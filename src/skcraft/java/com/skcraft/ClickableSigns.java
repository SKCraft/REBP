/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.google.common.base.Joiner;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Sign;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClickableSigns extends AbstractComponent implements Listener {
    private static final Pattern LINK_PATTERN = Pattern.compile(".*(https?://[^ ]+)");

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();
            if (block != null) {
                BlockState state = block.getState();
                if (state.getData() instanceof Sign) {
                    String text = Joiner.on("").join(((org.bukkit.block.Sign) state).getLines());
                    Matcher m = LINK_PATTERN.matcher(text);
                    if (m.matches()) {
                        event.getPlayer().sendMessage(m.group(1));
                    }
                }
            }
        }
    }
}
