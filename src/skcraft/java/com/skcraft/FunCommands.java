/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class FunCommands extends AbstractComponent {
	private static Random random = new Random();
	
	public final static String[] RESPONSES = new String[] {
        "Yes",
        "No",
        "Certainly",
        "In the near future",
        "Sorry, that looks unlikely",
        "I'm not so sure...",
        "Mind asking nicely?",
        "Possibly",
        ";)",
        "Do you really want to know?",
        "When pigs fly...",
        "Signs point to yes",
        "I'd rather not say"
    };
	
	public static void msgToAll(ChatColor color, String msg){
		for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
			ChatUtil.msg(player, color, msg);
		}
	}
	
	public static String getRandomPlayer(){
		List<? extends Player> players = Lists.newArrayList(BukkitUtil.getOnlinePlayers());
		return players.get(random.nextInt(players.size())).getName();
	}
	
    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class Commands {
        public Commands(FunCommands component) {
        }  
        
        @Command(aliases = { "gp" }, desc = "The Wizard", min = 1)
        @CommandPermissions({ "skcraft.fun.gp" })
        public void gp(CommandContext context, CommandSender sender) {
        	int numPlayers = BukkitUtil.getOnlinePlayers().size();
        	
        	msgToAll(ChatColor.GOLD, sender.getName() + " asks for the probability of '" + context.getJoinedStrings(0) + "'.");
        	if(numPlayers >= 3){
        		msgToAll(ChatColor.YELLOW, getRandomPlayer() + " (" + Integer.toString(0 + (int)(Math.random() * ((35 - 0) + 1))) + "%), " +
        								getRandomPlayer() + " (" + Integer.toString(36 + (int)(Math.random() * ((64 - 36) + 1))) + "%), " +
        								getRandomPlayer() + " (" + Integer.toString(65 + (int)(Math.random() * ((100 - 65) + 1))) + "%)");
        	}
        	if(numPlayers == 2){
        		msgToAll(ChatColor.YELLOW, getRandomPlayer() + " (" + Integer.toString(0 + (int)(Math.random() * ((64 - 0) + 1))) + "%), " +
        								getRandomPlayer() + " (" + Integer.toString(65 + (int)(Math.random() * ((100 - 65) + 1))) + "%)");
        	}
        	if(numPlayers == 1){
        		msgToAll(ChatColor.YELLOW, "Forever alone?");
        	}
        }
        
        @Command(aliases = { "8ball" }, desc = "The Wizard", min = 1)
        @CommandPermissions({ "skcraft.fun.8ball" })
        public void ball(CommandContext context, CommandSender sender) {
        	msgToAll(ChatColor.GOLD, sender.getName() + " asks '" + context.getJoinedStrings(0) + "'");
        	msgToAll(ChatColor.YELLOW, RESPONSES[random.nextInt(RESPONSES.length)]);
        }    
    }
}
