/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;

import com.sk89q.rebar.util.BlockUtil;

public abstract class AbstractState implements State {
    
    private Block block;
    private Sign sign;
    private BlockFace facing;
    private Block attachedTo;
    private org.bukkit.material.Sign signMat;
    private int thinkIn = -1;
    private boolean thinkReset = false;
    
    public AbstractState(Block block) {
        this.block = block;
        sign = BlockUtil.getState(block, Sign.class);
        MaterialData matData = sign.getData();
        if (matData instanceof org.bukkit.material.Sign) {
            signMat = ((org.bukkit.material.Sign) matData);
            facing = signMat.getFacing();
            attachedTo = block.getRelative(signMat.getAttachedFace());
        }
    }

    public Block getBlock() {
        return block;
    }

    public Sign getSign() {
        return sign;
    }

    public Block getAttachedTo() {
        return attachedTo;
    }

    public Block getSingleOutput() {
        return attachedTo.getRelative(signMat.getAttachedFace());
    }
    
    public BlockFace getFace0() {
        return facing;
    }
    
    public BlockFace getFace1() {
        return BlockUtil.getRightOf(facing);
    }
    
    public BlockFace getFace2() {
        return BlockUtil.getLeftOf(facing);
    }
    
    public boolean isPowered(Block block, BlockFace face) {
        Block target = block.getRelative(face);
        return target.isBlockFaceIndirectlyPowered(face)
                || target.getRelative(0, -1, 0).isBlockFacePowered(BlockFace.DOWN)
                || target.getRelative(0, 1, 0).isBlockFacePowered(BlockFace.UP);
    }
    
    public boolean isPoweredOutput(Block block) {
        return block.isBlockIndirectlyPowered();
    }
    
    public void setOutput(Block block, boolean val) {
        if (block.getType() != Material.LEVER) {
            return;
        }
        
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (data instanceof Lever) {
            Lever lever = (Lever) data;
            lever.setPowered(val);
            state.setData(lever);
            state.update();
        }
    }

    public boolean passthrough(int pin) {
        boolean val = in(pin);
        out(pin, val);
        return val;
    }

    public boolean passthrough(int pinIn, int pinOut) {
        boolean val = in(pinIn);
        out(pinOut, val);
        return val;
    }
    
    public boolean hasIn(int pin) {
        return pin >= 0 && pin < numIn();
    }
    
    public boolean hasOut(int pin) {
        return pin >= 0 && pin < numOut();
    }
    
    public void setNextTick(int ticks) {
        thinkIn = ticks;
        thinkReset = false;
    }
    
    public void clearTick() {
        thinkReset = true;
        thinkIn = -1;
    }
    
    public int getNextTick() {
        return thinkIn;
    }
    
    public boolean tickCleared() {
        return thinkReset;
    }
    
    public void reset() {
        thinkIn = -1;
        thinkReset = false;
    }
    
}
