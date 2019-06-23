/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import static com.skcraft.actionlists.RuleEntryLoader.INLINE;

import java.util.List;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.ActionListsManager;
import com.skcraft.actionlists.DefinitionException;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;
import com.sk89q.rebar.util.MaterialPattern;

public class BlockCriteriaLoader extends AbstractNodeLoader<BlockCriteria> {

    private final ActionListsManager manager;
    private MaterialPatternLoaderBuilder materialLoader = new MaterialPatternLoaderBuilder();

    public BlockCriteriaLoader(ActionListsManager manager) {
        this.manager = manager;
    }

    @Override
    public BlockCriteria read(ConfigurationNode node)
            throws DefinitionException {
        SubjectResolver<BlockState> resolver = manager.getSubjectResolvers()
                .getResolver(BlockState.class, node.getString("of", "block"));

        List<MaterialPattern> patterns = node.contains(INLINE) ?
                node.listOf(INLINE, materialLoader) : node.listOf("material", materialLoader);

        if (patterns.size() == 0) {
            throw new LoaderBuilderException("No block materials specified");
        }

        BlockCriteria criteria = new BlockCriteria(resolver);
        criteria.setPatterns(patterns);

        return criteria;
    }

}
