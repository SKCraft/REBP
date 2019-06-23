/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldguard.bukkit.util.Materials;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtil {
    
    public static CompoundInventory getDoubleChestInventory(Block block) {
        List<Inventory> inventories = new ArrayList<Inventory>();

        if (Materials.isInventoryBlock(block.getType())) {
            inventories.add(BlockUtil.getState(block, InventoryHolder.class).getInventory());

            if (block.getRelative(1, 0, 0).getType() == block.getType())
                inventories.add(BlockUtil.getState(block.getRelative(1, 0, 0), Chest.class).getInventory());

            if (block.getRelative(-1, 0, 0).getType() == block.getType())
                inventories.add(BlockUtil.getState(block.getRelative(-1, 0, 0), Chest.class).getInventory());

            if (block.getRelative(0, 0, 1).getType() == block.getType())
                inventories.add(BlockUtil.getState(block.getRelative(0, 0, 1), Chest.class).getInventory());

            if (block.getRelative(0, 0, -1).getType() == block.getType())
                inventories.add(BlockUtil.getState(block.getRelative(0, 0, -1), Chest.class).getInventory());
        }
        
        return new CompoundInventory(inventories); 
    }
    
    public static ItemStack parse(String line, short defaultData) {
        String[] parts = line.split(" +", 2);
        if (parts.length != 2) {
            return null;
        }
        
        int amount = 0;
        
        try {
            amount = Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException e) {
            return null;
        }
        
        return parse(parts[1], amount, defaultData);
    }
    
    public static ItemStack parse(String line, int amount, short defaultData) {
        int type = 0;
        int data = defaultData;
        
        String[] typeParts = line.split(":", 2);
        String guessType = typeParts[0];
        
        if (typeParts.length == 2) {
            try {
                data = Integer.parseInt(typeParts[1].trim());
                if (data < 0 || data >= 65536) {
                    return null;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        try {
            type = Integer.parseInt(guessType.trim());
            if (Material.getMaterial(type) == null) {
                return null;
            }
        } catch (NumberFormatException e) {
            ItemType weType = ItemType.lookup(guessType.trim());
            if (weType == null) {
                return null;
            }
            
            type = weType.getID();
        }
        
        return new ItemStack(type, amount, (short) data);
    }
    
    public static void dropItemStacks(Location loc, ItemStack item) {
        int left = item.getAmount();
        int stackSize = item.getMaxStackSize();
        if (stackSize == -1) stackSize = 64;
        
        while (left > 0) {
            int amt;
            if (left > stackSize) {
                amt = stackSize;
            } else {
                amt = left;
            }
            loc.getWorld().dropItem(loc,
                    new ItemStack(item.getTypeId(), amt, item.getDurability()));
            left -= amt;
        }
    }
    
    public static void dropItemStacksNaturally(Location loc, ItemStack item) {
        int left = item.getAmount();
        int stackSize = item.getMaxStackSize();
        if (stackSize == -1) stackSize = 64;
        
        while (left > 0) {
            int amt;
            if (left > stackSize) {
                amt = stackSize;
            } else {
                amt = left;
            }
            loc.getWorld().dropItemNaturally(loc,
                    new ItemStack(item.getTypeId(), amt, item.getDurability()));
            left -= amt;
        }
    }

    public static void reduceSlot(Inventory inven, int slot) {
        ItemStack item = inven.getItem(slot);
        if (item == null) {
        } else if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            inven.setItem(slot, item);
        } else {
            inven.setItem(slot, null);
        }
    }

    public static void reduceHeldItemSlot(Player player) {
        Inventory inven = player.getInventory();
        ItemStack item = player.getItemInHand();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            inven.setItem(player.getInventory().getHeldItemSlot(), item);
        } else {
            inven.setItem(player.getInventory().getHeldItemSlot(), null);
        }
    }

    public static boolean hasItems(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getTypeId() > 0 && item.getAmount() > 0) {
                return true;
            }
        }
        
        return false;
    }
    
}
