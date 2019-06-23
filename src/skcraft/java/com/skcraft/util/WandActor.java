/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public interface WandActor {

    String getName();
    
    String getHelp();
    
    boolean interact(Player player, Action action, Block block, PlayerInteractEvent event);
    
    public boolean hasPermissionStill(Player player);

    void destroy();

}
