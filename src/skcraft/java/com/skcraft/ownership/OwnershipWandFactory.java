/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.skcraft.util.WandActor;
import com.skcraft.util.WandFactory;
import org.bukkit.entity.Player;

public class OwnershipWandFactory implements WandFactory {
    
    private final ChunkOwnership ownership;
    
    public OwnershipWandFactory(ChunkOwnership ownership) {
        this.ownership = ownership;
    }

    @Override
    public WandActor create(Player player, CommandContext context) throws CommandException {
        return new OwnershipWand(ownership);
    }

    @Override
    public String getName() {
        return "Ownership Lookup";
    }

    @Override
    public boolean hasPermission(Player player) {
        return true;
    }

}
