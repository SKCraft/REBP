/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AttachmentTarget {

    String value();
    
}
