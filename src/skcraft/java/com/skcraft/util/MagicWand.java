/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.SessionDelegate;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;

public class MagicWand extends AbstractComponent {

    private Logger logger = createLogger(MagicWand.class);

    @InjectComponent
    private Sessions sessionManager;
    private SessionDelegate<MagicWandSession> sessions;
    private Map<String, WandFactory> types = new HashMap<String, WandFactory>();
    private Map<String, WandFactory> enumeratedTypes = new HashMap<String, WandFactory>();
    private Map<String, WandFactory> available = new HashMap<String, WandFactory>();
    private Map<String, WandFactory> enumeratedAvailable = new HashMap<String, WandFactory>();
    private File wandScriptsDir;

    @Override
    public void initialize() {
        wandScriptsDir = new File(Rebar.getInstance().getDataFolder(), "scriptedwands");
        sessions = sessionManager.forProfile(new MagicWandSession.Factory());
        reloadConfiguration();

        Rebar.getInstance().registerEvents(new Listener());
        Rebar.getInstance().registerCommands(WandCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void reloadConfiguration() {
        super.reloadConfiguration();

        Configuration config = Rebar.getInstance().getConfiguration();

        List<ConfigurationNode> nodes = config.getNodeList("magic-wand.scripted-wands", null);
        enumeratedAvailable.clear();
        enumeratedTypes.clear();
        for (ConfigurationNode node : nodes) {
            String filename = node.getString("filename");
            String description = node.getString("desc");
            String permission = node.getString("perm");
            List<String> aliases = node.getStringList("aliases", null);
            if (filename != null && aliases.size() > 0) {
                File path = new File(wandScriptsDir, filename);
                WandFactory factory = new BeanShellWandFactory(path, description, permission);
                enumeratedAvailable.put(aliases.get(0), factory);
                for (String alias : aliases) {
                    enumeratedTypes.put(alias, factory);
                }
            } else {
                logger.warning("MagicWand: Incomplete scripted wand definition found");
            }
        }
    }

    public MagicWandSession getSession(Player player) {
        return sessions.get(player);
    }

    public void register(WandFactory factory, String ... names) {
        if (names.length == 0) {
            return;
        }

        available.put(names[0], factory);

        for (String name : names) {
            types.put(name.toLowerCase(), factory);
        }
    }

    public WandFactory getFactory(String name) {
        if (name.matches("^![A-Za-z0-9_\\-]{1,32}$")) {
            File path = new File(wandScriptsDir, name.substring(1) + ".bsh");
            return new BeanShellWandFactory(path);
        }
        String nameLower = name.toLowerCase();
        if (enumeratedTypes.containsKey(nameLower)) {
            return enumeratedTypes.get(nameLower);
        }
        return types.get(nameLower);
    }

    public Map<String, WandFactory> getAvailable() {
        Map<String, WandFactory> allAvailable = new HashMap<String, WandFactory>();
        allAvailable.putAll(available);
        allAvailable.putAll(enumeratedAvailable);
        return allAvailable;
    }

    private class Listener implements org.bukkit.event.Listener {

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onPlayerInteract(PlayerInteractEvent event) {
            final Player player = event.getPlayer();
            final Block block = event.getClickedBlock();
            Action action = event.getAction();

            MagicWandSession session = getSession(player);
            WandActor actor = session.getActor();
            ItemStack held = player.getItemInHand();

            if (actor != null && held != null && session.isHeld(held)) {
                if (!actor.hasPermissionStill(player)) {
                    ChatUtil.error(player, "You no longer have permission for this wand. Unbind with /unbind");
                    return;
                }

                switch (action) {
                case LEFT_CLICK_BLOCK:
                    if (actor.interact(player, action, event.getClickedBlock(), event)) {
                        event.setCancelled(true);
                        return;
                    }
                case RIGHT_CLICK_BLOCK:
                    final Block affectedBlock = !player.isSneaking() ? block.getRelative(event.getBlockFace()) : block;

                    if (actor.interact(player, action, affectedBlock, event)) {
                        event.setCancelled(true);

                        // Fix an issue where the placed block is not removed client-side
                        if (affectedBlock.getTypeId() == 0) {
                            Rebar.getInstance().registerTimeout(new Runnable() {
                                @Override
                                public void run() {
                                    if (affectedBlock.getTypeId() != 0) return;
                                    if (!player.isOnline()) return;
                                    player.sendBlockChange(affectedBlock.getLocation(), 0, (byte)0);
                                }
                            }, 1);
                        }

                        return;
                    }
                default:
                    break;
                }
            }
        }

    }

    public static class WandCommands {

        private MagicWand magicWand;

        public WandCommands(MagicWand magicWand) {
            this.magicWand = magicWand;
        }

        @Command(aliases = {"bind", "wand"}, min = 0, max = -1, desc = "Bind a tool")
        public void bind(CommandContext context, CommandSender sender)
                throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);

            if (context.argsLength() == 0) {
                ChatUtil.header(sender, ChatColor.GRAY, ChatColor.YELLOW + "Wands");

                for (Map.Entry<String, WandFactory> entry : magicWand.getAvailable().entrySet()) {
                    if (!entry.getValue().hasPermission(player)) {
                        continue;
                    }

                    StringBuilder builder = new StringBuilder();
                    builder.append(ChatColor.GRAY);
                    builder.append("| ");
                    builder.append(ChatColor.AQUA);
                    builder.append(entry.getValue().getName());
                    builder.append(ChatColor.YELLOW);
                    builder.append(" as '");
                    builder.append(ChatColor.LIGHT_PURPLE);
                    builder.append("/");
                    builder.append(context.getCommand());
                    builder.append(" ");
                    builder.append(ChatColor.AQUA);
                    builder.append(entry.getKey());
                    builder.append(ChatColor.YELLOW);
                    builder.append("'");
                    ChatUtil.msg(sender, builder.toString());
                }

                ChatUtil.divider(sender, ChatColor.GRAY);

                return;
            }

            ItemStack held = player.getItemInHand();
            String name = context.getString(0);
            WandFactory factory = magicWand.getFactory(name);

            if (factory == null) {
                throw new CommandException("No wand type is known by the given name.");
            }

            if (!factory.hasPermission(player)) {
                throw new CommandPermissionsException();
            }

            if (held == null) {
                throw new CommandException("You are not holding anything.");
            }

            WandActor actor = factory.create(player, context);
            MagicWandSession session = magicWand.getSession(player);

            session.setHeld(held);
            session.setActor(actor);

            ChatUtil.msg(sender, ChatColor.YELLOW, "Your currently held item has been set to the '",
                    ChatColor.AQUA, actor.getName(), ChatColor.YELLOW, "' tool. " +
                    		"Use /unbind to unbind this tool.");
            ChatUtil.msg(sender, ChatColor.GRAY, "Quick help: ", actor.getHelp());
        }

        @Command(aliases = {"unbind"}, min = 0, max = 0, desc = "Unbind the current tool")
        public void unbind(CommandContext context, CommandSender sender)
                throws CommandException {

            Player player = CommandUtil.checkPlayer(sender);

            MagicWandSession session = magicWand.getSession(player);
            WandActor actor = session.getActor();

            if (actor != null) {
                session.setActor(null);
                ChatUtil.msg(sender, ChatColor.YELLOW, "Your wand has been unset from the '",
                        ChatColor.AQUA, actor.getName(), ChatColor.YELLOW, "' tool.");
            } else {
                throw new CommandException("You had nothing bound.");
            }
        }
    }

}
