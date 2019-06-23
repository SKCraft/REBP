/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.HashMap;
import java.util.Map;

public class SubjectResolverManager {
    
    private final Map<Class<?>, Map<String, SubjectResolver<?>>> resolvers
            = new HashMap<Class<?>, Map<String, SubjectResolver<?>>>();
    private final Map<SubjectResolver<?>, String> reverse = new HashMap<SubjectResolver<?>, String>();
    
    public void register(Class<?> clazz, String id, SubjectResolver<?> resolver) {
        Map<String, SubjectResolver<?>> map = resolvers.get(clazz);
        if (map == null) {
            map = new HashMap<String, SubjectResolver<?>>();
            resolvers.put(clazz, map);
        }
        
        reverse.put(resolver, id);
        
        map.put(id.toLowerCase(), resolver);
    }
    
    public String getId(Object object) {
        return reverse.get(object);
    }
    
    @SuppressWarnings("unchecked")
    public <T> SubjectResolver<T> getResolver(Class<T> clazz, String id) throws DefinitionException {
        Map<String, SubjectResolver<?>> map = resolvers.get(clazz);
        if (map == null) {
            throw new DefinitionException("Don't know how to resolve "
                    + clazz.getCanonicalName());
        }
        
        SubjectResolver<?> resolver = map.get(id);
        if (resolver == null) {
            throw new DefinitionException("Don't know how to resolve "
                    + clazz.getCanonicalName());
        }
        
        return (SubjectResolver<T>) resolver;
    }

}
