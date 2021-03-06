/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresComponent {
    Class<? extends Component>[] value();
}
