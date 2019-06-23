/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import com.sk89q.rebar.LoaderException;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.services.WalletService;
import com.sk89q.rebar.util.CompoundInventory;
import com.sk89q.rebar.util.InventoryUtil;
import com.skcraft.protection.ProtectionQuery;
import com.skcraft.protection.SignProtection;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.rebar.util.InventoryUtil.getDoubleChestInventory;

class Shop {
    private Sign sign;
    private Block inventoryBlock;
    private Payment price;
    private Payment product;
    private OfflinePlayer owner;
    private CompoundInventory inventory;
    private TransactionEndPoint endPoint;
    
    public Shop(Sign sign, Block inventoryBlock) throws InvalidShopException {
        this(sign, sign.getLine(1), sign.getLine(3), inventoryBlock);
    }
    
    public Shop(Sign sign, String priceString, String productString, Block inventoryBlock) throws InvalidShopException {
        this.sign = sign;
        this.inventoryBlock = inventoryBlock;
        
        price = parse(priceString);
        product = parse(productString);
        owner = findShopOwner();
        
        if (price == null) {
            throw new InvalidPriceException();
        }
        
        if (product == null) {
            throw new InvalidProductException();
        }
        
        if (owner == null) {
            throw new UnownedException();
        }

        inventory = getDoubleChestInventory(inventoryBlock);
        WalletService walletService = Rebar.getInstance().getServiceManager().load(WalletService.class);
        endPoint = new TransactionEndPoint(walletService, owner, inventory);
    }
    
    public void updateSign() {
        if (!product.canAfford(endPoint)) {
            sign.setLine(0, ChatColor.RED + ChatColor.stripColor(sign.getLine(0)));
            sign.update();
        } else {
            sign.setLine(0, ChatColor.DARK_GREEN + ChatColor.stripColor(sign.getLine(0)));
            sign.update();
        }
    }
    
    private OfflinePlayer findShopOwner() {
        ProtectionQuery query;
        try {
            query = Rebar.getInstance().getLoader().load(SignProtection.class).query(inventoryBlock);
        } catch (LoaderException e) {
            e.printStackTrace();
            return null;
        }
        if (!query.isProtected()) {
            return null;
        }
        return Rebar.offlinePlayer(query.getOwnerName());
    }

    public Sign getSign() {
        return sign;
    }

    public OfflinePlayer getShopOwner() {
        return owner;
    }

    public Payment getPrice() {
        return price;
    }

    public Payment getProduct() {
        return product;
    }

    public CompoundInventory getInventory() {
        return inventory;
    }

    public TransactionEndPoint getEndPoint() {
        return endPoint;
    }
    
    public static Payment parse(String line) {
        String[] parts = line.split(" +", 2);
        if (parts.length != 2) {
            return null;
        }
        
        int amount = 0;
        
        try {
            amount = Integer.parseInt(parts[0].trim());
            if (amount < 0) {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        /*if (parts[1].equalsIgnoreCase("credits")
                || parts[1].equalsIgnoreCase("credit")
                || parts[1].equalsIgnoreCase("money")) {
            return new CreditsPayment(amount);
        }*/
        
        ItemStack item = InventoryUtil.parse(parts[1].replace(" ", ""), amount, (short) 0);
        if (item == null) {
            return null;
        }
        
        return new ItemPayment(item);
    }
    
    public static class InvalidShopException extends Exception {
        private static final long serialVersionUID = -4162020080190138288L;
    }
    
    public static class InvalidPriceException extends InvalidShopException {
        private static final long serialVersionUID = -2888712301291580293L;
    }
    
    public static class InvalidProductException extends InvalidShopException {
        private static final long serialVersionUID = -6145215319637194492L;
    }
    
    public static class UnownedException extends InvalidShopException {
        private static final long serialVersionUID = -2807498818904519360L;
    }
}
