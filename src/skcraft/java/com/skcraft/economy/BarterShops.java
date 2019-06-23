/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft.economy;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.CompoundInventory;
import com.sk89q.worldguard.bukkit.util.Materials;
import com.skcraft.economy.Shop.InvalidPriceException;
import com.skcraft.economy.Shop.InvalidProductException;
import com.skcraft.economy.Shop.InvalidShopException;
import com.skcraft.economy.Shop.UnownedException;
import com.skcraft.protection.SignProtection;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static com.sk89q.rebar.util.BlockUtil.drop;
import static com.sk89q.rebar.util.BlockUtil.getState;
import static com.sk89q.rebar.util.ChatUtil.error;
import static com.sk89q.rebar.util.ChatUtil.msg;

public class BarterShops extends AbstractComponent {

    @InjectComponent
    private SignProtection protection;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new BlockListener());
        Rebar.getInstance().registerEvents(new PlayerListener());
    }

    @Override
    public void shutdown() {
    }

    private Block getInventoryBlock(Block sign) {
        Block inventory = null;

        for (int i = -1; i >= -4; i--) {
            Block test = sign.getRelative(0, i, 0);

            if (Materials.isInventoryBlock(test.getType())) {
                inventory = test;
                break;
            }
        }

        return inventory;
    }
    
    private Shop checkShop(Player player, Block block) {
        if (block.getType() != Material.WALL_SIGN) {
            return null;
        }

        Sign sign = getState(block, Sign.class);

        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[Shop]")) {
            return null;
        }

        Block inventory = getInventoryBlock(block);

        if (inventory == null) {
            drop(block);
            return null;
        }

        try {
            return new Shop(sign, inventory);
        } catch (InvalidPriceException e) {
            error(player, "Shop has an invalid price.");
            return null;
        } catch (InvalidProductException e) {
            error(player, "Shop has an invalid product.");
            return null;
        } catch (UnownedException e) {
            error(player, "Shop is unowned (needs a [Lock]).");
            return null;
        } catch (InvalidShopException e) {
            error(player, "Something is wrong with this shop: " + e.getClass().getCanonicalName());
            return null;
        }
    }
    
    public class PlayerListener implements Listener {
        @SuppressWarnings("deprecation")
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
            if (event.getHand() != EquipmentSlot.HAND) return;

            Player player = event.getPlayer();
            Shop shop = checkShop(player, event.getClickedBlock());
            if (shop == null) return;
            
            event.setCancelled(true);
            
            if (!event.getPlayer().isSneaking()) {
                msg(event.getPlayer(), ChatColor.RED, "Crouch and right click to buy.");
                return;
            }
            
            CompoundInventory buyerInven = new CompoundInventory(player.getInventory());
            TransactionEndPoint buyerEndPoint = new TransactionEndPoint(null, player, buyerInven);
            TransactionEndPoint shopEndPoint = shop.getEndPoint();
            Payment product = shop.getProduct();
            Payment price = shop.getPrice();
            
            if (!shopEndPoint.canAfford(product)) {
                msg(player, ChatColor.RED, "CURRENTLY OUT OF STOCK.");
                shop.updateSign();
                return;
            }
            
            if (!buyerEndPoint.canAfford(price)) {
                msg(player, ChatColor.RED, "YOU CANNOT AFFORD THIS.");
                shop.updateSign();
                return;
            }
            
            if (!shopEndPoint.canDeposit(price)) {
                msg(player, ChatColor.RED, "The shop's register needs to be cleared.");
                return;
            }
            
            if (!buyerEndPoint.canDeposit(product)) {
                msg(player, ChatColor.RED, "Your inventory does not have enough space.");
                return;
            }
            
            try {
                Payment forBuyer = shopEndPoint.withdraw(product);
                Payment forShop = buyerEndPoint.withdraw(price);
                forBuyer.deposit(buyerEndPoint);
                forShop.deposit(shopEndPoint);
            } catch (TransactionException e) {
                msg(player, ChatColor.RED, "An error occurred!");
                e.printStackTrace();
                return;
            }
            
            player.updateInventory();

            try {
                player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.CLICK2, 0);
                player.getWorld().playEffect(event.getClickedBlock().getLocation(), Effect.SMOKE, 4);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            msg(player, ChatColor.GOLD, "Order completed.");
            
            shop.updateSign();
        }
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onBlockBreak(BlockBreakEvent event) {
            Shop shop = checkShop(event.getPlayer(), event.getBlock());
            if (shop == null) return;
            
            Block under = event.getBlock().getRelative(0, -1, 0);
            
            if (!protection.canModify(under, event.getPlayer())) {
                error(event.getPlayer(), "You do not own the chest!");
                event.setCancelled(true);
                return;
            }
        }
        
        @EventHandler
        public void onBlockDamage(BlockDamageEvent event) {
            Player player = event.getPlayer();
            
            Shop shop = checkShop(player, event.getBlock());
            if (shop == null) return;
            
            CompoundInventory buyerInven = new CompoundInventory(player.getInventory());
            TransactionEndPoint buyerEndPoint = new TransactionEndPoint(null, player, buyerInven);
            TransactionEndPoint shopEndPoint = shop.getEndPoint();
            Payment product = shop.getProduct();
            Payment price = shop.getPrice();
            boolean canComplete = true;

            msg(player, ChatColor.AQUA, "Price: " + price.toString());
            msg(player, ChatColor.AQUA, "Selling: " + product.toString());
            
            if (shopEndPoint.canAfford(product)) {
                if (product instanceof ItemPayment) {
                    msg(player, ChatColor.GREEN, shopEndPoint.hasAmount(product) + " orders in stock.");
                } else {
                    msg(player, ChatColor.GREEN, "The shop can make this transaction.");
                }
            } else {
                msg(player, ChatColor.RED, "CURRENTLY OUT OF STOCK.");
                canComplete = false; 
            }
            
            if (buyerEndPoint.canAfford(price)) {
                msg(player, ChatColor.GREEN, "You can afford " + buyerEndPoint.hasAmount(price) + " orders.");
            } else {
                msg(player, ChatColor.RED, "YOU CANNOT AFFORD THIS.");
                canComplete = false;
            }
            
            if (canComplete) {
                msg(player, ChatColor.GOLD,
                        "RIGHT CLICK the sign to complete ONE transaction.");
            }

            // Debugging
            if (player.isSneaking()) {
                Block under = event.getBlock().getRelative(0, -1, 0);
                if (protection.canModify(under, player)) {
                    StringBuilder str = new StringBuilder();
                    
                    for (int i = 0; i < shop.getInventory().getSize(); i++) {
                        ItemStack item = shop.getInventory().getItem(i);
                        if (item != null) {
                            str.append(item.getTypeId() + ":" + item.getDurability()
                                    + "x" + item.getAmount() + " ");
                        }
                    }

                    msg(player, ChatColor.GRAY, "Items list: " + str.toString());
                }
                
            }
            
            shop.updateSign();
        }
        
        @EventHandler
        public void onSignChange(SignChangeEvent event) {
            if (!ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[Shop]")) {
                return;
            }

            Block under = getInventoryBlock(event.getBlock());
           
            if (under == null) {
                error(event.getPlayer(), "Shop signs must be above a chest!");
                drop(event.getBlock());
                event.setCancelled(true);
                return;
            }

            String productText = event.getLine(2);

            for (int i = -1; i >= -15; i--) {
                Block test = event.getBlock().getRelative(0, i, 0);

                if (!protection.canModify(test, event.getPlayer())) {
                    error(event.getPlayer(), "You do not own all chests below!");
                    drop(event.getBlock());
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (Shop.parse(productText) == null) {
                productText = event.getLine(3);
            }
            
            try {
                new Shop(BlockUtil.getState(event.getBlock(), Sign.class), event.getLine(1), productText, under);
            } catch (InvalidPriceException e) {
                error(event.getPlayer(), "Invalid price entered!");
                drop(event.getBlock());
                event.setCancelled(true);
                return;
            } catch (InvalidProductException e) {
                error(event.getPlayer(), "Invalid product entered!");
                drop(event.getBlock());
                event.setCancelled(true);
                return;
            } catch (UnownedException e) {
                error(event.getPlayer(), "You must protect the chest first!");
                drop(event.getBlock());
                event.setCancelled(true);
                return;
            } catch (InvalidShopException e) {
                error(event.getPlayer(), "Something is wrong with this shop: " + e.getClass().getCanonicalName());
                drop(event.getBlock());
                event.setCancelled(true);
                return;
            }

            event.setLine(0, "[Shop]");
            event.setLine(2, "will buy");
            event.setLine(3, productText);
            
            msg(event.getPlayer(), ChatColor.YELLOW,
                    "Shop created! Left click to check, right click to trade.");
        }
    }
    
}
