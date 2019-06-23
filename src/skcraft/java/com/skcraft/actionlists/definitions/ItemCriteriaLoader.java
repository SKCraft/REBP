/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import static com.skcraft.actionlists.RuleEntryLoader.INLINE;

import java.util.List;

import com.skcraft.actionlists.ActionListsManager;
import com.skcraft.actionlists.DefinitionException;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.LoaderBuilderException;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;
import com.sk89q.rebar.util.MaterialPattern;

public class ItemCriteriaLoader extends AbstractNodeLoader<ItemCriteria> {

    private final ActionListsManager actionListsManager;
    private MaterialPatternLoaderBuilder materialLoader = new MaterialPatternLoaderBuilder();

    public ItemCriteriaLoader(ActionListsManager actionListsManager) {
        this.actionListsManager = actionListsManager;
    }

    @Override
    public ItemCriteria read(ConfigurationNode node) throws DefinitionException {
        SubjectResolver<ItemStackSlot> resolver = actionListsManager.getSubjectResolvers()
                .getResolver(ItemStackSlot.class, node.getString("of", "held"));

        List<MaterialPattern> patterns = node.contains(INLINE) ? node.listOf(INLINE,
                materialLoader) : node.listOf("material", materialLoader);

        boolean hasDataCheck = node.getBoolean("has-data", false);

        if (patterns.size() == 0 && !hasDataCheck) {
            throw new LoaderBuilderException("No block materials specified");
        }

        ItemCriteria criteria = new ItemCriteria(resolver);
        criteria.setPatterns(patterns);
        criteria.setDataCheck(hasDataCheck);

        return criteria;
    }

}
