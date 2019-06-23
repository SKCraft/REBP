/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.annotations;

/**
 * A parameter to be loaded or saved.
 * 
 * @author sk89q
 */
public @interface Setting {

    /**
     * The name of the parameter. If unspecified, the variable name will be
     * used, except made lowercase and with dashes inserted before capitals
     * (i.e. houseType into house-type).
     * 
     * @return name of parameter
     */
    String value() default "";

    /**
     * Indicates whether the parameter is required.
     * 
     * @return true to indicate a required parameter
     */
    boolean required() default true;

}
