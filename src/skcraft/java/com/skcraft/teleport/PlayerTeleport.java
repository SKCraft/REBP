/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.teleport;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.CompoundInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class PlayerTeleport extends AbstractComponent implements Listener {
    private Multimap<UUID, UUID> acceptedPlayers = HashMultimap.create();
    private static final Material[] VALID_MATERIALS = new Material[]{
            Material.STONE,
            Material.GRASS,
            Material.DIRT,
            Material.COBBLESTONE,
            Material.WOOD,
            Material.SAPLING,
            Material.BEDROCK,
            Material.SAND,
            Material.GRAVEL,
            Material.GOLD_ORE,
            Material.IRON_ORE,
            Material.COAL_ORE,
            Material.LOG,
            Material.LEAVES,
            Material.SPONGE,
            Material.GLASS,
            Material.LAPIS_ORE,
            Material.LAPIS_BLOCK,
            Material.DISPENSER,
            Material.SANDSTONE,
            Material.NOTE_BLOCK,
            Material.BED_BLOCK,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.WEB,
            Material.APPLE,
            Material.BOW,
            Material.ARROW,
            Material.COAL,
            Material.DIAMOND,
            Material.IRON_INGOT,
            Material.GOLD_INGOT,
            Material.IRON_SWORD,
            Material.WOOD_SWORD,
            Material.WOOD_SPADE,
            Material.WOOD_PICKAXE,
            Material.WOOD_AXE,
            Material.STONE_SWORD,
            Material.STONE_SPADE,
            Material.STONE_PICKAXE,
            Material.STONE_AXE,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_SPADE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_AXE,
            Material.STICK,
            Material.BOWL,
            Material.MUSHROOM_SOUP,
            Material.GOLD_SWORD,
            Material.GOLD_SPADE,
            Material.GOLD_PICKAXE,
            Material.GOLD_AXE,
            Material.STRING,
            Material.FEATHER,
            Material.SULPHUR,
            Material.WOOD_HOE,
            Material.STONE_HOE,
            Material.IRON_HOE,
            Material.DIAMOND_HOE,
            Material.GOLD_HOE,
            Material.SEEDS,
            Material.WHEAT,
            Material.BREAD,
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET,
            Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_HELMET,
            Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS,
            Material.IRON_BOOTS,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.GOLD_HELMET,
            Material.GOLD_CHESTPLATE,
            Material.GOLD_LEGGINGS,
            Material.GOLD_BOOTS,
            Material.FLINT,
            Material.PORK,
            Material.GRILLED_PORK,
            Material.PAINTING,
            Material.GOLDEN_APPLE,
            Material.WOOD_DOOR,
            Material.BUCKET,
            Material.WATER_BUCKET,
            Material.LAVA_BUCKET,
            Material.MINECART,
            Material.SADDLE,
            Material.IRON_DOOR,
            Material.REDSTONE,
            Material.SNOW_BALL,
            Material.BOAT,
            Material.LEATHER,
            Material.MILK_BUCKET,
            Material.CLAY_BRICK,
            Material.CLAY_BALL,
            Material.SUGAR_CANE,
            Material.PAPER,
            Material.BOOK,
            Material.SLIME_BALL,
            Material.EGG,
            Material.COMPASS,
            Material.FISHING_ROD,
            Material.WATCH,
            Material.GLOWSTONE_DUST,
            Material.RAW_FISH,
            Material.COOKED_FISH,
            Material.INK_SACK,
            Material.BONE,
            Material.SUGAR,
            Material.CAKE,
            Material.BED,
            Material.COOKIE,
            Material.SHEARS,
            Material.MELON,
            Material.RECORD_3,
            Material.RECORD_4,
            Material.RECORD_5,
            Material.RECORD_6,
            Material.RECORD_7,
            Material.RECORD_8,
            Material.RECORD_9,
            Material.RECORD_10,
            Material.RECORD_11,
            Material.RECORD_12,
    };

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
        Rebar.getInstance().registerCommands(TeleportCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public boolean isTeleportInventory(Inventory inventory) {
        return inventory != null && inventory.getTitle() != null && inventory.getTitle().equalsIgnoreCase("Teleport To");
    }

    public Material getMaterial(Player player) {
        Material[] materials = Material.values();
        Material material = VALID_MATERIALS[Math.abs(("skcraft:" + player.getName()).hashCode()) % VALID_MATERIALS.length];
        if (new ItemStack(material, 1).getItemMeta() == null) {
            return Material.ARROW;
        }
        return material;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND && event.getItem() != null &&
                event.getItem().getType() == Material.ENDER_PEARL && event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            Inventory menuInventory = Bukkit.createInventory(null, 54, "Teleport To");

            int index = 0;
            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(event.getPlayer())) continue;

                Material material = getMaterial(target);
                ItemStack itemStack = new ItemStack(material, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(target.getName());
                itemStack.setItemMeta(itemMeta);
                menuInventory.setItem(index, itemStack);
                index++;
                if (index >= menuInventory.getSize()) {
                    break; // TODO: Handle more players
                }
            }

            event.getPlayer().openInventory(menuInventory);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isTeleportInventory(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (isTeleportInventory(event.getDestination())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isTeleportInventory(event.getClickedInventory())) {
            event.setCancelled(true);

            if (event.getAction() != InventoryAction.PICKUP_ALL) {
                return;
            }

            ItemStack item = event.getCurrentItem();
            HumanEntity humanEntity = event.getWhoClicked();

            if (item != null && item.getType() != Material.AIR && humanEntity instanceof Player) {
                humanEntity.closeInventory();

                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayerExact(name);
                CompoundInventory inven = new CompoundInventory(humanEntity.getInventory());

                if (inven.getCountOf(Material.ENDER_PEARL) == 0) {
                    humanEntity.sendMessage(ChatColor.RED + "You need one ender pearl to initiate a signal lock.");
                } else if (humanEntity.getFireTicks() > 0) {
                    humanEntity.sendMessage(ChatColor.RED + "The flames interfere with the Ender transmission.");
                } else if (target != null) {
                    if (acceptedPlayers.remove(target.getUniqueId(), humanEntity.getUniqueId())) {
                        int slot = inven.first(Material.ENDER_PEARL);
                        ItemStack stack = inven.getItem(slot);
                        if (stack.getAmount() == 1) {
                            inven.setItem(slot, null);
                        } else {
                            stack.setAmount(stack.getAmount() - 1);
                            inven.setItem(slot, stack);
                        }

                        humanEntity.getWorld().playEffect(humanEntity.getLocation(), Effect.ENDER_SIGNAL, 0);
                        humanEntity.teleport(target.getLocation().add(0.5, 0.5, 0.5));
                        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
                        target.getWorld().playEffect(target.getLocation(), Effect.ENDER_SIGNAL, 0);
                        target.getWorld().strikeLightningEffect(target.getLocation().subtract(0, 1, 0));
                        ChatUtil.msg(target, ChatColor.YELLOW, humanEntity.getName() + " has teleported to you.");
                        ChatUtil.msg(humanEntity, ChatColor.YELLOW, "Whoosh!");
                    } else {
                        ChatUtil.msg(humanEntity, ChatColor.RED, "You will have to ask " + target.getName() + " to /accept " + humanEntity.getName());
                    }
                } else {
                    ChatUtil.msg(humanEntity, ChatColor.RED, name + " isn't online anymore!");
                }
            }
        } else if (isTeleportInventory(event.getInventory())) {
            switch (event.getAction()) {
                case MOVE_TO_OTHER_INVENTORY:
                    event.setCancelled(true);
            }
        }
    }

    public static class TeleportCommands {
        private final PlayerTeleport component;

        public TeleportCommands(PlayerTeleport component) {
            this.component = component;
        }

        @Command(aliases = {"accept"}, desc = "Let a player teleport to you", min = 1, max = 1)
        public void accept(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            Player target = CommandUtil.matchSinglePlayer(sender, context.getString(0));
            component.acceptedPlayers.put(player.getUniqueId(), target.getUniqueId());
            ChatUtil.msg(sender, ChatColor.YELLOW, "Now " + target.getName() + " may teleport to you one time.");
        }

        @Command(aliases = {"clearaccept"}, desc = "Don't let anyone teleport to you", min = 0, max = 0)
        public void clear(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            component.acceptedPlayers.removeAll(player.getUniqueId());
            ChatUtil.msg(sender, ChatColor.YELLOW, "Now no one can teleport to you.");
        }
    }

}
