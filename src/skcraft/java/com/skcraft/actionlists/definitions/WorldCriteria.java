/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.Criteria;
import com.skcraft.actionlists.SubjectResolver;

public class WorldCriteria implements Criteria<BukkitContext> {

    private SubjectResolver<BlockState> resolver;
    private Set<String> names = new HashSet<String>();

    public WorldCriteria(SubjectResolver<BlockState> resolver) {
        this.resolver = resolver;
    }

    public SubjectResolver<BlockState> getResolver() {
        return resolver;
    }

    public void setResolver(SubjectResolver<BlockState> resolver) {
        this.resolver = resolver;
    }

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
    }

    @Override
    public boolean matches(BukkitContext context) {
        BlockState block = resolver.resolve(context);

        if (block == null) {
            return false;
        }

        return names.contains(block.getWorld().getName());
    }

}
