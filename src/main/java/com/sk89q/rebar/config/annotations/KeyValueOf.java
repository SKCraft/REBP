/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.rebar.config.KeyValueLoader;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyValueOf {

    @SuppressWarnings("rawtypes")
    Class<? extends KeyValueLoader> value();
    
    @SuppressWarnings("rawtypes")
    Class<? extends Map> type() default HashMap.class;

}
