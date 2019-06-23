/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Java15Compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class CompoundInventory implements Inventory {

    private List<Inventory> inventories;

    public CompoundInventory(List<Inventory> inventories) {
        this.inventories = inventories;
    }

    public CompoundInventory(Inventory ... inventories) {
        this.inventories = Arrays.asList(inventories);
    }

    public List<Inventory> getInventories() {
        return inventories;
    }

    @Override
	public int getSize() {
        int size = 0;

        for (Inventory inventory : inventories) {
            size += inventory.getSize();
        }

        return size;
    }

    @Override
	public String getName() {
        if (inventories.size() == 0) return "NONE";
        return inventories.get(0).getName();
    }

    @Override
	public ItemStack getItem(int index) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            if (index - slotOffset < inventory.getSize()) {
                ItemStack item = inventory.getItem(index - slotOffset);
                if (item == null || item.getTypeId() == 0) {
                    return null;
                }
                return item;
            }
            slotOffset += inventory.getSize();
        }

        return null;
    }

    @Override
	public void setItem(int index, ItemStack item) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            if (index - slotOffset < inventory.getSize()) {
                inventory.setItem(index - slotOffset, item);
                return;
            }
            slotOffset += inventory.getSize();
        }
    }

    @Override
	public HashMap<Integer, ItemStack> addItem(ItemStack ... items) {
        HashMap<Integer, ItemStack> lastResult = new HashMap<Integer, ItemStack>();

        for (Inventory inventory : inventories) {
            lastResult = inventory.addItem(items);
            ItemStack[] stack = new ItemStack[lastResult.values().size()];
            lastResult.values().toArray(stack);
            if (stack.length == 0) {
                return lastResult;
            }
        }

        return lastResult;
    }

    @Override
	public HashMap<Integer, ItemStack> removeItem(ItemStack ... items) {
        HashMap<Integer, ItemStack> lastResult = new HashMap<Integer, ItemStack>();

        for (Inventory inventory : inventories) {
            lastResult = inventory.removeItem(items);
            // TODO: This is broken!
            items = (ItemStack[]) lastResult.values().toArray();
            if (items.length == 0) {
                return lastResult;
            }
        }

        return lastResult;
    }

    public int removeSingleItem(ItemStack item) {
        int left = item.getAmount() & 0xFF;

        for (Inventory inventory : inventories) {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack cur = inventory.getItem(i);
                if (cur == null) continue;
                if (!cur.isSimilar(item)) continue;
                int curAmount = cur.getAmount() & 0xFF;

                if (curAmount > left) {
                    cur.setAmount(curAmount - left);
                    inventory.setItem(i, cur);
                    left = 0;
                } else {
                    left -= curAmount;
                    inventory.setItem(i, null);
                }

                if (left == 0) {
                    return 0;
                }
            }
        }

        return left;
    }

    public List<ItemStack> getItemsList() {
        List<ItemStack> contents = new ArrayList<ItemStack>();

        for (Inventory inventory : inventories) {
            for (ItemStack stack : inventory.getContents()) {
                if (stack != null && stack.getTypeId() > 0 && stack.getAmount() != 0) {
                    contents.add(stack);
                }
            }
        }

        return contents;
    }

    @Override
	public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];

        int i = 0;
        for (Inventory inventory : inventories) {
            for (ItemStack stack : inventory.getContents()) {
                contents[i] = stack;
                i++;
            }
        }

        return contents;
    }

    @Override
	public void setContents(ItemStack[] items) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            inventory.setContents(Java15Compat.Arrays_copyOfRange(items, slotOffset, slotOffset + inventory.getSize()));
            slotOffset += inventory.getSize();
        }
    }

    @Override
    public ItemStack[] getStorageContents() {
        return getContents();
    }

    @Override
    public void setStorageContents(ItemStack[] itemStacks) throws IllegalArgumentException {
        setContents(itemStacks);
    }

    @Override
	public boolean contains(int materialId) {
        for (Inventory inventory : inventories) {
            if (inventory.contains(materialId))
                return true;
        }

        return false;
    }

    @Override
	public boolean contains(Material material) {
        return contains(material.getId());
    }

    @Override
	public boolean contains(ItemStack item) {
        for (Inventory inventory : inventories) {
            if (inventory.contains(item))
                return true;
        }

        return false;
    }

    @Override
	public boolean contains(int materialId, int amount) {
        for (Inventory inventory : inventories) {
            if (inventory.contains(materialId, amount))
                return true;
        }

        return false;
    }

    @Override
	public boolean contains(Material material, int amount) {
        return contains(material.getId(), amount);
    }

    @Override
	public boolean contains(ItemStack item, int amount) {
        for (Inventory inventory : inventories) {
            if (inventory.contains(item, amount))
                return true;
        }

        return false;
    }

    @Override
	public HashMap<Integer, ? extends ItemStack> all(int materialId) {
        HashMap<Integer, ItemStack> result = new HashMap<Integer, ItemStack>();
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(materialId).entrySet()) {
                result.put(entry.getKey() + slotOffset, entry.getValue());
            }
            slotOffset += inventory.getSize();
        }

        return result;
    }

    @Override
	public HashMap<Integer, ? extends ItemStack> all(Material material) {
        HashMap<Integer, ItemStack> result = new HashMap<Integer, ItemStack>();
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(material).entrySet()) {
                result.put(entry.getKey() + slotOffset, entry.getValue());
            }
            slotOffset += inventory.getSize();
        }

        return result;
    }

    @Override
	public HashMap<Integer, ? extends ItemStack> all(ItemStack item) {
        HashMap<Integer, ItemStack> result = new HashMap<Integer, ItemStack>();
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            for (Map.Entry<Integer, ? extends ItemStack> entry : inventory.all(item).entrySet()) {
                result.put(entry.getKey() + slotOffset, entry.getValue());
            }
            slotOffset += inventory.getSize();
        }

        return result;
    }

    @Override
	public int first(int materialId) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            int slot = inventory.first(materialId);
            if (slot != -1) {
                return slotOffset + slot;
            }
            slotOffset += inventory.getSize();
        }

        return -1;
    }

    @Override
	public int first(Material material) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            int slot = inventory.first(material);
            if (slot != -1) {
                return slotOffset + slot;
            }
            slotOffset += inventory.getSize();
        }

        return -1;
    }

    @Override
	public int first(ItemStack item) {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            int slot = inventory.first(item);
            if (slot != -1) {
                return slotOffset + slot;
            }
            slotOffset += inventory.getSize();
        }

        return -1;
    }

    @Override
	public int firstEmpty() {
        int slotOffset = 0;

        for (Inventory inventory : inventories) {
            int slot = inventory.firstEmpty();
            if (slot != -1) {
                return slotOffset + slot;
            }
            slotOffset += inventory.getSize();
        }

        return -1;
    }

    @Override
	public void remove(int materialId) {
        for (Inventory inventory : inventories) {
            inventory.remove(materialId);
        }
    }

    @Override
	public void remove(Material material) {
        for (Inventory inventory : inventories) {
            inventory.remove(material);
        }
    }

    @Override
	public void remove(ItemStack item) {
        for (Inventory inventory : inventories) {
            inventory.remove(item);
        }
    }

    @Override
	public void clear(int index) {
        setItem(index, null);
    }

    @Override
	public void clear() {
        for (int i = 0; i < getSize(); i++) {
            setItem(i, null);
        }
    }

    public int getNumUsedSlots() {
        int count = 0;

        for (int i = 0; i < getSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack == null) continue;
            if (stack.getTypeId() != 0 && stack.getAmount() != 0) {
                count++;
            }
        }

        return count;
    }

    public ItemStack findClosestItem(ItemStack wanted) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack == null) continue;
            if (stack.getTypeId() == wanted.getTypeId() && stack.getDurability() == wanted.getDurability()) {
                ItemStack ret = stack.clone();
                ret.setAmount(wanted.getAmount());
                return ret;
            }
        }
        return wanted;
    }

    public int getCountOf(int materialId) {
        int count = 0;

        for (int i = 0; i < getSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack == null) continue;
            if (stack.getTypeId() == materialId) {
                count += stack.getAmount() & 0xFFFF;
            }
        }

        return count;
    }

    public int getCountOf(Material material) {
        return getCountOf(material.getId());
    }

    public int getCountOf(ItemStack wanted) {
        int count = 0;

        for (int i = 0; i < getSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack == null) continue;
            if (stack.isSimilar(wanted)) {
                count += stack.getAmount() & 0xFFFF;
            }
        }

        return count;
    }

    public boolean hasSpaceFor(ItemStack wanted) {
        int neededSpace = wanted.getAmount();
        int maxStackSize = wanted.getMaxStackSize();
        if (maxStackSize == -1) maxStackSize = 64;

        for (int i = 0; i < getSize(); i++) {
            if (neededSpace < 0) {
                return true;
            }

            ItemStack stack = getItem(i);
            if (stack == null) {
                neededSpace -= maxStackSize;
                continue;
            }

            if (stack.isSimilar(wanted)) {
                int leftOver = maxStackSize - stack.getAmount();
                if (leftOver > 0) {
                    neededSpace -= leftOver;
                }
            }
        }

        return neededSpace < 0;
    }

    public boolean transferTo(Inventory destination) {
        boolean needsUpdate = false;
        int startSlot = 0;

        for (int si = 0; si < this.getSize(); si++) {
            ItemStack sourceItem = this.getItem(si);

            if (sourceItem == null || sourceItem.getAmount() == 0) {
                continue;
            }

            int freeSlot = -1;
            boolean found = false;
            boolean hitPotentialSlot = false;

            int max = sourceItem.getMaxStackSize();
            if (max == -1) max = 1; // Safety

            for (int di = startSlot; di < destination.getSize(); di++) {
                ItemStack destItem = destination.getItem(di);

                // Found a free slot!
                if (destItem == null || destItem.getAmount() == 0) {
                    freeSlot = di;
                    hitPotentialSlot = true;
                    // But we want to wait to see if we find a partial stack
                } else if (destItem.getType() == sourceItem.getType()) {
                    int left = max - (destItem.getAmount() & 0xFFFF);

                    if (left > sourceItem.getAmount()) { // More than enough
                        destItem.setAmount(destItem.getAmount() + sourceItem.getAmount());
                        this.setItem(si, null);

                        needsUpdate = true;
                        found = true;
                        break;
                    } else if (left > 0) { // Some free space
                        sourceItem.setAmount(sourceItem.getAmount() - left);
                        destItem.setAmount(max);

                        needsUpdate = true;
                        found = true;
                        startSlot = di;
                        break;
                    }
                } else {
                    if (!hitPotentialSlot) {
                        hitPotentialSlot = destItem.getAmount() < destItem.getMaxStackSize();
                    }
                }

                if (!hitPotentialSlot) {
                    startSlot = di;
                }
            }

            // Looks like we did not find a partial stack
            if (!found) {
                if (freeSlot > -1) {
                    destination.setItem(freeSlot, sourceItem);
                    this.setItem(si, null);
                } else {
                    // Uh oh -- no free space at all!
                    break;
                }
            }
        }

        return needsUpdate;
    }

    public static CompoundInventory forDoubleChest(Block block) {
        if (block.getType() != Material.CHEST) {
            throw new IllegalArgumentException("A chest block must be provided");
        }

        Inventory inven = BlockUtil.getState(block, Chest.class).getInventory();
        Block testBlock;

        testBlock = block.getRelative(1, 0, 0);
        if (testBlock.getType() == Material.CHEST) {
            return new CompoundInventory(inven, BlockUtil.getState(testBlock, Chest.class).getInventory());
        }

        testBlock = block.getRelative(-1, 0, 0);
        if (testBlock.getType() == Material.CHEST) {
            return new CompoundInventory(inven, BlockUtil.getState(testBlock, Chest.class).getInventory());
        }

        testBlock = block.getRelative(0, 0, 1);
        if (testBlock.getType() == Material.CHEST) {
            return new CompoundInventory(inven, BlockUtil.getState(testBlock, Chest.class).getInventory());
        }

        testBlock = block.getRelative(0, 0, -1);
        if (testBlock.getType() == Material.CHEST) {
            return new CompoundInventory(inven, BlockUtil.getState(testBlock, Chest.class).getInventory());
        }

        return new CompoundInventory(inven);
    }

    @Override
	public List<HumanEntity> getViewers() {
        return new ArrayList<HumanEntity>();
    }

    @Override
	public String getTitle() {
        return null;
    }

    @Override
	public InventoryType getType() {
        return null;
    }

    @Override
	public InventoryHolder getHolder() {
        return null;
    }

    @Override
	public ListIterator<ItemStack> iterator() {
        return null;
    }

    @Override
	public int getMaxStackSize() {
        return 64;
    }

    @Override
	public void setMaxStackSize(int size) {
    }

    @Override
	public ListIterator<ItemStack> iterator(int index) {
        throw new UnsupportedOperationException("iterator(index) not yet supported");
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
	public boolean containsAtLeast(ItemStack arg0, int arg1) {
        throw new UnsupportedOperationException("iterator(index) not yet supported");
	}

}
