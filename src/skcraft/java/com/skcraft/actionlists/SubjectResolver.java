/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * Attempts to resolve the requested object from the given context. For
 * example, a block place event can have multiple 'relevant' blocks, such as
 * the clicked block or the clicked block. A resolver is used to figure out
 * which block is requested, and then the resolver would return the object
 * requested by accessing it through the given context.
 * 
 * @author sk89q
 * @param <T> requested object
 */
public interface SubjectResolver<T> {
    
    /**
     * Resolve the object from the context.
     * 
     * @param context
     * @return the object, or possibly null
     */
    T resolve(Context context);

}
