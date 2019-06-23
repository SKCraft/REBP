/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.Context;
import com.skcraft.actionlists.SubjectResolver;

public class BlockPlacedSubjectResolver implements SubjectResolver<BlockState> {

    @Override
    public BlockState resolve(Context context) {
        return ((BukkitContext) context).getPlacedBlock();
    }

}
