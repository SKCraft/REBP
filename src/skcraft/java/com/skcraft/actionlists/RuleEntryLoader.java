/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.ConfigurationValue;
import com.sk89q.rebar.config.LoaderException;

public class RuleEntryLoader extends AbstractNodeLoader<RuleEntry> {

    public static final String INLINE = "_";
    private static Logger logger = Logger.getLogger(RuleEntryLoader.class.getCanonicalName());

    private final DefinitionLoader<Criteria<?>> criteriaLoader;
    private final DefinitionLoader<Action<?>> actionLoader;

    public RuleEntryLoader(ActionListsManager actionListsManager) {
        criteriaLoader = new DefinitionLoader<Criteria<?>>(actionListsManager.getCriterion(), true);
        actionLoader = new DefinitionLoader<Action<?>>(actionListsManager.getActions(), false);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public RuleEntry read(ConfigurationNode node) {
        List<String> when = node.getStringList("when", new ArrayList<String>());
        if (when.size() == 0) {
            logger.warning("EventListeners: A rule has missing 'when' clause");
            return null;
        }

        try {
            DefinedRule rule = new DefinedRule();

            for (Criteria criteria : node.listOf("if", criteriaLoader)) {
                rule.add(criteria);
            }

            for (Action action : node.listOf("then", actionLoader)) {
                rule.add(action);
            }

            if (rule.hasActions()) {
                return new RuleEntry(when, rule);
            } else {
                logger.warning("EventListeners: A rule has no actions");
                return null;
            }
        } catch (LoaderException e) {
            logger.warning("EventListeners: A rule had a severe misconfiguration and was skipped");
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <V> V invertCriteria(V object, boolean inverted) {
        if (object instanceof Criteria && inverted) {
            return (V) new InvertedCriteria((Criteria<?>) object);
        }
        return object;
    }

    private class DefinitionLoader<T> extends AbstractNodeLoader<T> {

        private final DefinitionManager<T> manager;
        private final boolean strict;

        public DefinitionLoader(DefinitionManager<T> manager, boolean strict) {
            this.manager = manager;
            this.strict = strict;
        }

        @Override
        public T read(ConfigurationNode node) {
            String id = node.getString("type");
            boolean inverted = node.getBoolean("negate", false);

            try {
                if (id != null) {
                    return invertCriteria(manager.newInstance(id, node, null), inverted);
                }

                for (String key : node.getKeys(ConfigurationNode.ROOT)) {
                    if (key.startsWith("?")) {
                        id = key;

                        Object value = node.get(key);
                        return invertCriteria(
                                manager.newInstance(key.substring(1), node, new ConfigurationValue(value)), inverted);
                    }
                }

                logger.warning("EventListeners: Missing 'type' in definition");

                return null;
            } catch (DefinitionException e) {
                logger.warning("EventListeners: Invalid definition " +
                        "identified by type '" + id + "': " + e.getMessage());

                if (strict) {
                    throw new LoaderException();
                }

                return null;
            }
        }

    }

}
