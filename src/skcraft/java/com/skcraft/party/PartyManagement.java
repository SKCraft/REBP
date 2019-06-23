package com.skcraft.party;/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandRunnable;
import com.sk89q.rebar.util.CommandUtil;
import com.skcraft.cardinal.Cardinal;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.service.party.Member;
import com.skcraft.cardinal.service.party.Parties;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.service.party.PartyCache;
import com.skcraft.cardinal.service.party.PartyExistsException;
import com.skcraft.cardinal.service.party.Rank;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

public class PartyManagement extends AbstractComponent {

    private PartyCache partyCache;

    @Override
    public void initialize() {
        partyCache = Cardinal.load().getInstance(PartyCache.class);
        Rebar.getInstance().registerCommands(RootCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public PartyCache getPartyCache() {
        return partyCache;
    }

    public static class RootCommands {
        public RootCommands(PartyManagement component) {
        }

        @Command(aliases = { "party" }, desc = "Party management commands")
        @NestedCommand(Commands.class)
        @CommandPermissions({ "skcraft.party" })
        public void friends(CommandContext context, CommandSender sender) throws CommandException {
        }
    }

    public static class Commands {
        private PartyManagement component;

        public Commands(PartyManagement component) {
            this.component = component;
        }

        @Command(aliases = { "create" }, desc = "Create a new friends list", min = 1, max = 1)
        public void create(CommandContext context, CommandSender sender) throws CommandException {
            Rebar.getInstance().getExecutor().execute(new CommandRunnable(context, sender) {
                @Override
                public void execute(CommandContext context, CommandSender sender) throws CommandException {
                    PartyCache partyCache = component.getPartyCache();
                    Player player = CommandUtil.checkPlayer(sender);
                    String partyName = context.getString(0);
                    MojangId issuer = new MojangId(player.getUniqueId(), player.getName());

                    if (partyName.length() > 14) {
                        throw new CommandException("Friend list names can be at maximum 14 characters long");
                    }

                    Party party = new Party();
                    party.setName(partyName);
                    party.setCreateTime(new Date());
                    party.setMembers(Sets.newHashSet(new Member(issuer, Rank.OWNER)));
                    try {
                        partyCache.create(party);
                    } catch (PartyExistsException e) {
                        throw new CommandException("There is already a friends list by that name.");
                    }
                    partyCache.getManager().refresh(party);

                    ChatUtil.msg(sender, ChatColor.YELLOW, "Created party of name '" + partyName + "'.");
                }
            });
        }

        @Command(aliases = { "add" }, flags = "m", desc = "Add to a friends list", min = 2, max = 2)
        public void add(CommandContext context, CommandSender sender) throws CommandException {
            Rebar.getInstance().getExecutor().execute(new CommandRunnable(context, sender) {
                @Override
                public void execute(CommandContext context, CommandSender sender) throws CommandException {
                    PartyCache partyCache = component.getPartyCache();
                    Player player = CommandUtil.checkPlayer(sender);
                    MojangId caller = new MojangId(player.getUniqueId(), player.getName());
                    MojangId target = CommandUtil.lookupUser(Rebar.getInstance().getNameResolver(), context.getString(1));
                    Party party = partyCache.get(context.getString(0));

                    if (party == null) {
                        throw new CommandException("That party doesn't exist.");
                    } else if (!Parties.canManage(party, caller)) {
                        throw new CommandException("You can't manage that party.");
                    } else if (caller.equals(target)) {
                        throw new CommandException("You cannot add yourself to the party.");
                    } else {
                        partyCache.addMembers(party, Sets.newHashSet(new Member(target, context.hasFlag('m') ? Rank.MEMBER : Rank.MANAGER)));
                        partyCache.getManager().refresh(party);
                        ChatUtil.msg(sender, ChatColor.YELLOW, "Added '" + target.getName()
                                + "' to the list '" + party.getName() + "' as a " + (context.hasFlag('m') ? "member": "manager") + ".");
                    }
                }
            });
        }

        @Command(aliases = { "remove" }, desc = "Remove from a friends list", min = 2, max = 2)
        public void remove(CommandContext context, CommandSender sender) throws CommandException {
            Rebar.getInstance().getExecutor().execute(new CommandRunnable(context, sender) {
                @Override
                public void execute(CommandContext context, CommandSender sender) throws CommandException {
                    PartyCache partyCache = component.getPartyCache();
                    Player player = CommandUtil.checkPlayer(sender);
                    MojangId caller = new MojangId(player.getUniqueId(), player.getName());
                    MojangId target = CommandUtil.lookupUser(Rebar.getInstance().getNameResolver(), context.getString(1));
                    Party party = partyCache.get(context.getString(0));

                    if (party == null) {
                        throw new CommandException("That party doesn't exist.");
                    } else if (!Parties.canManage(party, caller)) {
                        throw new CommandException("You are not a manager of that party.");
                    } else if (caller.equals(target)) {
                        throw new CommandException("You cannot remove yourself from the party.");
                    } else {
                        partyCache.removeMembers(party, Sets.newHashSet(new Member(target, Rank.MEMBER)));
                        partyCache.getManager().refresh(party);

                        ChatUtil.msg(sender, ChatColor.YELLOW, "Removed '" + target.getName()
                                + "' from the party '" + party.getName() + "'.");
                    }
                }
            });
        }

        @Command(aliases = { "info" }, desc = "Get information about a party", min = 1, max = 1)
        public void info(CommandContext context, CommandSender sender) throws CommandException {
            Rebar.getInstance().getExecutor().execute(new CommandRunnable(context, sender) {
                @Override
                public void execute(CommandContext context, CommandSender sender) throws CommandException {
                    PartyCache partyCache = component.getPartyCache();
                    Party party = partyCache.get(context.getString(0));

                    if (party == null) {
                        throw new CommandException("That party doesn't exist.");
                    } else {
                        ChatUtil.msg(sender, ChatColor.YELLOW, "Party: ", ChatColor.AQUA, party.getName());
                        ChatUtil.msg(sender, ChatColor.YELLOW, "Created: ", ChatColor.AQUA, party.getCreateTime().toString());
                        ChatUtil.msg(sender, ChatColor.YELLOW, "Members: ", ChatColor.AQUA, Parties.getMemberListStr(party));
                        ChatUtil.msg(sender, ChatColor.GRAY, "Owners and members marked with * can add/remove players.");
                    }
                }
            });
        }
    }

}
