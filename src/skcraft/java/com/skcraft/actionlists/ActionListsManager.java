/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;


/**
 * Manages action lists and details relevant to them.
 *
 * @author sk89q
 */
public class ActionListsManager {

    private final AttachmentManager attachments = new AttachmentManager();
    private final DefinitionManager<Criteria<?>> criterion = new DefinitionManager<Criteria<?>>();
    private final DefinitionManager<Action<?>> actions = new DefinitionManager<Action<?>>();
    private final SubjectResolverManager subjectResolvers = new SubjectResolverManager();
    private final ExpressionParser exprParser = new ExpressionParser();

    /**
     * Get the attachment manager.
     *
     * @return the attachment manager
     */
    public AttachmentManager getAttachments() {
        return attachments;
    }

    /**
     * Get the criterion manager.
     *
     * @return criteron manager
     */
    public DefinitionManager<Criteria<?>> getCriterion() {
        return criterion;
    }

    /**
     * Get the actions manager.
     *
     * @return actions manager
     */
    public DefinitionManager<Action<?>> getActions() {
        return actions;
    }

    /**
     * Get the subject resolvers manager.
     *
     * @return the subject resolvers manager
     */
    public SubjectResolverManager getSubjectResolvers() {
        return subjectResolvers;
    }

    /**
     * Get the expression parser.
     *
     * @return the expression parser
     */
    public ExpressionParser getParser() {
        return exprParser;
    }

}
