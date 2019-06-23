/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages known attachment points.
 *
 * @see Attachment
 * @author sk89q
 */
public class AttachmentManager {

    private final Map<String, Attachment> attachments = new HashMap<String, Attachment>();

    /**
     * Register an {@link Attachment}.
     *
     * @param name name of the attachment (used in a rule's 'when' clause) (case insensitive)
     * @param attachment attachment
     */
    public void register(String name, Attachment attachment) {
        attachments.put(name.toLowerCase(), attachment);
    }

    /**
     * Register the fields in an object that are marked with {@link AttachmentTarget}.
     *
     * @param object object to search
     * @throws IllegalAccessException thrown on failure to register the attachment
     * @throws IllegalArgumentException thrown on failure to register the attachment
     */
    public void registerAnnotated(Object object) throws IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getFields()) {
            AttachmentTarget target = field.getAnnotation(AttachmentTarget.class);
            register(target.value(), (Attachment) field.get(object));
        }
    }

    /**
     * Get an attachment.
     *
     * @param name name of the attachment (case insensitive)
     * @return attachment
     */
    public Attachment get(String name) {
        return attachments.get(name.toLowerCase());
    }

    /**
     * Forget all the rules in the registered attachments.
     */
    public void forgetRules() {
        for (Attachment attachment : attachments.values()) {
            attachment.forgetRules();
        }
    }

}
