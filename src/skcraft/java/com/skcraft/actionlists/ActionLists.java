/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.BlockState;

import com.skcraft.actionlists.definitions.BlockCriteriaLoader;
import com.skcraft.actionlists.definitions.BlockDefaultSubjectResolver;
import com.skcraft.actionlists.definitions.BlockPlacedSubjectResolver;
import com.skcraft.actionlists.definitions.DenyActionLoader;
import com.skcraft.actionlists.definitions.ItemCriteriaLoader;
import com.skcraft.actionlists.definitions.ItemHeldSubjectResolver;
import com.skcraft.actionlists.definitions.ItemStackSlot;
import com.skcraft.actionlists.definitions.TellActionLoader;
import com.skcraft.actionlists.definitions.UpdateItemActionFactory;
import com.skcraft.actionlists.definitions.WorldCriteriaLoader;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.Configuration;

public class ActionLists extends AbstractComponent {

    private final Logger logger = createLogger(ActionLists.class);

    private ActionListsManager manager;

    @Override
    public void initialize() {
        Listener listener = new Listener();
        Rebar.getInstance().registerEvents(listener);

        manager = new ActionListsManager();
        registerDefinitions(listener);

        Rebar.server().getPluginManager().callEvent(new DefinitionInitializeEvent(manager));

        try {
            reloadData();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load action lists", e);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void reloadConfiguration() {
        super.reloadConfiguration();

        try {
            reloadData();
        } catch (IOException e) {
            logger.log(Level.WARNING, "ActionLists: Failed to load action lists", e);
        }
    }

    public void reloadData() throws IOException {
        File file = new File(Rebar.getInstance().getDataFolder(), "action_lists.yml");

        if (!file.exists()) {
            return;
        }

        Configuration config = new Configuration(file);
        config.load();

        manager.getAttachments().forgetRules();

        int numRules = 0;
        for (RuleEntry entry : config.listOf("rules", new RuleEntryLoader(manager))) {
            Rule<?> rule = entry.getRule();

            for (String name : entry.getAttachmentNames()) {
                Attachment attachment = manager.getAttachments().get(name);
                if (attachment == null) {
                    logger.warning("Unknown 'when' event to handle, named '" + name + "'");
                } else {
                    attachment.learn(rule);
                }
            }

            numRules++;
        }

        logger.info("ActionLists: " + numRules + " rule(s) processed.");
    }

    private void registerDefinitions(Listener listener) {
        AttachmentManager attachments = manager.getAttachments();
        try {
            attachments.registerAnnotated(listener);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Failed to register all attachments", e);
        }

        SubjectResolverManager subjectResolvers = manager.getSubjectResolvers();
        subjectResolvers.register(BlockState.class, "block", new BlockDefaultSubjectResolver());
        subjectResolvers.register(BlockState.class, "placed", new BlockPlacedSubjectResolver());
        subjectResolvers.register(ItemStackSlot.class, "held", new ItemHeldSubjectResolver());

        DefinitionManager<Criteria<?>> criterion = manager.getCriterion();
        criterion.register("world", new WorldCriteriaLoader(manager));
        criterion.register("block", new BlockCriteriaLoader(manager));
        criterion.register("item", new ItemCriteriaLoader(manager));

        DefinitionManager<Action<?>> actions = manager.getActions();
        actions.register("deny", new DenyActionLoader());
        actions.register("tell", new TellActionLoader(manager));
        actions.register("update-item", new UpdateItemActionFactory(manager));
    }

}
