/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.protection;

import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.StringUtil;
import com.sk89q.worldguard.bukkit.util.Materials;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtectionQuery {
    
    private List<Block> protectingSigns = new ArrayList<Block>();
    
    public ProtectionQuery(Block block) {
        detect(block);
    }
    
    private boolean detect(Block block) {
        Block above = block.getRelative(0, 1, 0);

        // This checks to see if the given block is the actual Lock sign
        if (detectProtectingSign(block)) return true;

        // This checks if the sign above is a lock sign (meaning that we
        // can't break this block underneath)
        if (detectProtectingSignPost(above)) return true;

        detectBlock(block);

        if (isHorizontalConnecting(block.getType())) {
            if (detectConnectedBlock(block.getRelative(-1, 0, 0), block.getType())) return true;
            if (detectConnectedBlock(block.getRelative(1, 0, 0), block.getType())) return true;
            if (detectConnectedBlock(block.getRelative(0, 0, -1), block.getType())) return true;
            if (detectConnectedBlock(block.getRelative(0, 0, 1), block.getType())) return true;
        }
        
        return false;
    }

    private boolean detectBlock(Block block) {
        // This checks old status-quo way of protecting with signs underneath
        if (detectBelow(block)) return true;

        // This checks the new way of signs attached to the chest
        if (detectAttached(block)) return true;

        return false;
    }

    private boolean detectConnectedBlock(Block block, Material expectedType) {
        if (block.getType() == expectedType) {
            return detectBlock(block);
        }

        return false;
    }
    
    private boolean detectProtectingSign(Block block) {
        if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
            return false;
        }

        if (isLockSign(block)) {
            setProtectingSign(block);
            return true;
        }
        
        return false;
    }
    
    private boolean detectProtectingSignPost(Block block) {
        if (block.getType() != Material.SIGN_POST) {
            return false;
        }

        if (isLockSign(block)) {
            setProtectingSign(block);
            return true;
        }
        
        return false;
    }
    
    private boolean detectProtectingWallSign(Block block, Block attachedTo) {
        if (block.getType() != Material.WALL_SIGN) {
            return false;
        }
        
        MaterialData materialData = block.getState().getData();

        if (materialData != null && !(materialData instanceof Attachable)
                || !block.getRelative(((Attachable) materialData).getAttachedFace()).equals(attachedTo)) {
            return false;
        }
        
        if (isLockSign(block)) {
            setProtectingSign(block);
            return true;
        }
        
        return false;
    }
    
    private boolean isLockSign(Block block) {
        return BlockUtil.getState(block, Sign.class).getLine(0).equalsIgnoreCase("[Lock]");
    }
    
    private boolean detectBelow(Block block) {
        if (!isProtectedContainer(block.getType())) return false;
        
        detectProtectingSignPost(block.getRelative(0, -1, 0));

        return protectingSigns.size() > 0;
    }
    
    private boolean detectAttached(Block block) {
        if (!isProtected(block.getType())) return false;

        // Check all the sides of this block to see if it's either a [Lock]
        // wall sign or an adjacent protected block
        if (detectAdjacentAttached(block.getRelative(1, 0, 0), block)) return true;
        if (detectAdjacentAttached(block.getRelative(-1, 0, 0), block)) return true;
        if (detectAdjacentAttached(block.getRelative(0, 0, 1), block)) return true;
        if (detectAdjacentAttached(block.getRelative(0, 0, -1), block)) return true;
        
        return false;
    }
    
    private boolean detectAdjacentAttached(Block block, Block attachedTo) {
        if (detectProtectingWallSign(block, attachedTo)) return true;
        
        // If we have a chest/furnace/etc. next to this block then see if
        // that adjacent block is protected with a wall sign
        if (isHorizontalConnecting(attachedTo.getType()) && attachedTo.getType() == block.getType()) {
            if (detectProtectingWallSign(block.getRelative(1, 0, 0), block)) return true;
            if (detectProtectingWallSign(block.getRelative(-1, 0, 0), block)) return true;
            if (detectProtectingWallSign(block.getRelative(0, 0, 1), block)) return true;
            if (detectProtectingWallSign(block.getRelative(0, 0, -1), block)) return true;
        }
        
        return false;
    }

    public static boolean isHorizontalConnecting(Material material) {  
        return material == Material.CHEST || material == Material.TRAPPED_CHEST;
    }

    public static boolean isVerticalConnecting(Material material) {  
        return material == Material.WOODEN_DOOR;
    }

    public static boolean dependsOnBottom(Material material) {  
        return material == Material.WOODEN_DOOR;
    }

    public static boolean isProtected(Material material) {  
        return true;
        /*return material == Material.CHEST
                || material == Material.FURNACE
                || material == Material.BURNING_FURNACE
                || material == Material.DISPENSER
                || material == Material.NOTE_BLOCK
                || material == Material.JUKEBOX
                || material == Material.WOODEN_DOOR
                || material == Material.BREWING_STAND
                || material == Material.TRAP_DOOR;*/
    }

    public static boolean isProtectedContainer(Material material) {
        return Materials.isInventoryBlock(material);
    }
    
    public static boolean isUnsafe(Material material) {
        return material == Material.SAND
                || material == Material.GRAVEL
                || material == Material.WALL_SIGN
                || material == Material.SIGN_POST
                || material == Material.TNT
                || material == Material.ICE
                || material == Material.LEAVES;
    }

    public boolean isProtected() {
        return protectingSigns.size() > 0;
    }
    
    public boolean isListedOwner(String name) {
        if (protectingSigns.size() == 0) return false;
        
        for (Block protectingSign : protectingSigns) {
            boolean hasAccess = false;
            Sign sign = BlockUtil.getState(protectingSign, Sign.class);
            String[] lines = sign.getLines();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.length() == 0) continue;
                
                if (line.charAt(0) == '#') {
                    // TODO: FRIENDS
                } else if (line.equalsIgnoreCase(name) ||
                        (name.length() > 15 && name.substring(0, 15).equalsIgnoreCase(line))) {
                    hasAccess = true;
                    break;
                }
            }
            
            if (!hasAccess) {
                return false;
            }
        }
        
        return true;
    }

    private void setProtectingSign(Block protectingSign) {
        protectingSigns.add(protectingSign);
    }

    public String getOwnerName() {
        if (protectingSigns.size() == 0) return null;
        
        String name = null;
        
        for (Block protectingSign : protectingSigns) {
            Sign sign = BlockUtil.getState(protectingSign, Sign.class);
            String[] lines = sign.getLines();
            String testName = lines[1].trim();
            
            if (name == null) {
                name = testName;
            } else if (!name.equalsIgnoreCase(testName)) {
                return "*CONFLICT*";
            }
        }
        
        return name;
    }

    public String getAccessibleString() {
        if (protectingSigns.size() == 0) return "*EVERYONE*";
        
        Set<String> names = new HashSet<String>();
        
        for (Block protectingSign : protectingSigns) {
            Sign sign = BlockUtil.getState(protectingSign, Sign.class);
            String[] lines = sign.getLines();
            for (int i = 1; i < lines.length; i++) {
                String name = lines[i].trim();
                if (name.length() > 0)
                    names.add(name.toLowerCase());
            }
        }
        
        return StringUtil.joinString(names, ", ", 0);
    }
    
}
