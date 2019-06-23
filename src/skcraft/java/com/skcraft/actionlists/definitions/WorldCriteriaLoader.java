/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import static com.skcraft.actionlists.RuleEntryLoader.INLINE;

import java.util.Set;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.ActionListsManager;
import com.skcraft.actionlists.DefinitionException;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.StringLoaderBuilder;

public class WorldCriteriaLoader extends AbstractNodeLoader<WorldCriteria> {

    private final ActionListsManager manager;
    private static final StringLoaderBuilder strLB = new StringLoaderBuilder();

    public WorldCriteriaLoader(ActionListsManager manager) {
        this.manager = manager;
    }

    @Override
    public WorldCriteria read(ConfigurationNode node)
            throws DefinitionException {
        SubjectResolver<BlockState> resolver = manager.getSubjectResolvers()
                .getResolver(BlockState.class, node.getString("of", "block"));

        Set<String> names = node.contains(INLINE) ?
                node.setOf(INLINE, strLB) : node.setOf("world", strLB);

        if (names.size() == 0) {
            throw new LoaderBuilderException("No world names specified");
        }

        WorldCriteria criteria = new WorldCriteria(resolver);
        criteria.setNames(names);

        return criteria;
    }

}
