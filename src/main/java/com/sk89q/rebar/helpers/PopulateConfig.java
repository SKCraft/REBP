/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.helpers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.sk89q.rebar.LoaderHelperProcessor;

@Retention(RetentionPolicy.RUNTIME)
@LoaderHelperProcessor(ConfigurationLoaderHelper.class)
public @interface PopulateConfig {

}
