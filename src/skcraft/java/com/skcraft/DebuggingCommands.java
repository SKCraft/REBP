/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DebuggingCommands extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(DebugCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class DebugCommands {
        public DebugCommands(DebuggingCommands component) {
        }

        @Command(aliases = { "relchunk" }, usage = "[<radius>]",
                desc = "Reload chunks", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.reload-chunk" })
        public void reloadChunk(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            World world = player.getWorld();
            Chunk cur = player.getLocation().getBlock().getChunk();

            if (context.argsLength() > 0) {
                int radius = context.getInteger(0);

                if (radius > 15) {
                    throw new CommandException("Specified radius is too large! Max 15");
                }

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (world.isChunkLoaded(x + cur.getX(), z + cur.getZ())) {
                            ChatUtil.msg(sender, ChatColor.YELLOW,
                                    "Force reloading chunk: (" + (x + cur.getX()) + ", " + (z + cur.getZ()) + ")");
                            world.unloadChunk(x + cur.getX(), z + cur.getZ(), true, false);
                            world.loadChunk(x + cur.getX(), z + cur.getZ());
                        } else {
                        }
                    }
                }
            } else {
                ChatUtil.msg(sender, ChatColor.YELLOW,
                        "Force reloading chunk: (" + cur.getX() + ", " + cur.getZ() + ")");
                world.unloadChunk(cur.getX(), cur.getZ(), true, false);
                world.loadChunk(cur.getX(), cur.getZ());
            }
        }

        @Command(aliases = { "f" }, usage = "[<amount>]",
                desc = "Set food", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-food" })
        public void setFood(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Food level=" + player.getFoodLevel() + ".");
                return;
            }
            player.setFoodLevel(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set food level to " + player.getFoodLevel() + ".");
        }

        @Command(aliases = { "e" }, usage = "[<amount>]",
                desc = "Set exhaustion level", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-exhaustion" })
        public void setExhaustion(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Exhaustion level=" + player.getExhaustion() + ".");
                return;
            }
            player.setExhaustion(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set exhaustion level to " + player.getExhaustion() + ".");
        }

        @Command(aliases = { "h" }, usage = "[<amount>]",
                desc = "Set health", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-health" })
        public void setHealth(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Health=" + player.getHealth() + ".");
                return;
            }
            player.setHealth(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set health to " + player.getHealth() + ".");
        }

        @Command(aliases = { "xp" }, usage = "[<amount>]",
                desc = "Set XP", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-xp" })
        public void setXP(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "XP=" + player.getTotalExperience() + ".");
                return;
            }
            player.setTotalExperience(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set XP level to " + player.getTotalExperience() + ".");
        }

        @Command(aliases = { "level" }, usage = "[<amount>]",
                desc = "Set level", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-level" })
        public void setLevel(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Level=" + player.getLevel() + ".");
                return;
            }
            player.setLevel(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set level to " + player.getLevel() + ".");
        }

        @Command(aliases = { "txp" }, usage = "[<amount>]",
                desc = "Set total experience level", min = 0, max = 1)
        @CommandPermissions({ "skcraft.debug.set-level" })
        public void setTotalExperience(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            if (context.argsLength() == 0) {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Total experience=" + player.getTotalExperience() + ".");
                return;
            }
            player.setTotalExperience(context.getInteger(0));
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set total experience to " + player.getTotalExperience() + ".");
        }

        @Command(aliases = { "su" }, usage = "",
                desc = "Set survival game mode", min = 0, max = 0)
        @CommandPermissions({ "skcraft.debug.survival" })
        public void setSurvival(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            player.setGameMode(GameMode.SURVIVAL);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set to survival.");
        }

        @Command(aliases = { "cr" }, usage = "",
                desc = "Set survival game mode", min = 0, max = 0)
        @CommandPermissions({ "skcraft.debug.creative" })
        public void setCreative(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            player.setGameMode(GameMode.CREATIVE);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Set to creative.");
        }

        @Command(aliases = { "chunkcount" }, usage = "",
                desc = "Get number of chunks", min = 0, max = 0)
        @CommandPermissions({ "skcraft.debug.chunkcount" })
        public void chunkCount(CommandContext context, CommandSender sender) throws CommandException {
            for (World world : Rebar.server().getWorlds()) {
                int numChunks = world.getLoadedChunks().length;
                ChatUtil.msg(sender, ChatColor.YELLOW, world.getName() + ": " + numChunks);
            }
        }

        @Command(aliases = { "itemname" }, usage = "",
                desc = "Set the name of an item", min = 1, max = -1)
        @CommandPermissions({ "skcraft.debug.setitemname" })
        public void setItemName(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ItemStack item = player.getItemInHand();
            if (item == null) {
                throw new CommandException("No item held in hand!");
            }
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(WorldGuardPlugin.inst().replaceMacros(sender, context.getJoinedStrings(0)));
            item.setItemMeta(meta);
            player.setItemInHand(item);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Item meta data set!");
        }

        @Command(aliases = { "addlore" }, usage = "",
                desc = "Add lore to an item", min = 1, max = -1, flags = "a")
        @CommandPermissions({ "skcraft.debug.addlore" })
        public void addLore(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ItemStack item = player.getItemInHand();
            if (item == null) {
                throw new CommandException("No item held in hand!");
            }
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore == null || !context.hasFlag('a')) {
                lore = new ArrayList<>();
            }
            lore.add(WorldGuardPlugin.inst().replaceMacros(sender, context.getJoinedStrings(0)));
            meta.setLore(lore);
            item.setItemMeta(meta);
            player.setItemInHand(item);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Item meta data set!");
        }

        @Command(aliases = { "clearlore" }, usage = "",
                desc = "Remove lore from an item", min = 0, max = 0)
        @CommandPermissions({ "skcraft.debug.removelore" })
        public void removeLore(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ItemStack item = player.getItemInHand();
            if (item == null) {
                throw new CommandException("No item held in hand!");
            }
            ItemMeta meta = item.getItemMeta();
            meta.setLore(new ArrayList<String>());
            item.setItemMeta(meta);
            player.setItemInHand(item);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Item meta data set!");
        }

        @Command(aliases = { "distributeitem" }, usage = "",
                desc = "Remove lore from an item", min = 0, max = 0)
        @CommandPermissions({ "skcraft.debug.distributeitem" })
        public void distributeItem(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ItemStack item = player.getItemInHand();
            if (item == null) {
                throw new CommandException("No item held in hand!");
            }
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.getInventory().addItem(item.clone());
            }
            ChatUtil.msg(sender, ChatColor.YELLOW, "Item distributed!");
        }
    }

}
