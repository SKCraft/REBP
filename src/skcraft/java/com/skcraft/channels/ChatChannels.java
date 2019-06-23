/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.SessionDelegate;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.management.ChatChannelsMXBean;
import com.sk89q.rebar.management.ManagementUtils;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class ChatChannels extends AbstractComponent implements ChatChannelsMXBean {

    private static final Logger logger = createLogger(ChatChannels.class);

    @InjectComponent private Sessions sessionsComponent;
    private SessionDelegate<ChannelSession> sessions;
    private ChannelManager channels = new ChannelManager();
    private AsyncPlayerChatEvent lastPublicChat = null;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());
        Rebar.getInstance().registerCommands(Commands.class, this);
        sessions = sessionsComponent.forProfile(new ChannelSessionFactory());
        ManagementUtils.register(this, "com.sk89q.skcraft.channels:type=ChatChannels");
    }

    @Override
    public void shutdown() {
    }

    public ChannelManager getChannels() {
        return channels;
    }

    public ChannelSession getSession(Player player) {
        return sessions.get(player);
    }

    public boolean isInChannel(Player player) {
        return sessions.get(player).getChannel() != null;
    }

    public boolean isPublic(Player player, AsyncPlayerChatEvent event) {
        return !isInChannel(player) || lastPublicChat == event;
    }

    public void sendCommuneMessage(String senderName, String message, ChatColor senderColor) {
        for (Player player : Rebar.server().getOnlinePlayers()) {
            Channel chan = sessions.get(player).getChannel();
            if (chan != null) {
                player.sendMessage(ChatColor.DARK_GRAY + "(" +
                        ChatColor.DARK_GRAY + ChatColor.stripColor(senderName) +
                        ChatColor.DARK_GRAY + ") " + highlightName(player, message, ChatColor.DARK_GRAY));
            } else {
                player.sendMessage(
                       "(" + senderColor
                        + senderName + ChatColor.WHITE + ") "
                        + message);
            }
        }
    }

    public void sendCommuneSharedMessage(String message) {
        Rebar.getInstance().getServer().broadcastMessage(ChatColor.GRAY
                + "*"
                + ChatColor.WHITE + message);
    }

    /**
     * Play a sound indicating that the player was mentioned.
     *
     * @param player the player
     */
    public void ding(Player player) {
        /*User user = users.getUser(player);

        if (user.isDingDisabled()) {
            return;
        }*/

        ChannelSession sess = getSession(player);

        if (sess.canDing()) {
            sess.rememberDing();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 0.7f, 4f);
        }
    }

    /**
     * Highlight instances of a player's name in a message and play the ding
     * sound as well.
     *
     * @param player the player
     * @param text text to highlight
     * @param textColor color of text
     * @return new text
     */
    private String highlightName(Player player, String text, ChatColor textColor) {
        //User user = users.getUser(player);
        boolean found = false;

        String name = player.getName().toLowerCase();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            String test = words[i].replaceAll("[^A-Za-z0-9]", "").toLowerCase();
            boolean matches = (test.length() > 2 || test.matches("^[^aeiouy]{2}$")) && name.startsWith(test);

            /*if (user.hasConfiguredHighlights()) {
                matches = user.matchesHighlight(test);
            }*/

            if (matches) {
                words[i] = ChatColor.AQUA + words[i] + textColor.toString();
                found = true;
            }
        }

        /*if (found) {
            ding(player);
        }*/

        return StringUtil.joinString(words, " ");
    }

    public class PlayerListener implements Listener {

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            Player sender = event.getPlayer();
            Set<Player> recipients = event.getRecipients();
            String msg = event.getMessage();

            Channel channel = sessions.get(sender).getChannel();

            boolean forcedPublic = channel != null && msg.endsWith("/") && msg.length() > 1;
            boolean isPublic = forcedPublic || channel == null;

            if (forcedPublic) {
                event.setMessage(msg.substring(0, msg.length() - 1));
                lastPublicChat = event;
            }

            if (!isPublic) {
                event.setFormat("<" + ChatColor.DARK_GRAY + "#" + ChatColor.WHITE + "%1$s" + ChatColor.WHITE + "> %2$s");
            }

            Iterator<Player> it = recipients.iterator();
            while (it.hasNext()) {
                Player player = it.next();

                Channel chan = sessions.get(player).getChannel();
                if (player.equals(sender)) {
                } else if (isPublic && chan != null) {
                    player.sendMessage(ChatColor.DARK_GRAY + "<" +
                            ChatColor.DARK_GRAY + ChatColor.stripColor(event.getPlayer().getDisplayName()) +
                            ChatColor.DARK_GRAY + "> " + highlightName(player, event.getMessage(), ChatColor.DARK_GRAY));
                    it.remove();
                } else if (!isPublic && (chan == null || channel != chan)) {
                    it.remove();
                } else {
                    player.sendMessage("<" + (!isPublic ? ChatColor.DARK_GRAY + "#" : "") +
                            ChatColor.WHITE + event.getPlayer().getDisplayName() +
                            ChatColor.WHITE + "> " + highlightName(player, event.getMessage(), ChatColor.WHITE));
                    it.remove();
                }
            }
        }

    }

    public static class Commands {
        private ChatChannels component;

        public Commands(ChatChannels component) {
            this.component = component;
        }

        @Command(aliases = {"join"}, usage = "<channel>", desc = "Join a chat channel",
                 min = 1, max = 1)
        public void join(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            String id = ChannelManager.normalize(context.getString(0));
            Channel channel = component.getChannels().get(id);
            ChannelSession sess = component.getSession(player);
            ChatUtil.msg(sender, ChatColor.YELLOW, "** You've joined #" + id + ". /leave to leave **");
            ChatUtil.msg(sender, ChatColor.GRAY, "(In #" + id + ": " + StringUtil.joinString(channel.getMembersList(), ", ", 0) + ")");
            sess.join(channel);
            logger.info(player.getName() + " joined #" + id);
        }

        @Command(aliases = {"leave"}, desc = "Leave your current channel",
                 min = 0, max = 0)
        public void leave(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ChannelSession sess = component.getSession(player);
            Channel channel = sess.getChannel();
            if (channel == null) {
                throw new CommandException("You are not in a channel!");
            }
            String id = channel.getId();
            sess.leave();
            ChatUtil.msg(sender, ChatColor.YELLOW, "You've left #" + id + ".");
            logger.info(player.getName() + " left #" + id);
        }

        @Command(aliases = {"here"}, desc = "See who is here",
                 min = 0, max = 0)
        public void here(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            ChannelSession sess = component.getSession(player);
            Channel channel = sess.getChannel();
            if (channel == null) {
                throw new CommandException("You are not in a channel!");
            }
            String id = channel.getId();
            ChatUtil.msg(sender, ChatColor.YELLOW, "In #" + id + ": " + StringUtil.joinString(channel.getMembersList(), ", ", 0));
        }

        @Command(aliases = {"listall"}, desc = "See who is here", min = 0, max = 0)
        @CommandPermissions({"skcraft.chat-channels.list-all"})
        public void listAl(CommandContext context, CommandSender sender) throws CommandException {
            for (Player player : Rebar.server().getOnlinePlayers()) {
                ChannelSession sess = component.getSession(player);
                Channel channel = sess.getChannel();
                if (channel != null) {
                    String id = channel.getId();
                    ChatUtil.msg(sender, player.getName() + " is in #" + id);
                }
            }
        }
    }

    @Override
    public int getChannelCount() {
        return channels.size();
    }

}
