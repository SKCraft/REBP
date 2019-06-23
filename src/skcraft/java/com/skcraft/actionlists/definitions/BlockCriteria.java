/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import java.util.List;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.Criteria;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.util.MaterialPattern;

public class BlockCriteria implements Criteria<BukkitContext> {

    private SubjectResolver<BlockState> resolver;
    private MaterialPattern[] patterns = new MaterialPattern[0];

    public BlockCriteria(SubjectResolver<BlockState> resolver) {
        this.resolver = resolver;
    }

    public SubjectResolver<BlockState> getResolver() {
        return resolver;
    }

    public void setResolver(SubjectResolver<BlockState> resolver) {
        this.resolver = resolver;
    }

    public MaterialPattern[] getPatterns() {
        return patterns;
    }

    public void setPatterns(MaterialPattern[] patterns) {
        this.patterns = patterns;
    }

    public void setPatterns(List<MaterialPattern> patterns) {
        MaterialPattern[] arr = new MaterialPattern[patterns.size()];
        patterns.toArray(arr);
        this.patterns = arr;
    }

    @Override
    public boolean matches(BukkitContext context) {
        BlockState block = resolver.resolve(context);

        if (block == null) {
            return false;
        }

        for (MaterialPattern pattern : patterns) {
            if (pattern.matches(block.getTypeId(), block.getRawData())) {
                return true;
            }
        }

        return false;
    }

}
