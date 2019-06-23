/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import com.skcraft.actionlists.SubjectResolver;

/**
 * Indicates a field that uses a {@link SubjectResolver}.
 * 
 * @author sk89q
 */
public @interface Subject {
    
    Class<?> value();

}
