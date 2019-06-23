/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * A piece of data that has a name.
 */
public class NamedData {

    @Getter @Setter(AccessLevel.PACKAGE) @NonNull
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NamedData that = (NamedData) o;

        if (!name.equalsIgnoreCase(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
