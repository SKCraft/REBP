/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.sk89q.worldedit.Vector;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class RTreeIndexTest {

    private final Vector inFountain = new Vector(5, 5, 5);
    private final Vector inCourtyard = new Vector(10, 10, 10);
    private final Vector outside = new Vector(10.1, 10.1, 10.1);
    private final Boundary courtyard = new CuboidBoundary(new Vector(0, 0, 0), new Vector(10, 10, 10));
    private final Boundary fountain = new CuboidBoundary(new Vector(0, 0, 0), new Vector(5, 5, 5));
    private final Zone<Object> courtyardZone = new Zone<Object>(courtyard);
    private final Zone<Object> fountainZone = new Zone<Object>(fountain);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFindContaining() {
        SpatialIndex<Object> index = new RTreeIndex<Object>();
        index.add(courtyardZone);
        index.add(fountainZone);

        List<Zone<Object>> results;

        results = index.findContaining(inCourtyard);
        assertThat(results, hasItem(courtyardZone));
        assertThat(results, not(hasItem(fountainZone)));

        results = index.findContaining(inFountain);
        assertThat(results, hasItems(courtyardZone, fountainZone));

        results = index.findContaining(outside);
        assertThat(results, not(hasItems(courtyardZone, fountainZone)));
    }

    @Test
    public void testRemove() {
        SpatialIndex<Object> index = new RTreeIndex<Object>();
        index.add(courtyardZone);
        index.add(fountainZone);

        List<Zone<Object>> results;

        results = index.findContaining(inCourtyard);
        assertThat(results, hasItem(courtyardZone));
        assertThat(results, not(hasItem(fountainZone)));

        results = index.findContaining(inFountain);
        assertThat(results, hasItems(courtyardZone, fountainZone));

        results = index.findContaining(outside);
        assertThat(results, not(hasItems(courtyardZone, fountainZone)));

        index.remove(courtyardZone);

        results = index.findContaining(inCourtyard);
        assertThat(results, not(hasItems(courtyardZone, fountainZone)));

        results = index.findContaining(inFountain);
        assertThat(results, hasItems(fountainZone));
        assertThat(results, not(hasItem(courtyardZone)));

        results = index.findContaining(outside);
        assertThat(results, not(hasItems(courtyardZone, fountainZone)));
    }

}
