/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CapsuleConfig {

    private Set<String> capsules = new HashSet<String>();

}
