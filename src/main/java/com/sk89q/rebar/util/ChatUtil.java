/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatUtil {

    private static final int TABLE_DASH_LENGTH = 52;
    private static final String TABLE_DASH_BOTTOM = StringUtil.repeatString(
            "-", TABLE_DASH_LENGTH);

    private ChatUtil() {
    }

    public static void divider(CommandSender target, ChatColor color) {
        target.sendMessage(color + TABLE_DASH_BOTTOM);
    }

    public static void header(CommandSender target, ChatColor color,
            String title) {
        int titleLen = title.length() + 2;
        int dashLen = TABLE_DASH_LENGTH - titleLen;
        int halfLen = dashLen / 2;
        String dashes = StringUtil.repeatString("-", halfLen);
        target.sendMessage(color + dashes + " " + title + color + " " + dashes);
    }

    public static void error(CommandSender target, String message) {
        target.sendMessage(ChatColor.RED + message);
    }

    public static void error(CommandSender target, Throwable throwable) {
        target.sendMessage(ChatColor.RED
                + "An internal server error has occurred. Please report this error.");
        throwable.printStackTrace();
    }

    public static void blocked(CommandSender target, String message) {
        target.sendMessage(ChatColor.DARK_RED + message);
    }

    public static void msg(CommandSender target, ChatColor color, String message) {
        for (String line : message.split("\n")) {
            target.sendMessage(color + line);
        }
    }

    public static void msg(CommandSender target, Object... objects) {
        StringBuilder str = new StringBuilder();
        for (Object obj : objects) {
            str.append(obj);
        }
        for (String line : str.toString().split("\n")) {
            target.sendMessage(line);
        }
    }

    public static void msg(CommandSender target, List<Object> objects) {
        StringBuilder str = new StringBuilder();
        for (Object obj : objects) {
            str.append(obj);
        }
        for (String line : str.toString().split("\n")) {
            target.sendMessage(line);
        }
    }

    public static void sendHint(CommandSender target, String title, String message) {
        target.sendMessage(
                ChatUtil.addBg(ChatColor.GOLD +
                        "TIP: [" + ChatColor.GREEN + title
                        + ChatColor.GOLD
                        + "]: " + ChatColor.RESET + message, 0, 255, 255));
        if (target instanceof Player) {
            ((Player) target).playSound(((Player) target).getLocation(),
                    Sound.BLOCK_NOTE_PLING, 1, 1);
        }
    }

    public static String replaceColorMacros(String str) {
        str = str.replaceAll("(?i)&([a-f0-9klmnor])", "\u00A7$1");

        str = str.replace("`r", ChatColor.RED.toString());
        str = str.replace("`R", ChatColor.DARK_RED.toString());

        str = str.replace("`y", ChatColor.YELLOW.toString());
        str = str.replace("`Y", ChatColor.GOLD.toString());

        str = str.replace("`g", ChatColor.GREEN.toString());
        str = str.replace("`G", ChatColor.DARK_GREEN.toString());

        str = str.replace("`c", ChatColor.AQUA.toString());
        str = str.replace("`C", ChatColor.DARK_AQUA.toString());

        str = str.replace("`b", ChatColor.BLUE.toString());
        str = str.replace("`B", ChatColor.DARK_BLUE.toString());

        str = str.replace("`p", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("`P", ChatColor.DARK_PURPLE.toString());

        str = str.replace("`0", ChatColor.BLACK.toString());
        str = str.replace("`1", ChatColor.DARK_GRAY.toString());
        str = str.replace("`2", ChatColor.GRAY.toString());
        str = str.replace("`w", ChatColor.WHITE.toString());

        return str;
    }

    public static String addBg(String text, int r, int g, int b) {
        return text;
        /*
        r = (int) ((r / 255.0) * 15);
        g = (int) ((g / 255.0) * 15);
        b = (int) ((b / 255.0) * 15);
        return text + "\u00A7k" + (char) ((r << 8) + (g << 4) + b + 0x3400);*/
    }

    public static String toString(Block block) {
        return "(" + block.getX() + "," + block.getY() + "," + block.getZ()
                + ")";
    }

    public static String toString(Chunk chunk) {
        return "(" + chunk.getX() + "," + +chunk.getZ() + ")";
    }

}
