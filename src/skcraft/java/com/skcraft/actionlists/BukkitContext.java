/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class BukkitContext extends Context {

    private final Event event;
    private Entity source;
    private BlockState block;
    private BlockState placedBlock;
    private BlockState clickedBlock;

    public BukkitContext(Event event) {
        this.event = event;
    }

    public Entity getSource() {
        return source;
    }

    public void setSource(Player source) {
        this.source = source;
    }

    public BlockState getBlock() {
        return block;
    }

    public void setBlock(BlockState block) {
        this.block = block;
    }

    public BlockState getPlacedBlock() {
        return placedBlock;
    }

    public void setPlacedBlock(BlockState placedBlock) {
        this.placedBlock = placedBlock;
    }

    public BlockState getClickedBlock() {
        return clickedBlock;
    }

    public void setClickedBlock(BlockState clickedBlock) {
        this.clickedBlock = clickedBlock;
    }

    public Event getEvent() {
        return event;
    }

}
