/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.Unlisted;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.skcraft.security.ClientIdentityVerifier;

@Unlisted
public class OpMode extends AbstractComponent {

    private final static Logger logger = Logger.getLogger(OpMode.class.getCanonicalName());
    private final static Random random = new Random();
    private final Set<String> opped = new HashSet<String>();
    @InjectComponent
    private InteractivePrompt prompter;
    @InjectComponent
    private ClientIdentityVerifier verifier;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(OpModeCommands.class, this);
        Rebar.getInstance().registerEvents(new PlayerListener());
    }

    @Override
    public void shutdown() {
    }

    public void setOpMode(Player player) {
        logger.warning("OP-MODE: Set op: " + player.getName());
        player.setOp(true);
        opped.add(player.getName().toLowerCase());
    }

    public void unsetOpMode(Player player) {
        logger.warning("OP-MODE: Deopped: " + player.getName());
        player.setOp(false);
        opped.remove(player.getName().toLowerCase());
    }

    public boolean hasOpMode(Player player) {
        return opped.contains(player.getName().toLowerCase());
    }

    public InteractivePrompt getPrompter() {
        return prompter;
    }

    private static class OpModePrompt implements Prompt {
        private String challenge;
        private OpMode component;

        public OpModePrompt(OpMode component) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < 2; i++) {
                str.append(random.nextInt(10));
            }
            challenge = str.toString();
            this.component = component;
        }

        @Override
        public void accept(CommandSender sender, String message)
                throws PromptComplete {
            if (message.trim().equals("cancel")) {
                sender.sendMessage(ChatColor.YELLOW + "Op mode cancelled.");
                throw new PromptComplete();
            } else if (message.trim().equals(challenge)) {
                component.setOpMode((Player) sender);
                sender.sendMessage(ChatColor.YELLOW + "Use /deopme to turn off. ALL ACTIONS LOGGED.");
                throw new PromptComplete();
            } else {
                sender.sendMessage(ChatColor.RED + "Please enter the challenge: " + challenge);
            }
        }

        @Override
        public void start(CommandSender sender) {
            sender.sendMessage(ChatColor.RED + "Please enter the challenge: " + challenge);
        }
    }

    public static class OpModeCommands {
        private OpMode component;

        public OpModeCommands(OpMode component) {
            this.component = component;
        }

        @Command(aliases = { "op" }, desc = "Op someone", min = 1, max = 1)
        @CommandPermissions({ "skcraft.op-mode" })
        public void op(CommandContext context, CommandSender sender) throws CommandException {
            if (sender instanceof Player) {
                throw new CommandException("You must set op from console! Use /opme instead");
            }

            Player player = CommandUtil.matchPlayerExactly(sender, context.getString(0));
            player.setOp(true);
            logger.warning("Opped " + player.getName());
            ChatUtil.msg(sender, ChatColor.YELLOW, "Made " + player.getName() + " op");
        }

        @Command(aliases = { "deop" }, desc = "Deop someone", min = 1, max = 1)
        @CommandPermissions({ "skcraft.op-mode" })
        public void deop(CommandContext context, CommandSender sender) throws CommandException {
            if (sender instanceof Player) {
                throw new CommandException("You must deop from console! Use /deopme instead");
            }

            Player player = CommandUtil.matchPlayerExactly(sender, context.getString(0));
            player.setOp(false);
            logger.warning("Deopped " + player.getName());
            ChatUtil.msg(sender, ChatColor.YELLOW, "Deopped " + player.getName());
        }

        @Command(aliases = { "opme" }, desc = "Turn on op-mode")
        @CommandPermissions({ "skcraft.op-mode" })
        public void opMe(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            logger.warning("OP-MODE: Trying to be opped: " + player.getName());
            /*if (!component.verifier.isVerified(player)) {
                logger.warning("OP-MODE: Bad authorization bucket: " + player.getName());
                throw new CommandException("You are not in the right authorization bucket for this request.");
            }*/
            component.getPrompter().prompt(player, new OpModePrompt(component));
        }

        @Command(aliases = { "deopme" }, desc = "Turn off op-mode")
        @CommandPermissions({ "skcraft.op-mode" })
        public void deopMe(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            component.unsetOpMode(player);
            ChatUtil.msg(sender, ChatColor.YELLOW, "You've been de-opped!");
        }

    }

    public class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            if (hasOpMode(player)) {
                unsetOpMode(player);
            }
        }
    }
}
