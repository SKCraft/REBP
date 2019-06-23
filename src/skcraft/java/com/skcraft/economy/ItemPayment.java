/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import org.bukkit.inventory.ItemStack;

public class ItemPayment implements Payment {
    
    private ItemStack item;
    
    public ItemPayment(ItemStack item) {
        if (item == null) {
            throw new IllegalArgumentException("Can't have null item payment");
        }
        this.item = item;
    }

    @Override
    public void deposit(TransactionEndPoint endPoint) throws TransactionException {
        if (!endPoint.getInventory().hasSpaceFor(item)) {
            throw new InsufficientSpaceException();
        }
        
        endPoint.getInventory().addItem(item.clone());
    }

    @Override
    public Payment withdraw(TransactionEndPoint endPoint) throws TransactionException {
        ItemStack item = endPoint.getInventory().findClosestItem(this.item);

        if (endPoint.getInventory().getCountOf(item) == 0) {
            throw new InsufficientFundsException();
        }

        endPoint.getInventory().removeSingleItem(item);
        return new ItemPayment(item);
    }
    
    public ItemStack getItem() {
        return item;
    }

    @Override
    public boolean canAfford(TransactionEndPoint endPoint) {
        ItemStack item = endPoint.getInventory().findClosestItem(this.item);
        return endPoint.getInventory().getCountOf(item) >= item.getAmount();
    }
    
    @Override
    public int getAmountAfforded(TransactionEndPoint endPoint) {
        ItemStack item = endPoint.getInventory().findClosestItem(this.item);
        return item.getAmount() > 0 ? endPoint.getInventory().getCountOf(item) / item.getAmount() : 9999;
    }

    @Override
    public boolean canDeposit(TransactionEndPoint endPoint) {
        return endPoint.getInventory().hasSpaceFor(item);
    }
    
    public String toString() {
        return item.getAmount() + " " + item.getType().name() + " (id: " + item.getTypeId()
                + ") (data: " + item.getDurability() + ")";
    }

}
