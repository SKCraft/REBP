/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.service.claim.Claim;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.util.WorldVector3i;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class OwnershipSubcommands extends AbstractOwnershipCommands {

    public OwnershipSubcommands(ChunkOwnership ownership) {
        super(ownership);
    }

    @Command(aliases = {"force-claim"}, min = 1, max = 2, desc = "Unclaim chunks")
    @CommandPermissions("skcraft.ownership.management.force-claim")
    public void forceClaim(CommandContext context, CommandSender sender) throws CommandException {
        Player player = CommandUtil.checkPlayer(sender);
        World world = player.getWorld();

        final ClaimCache claimCache = getOwnership().getClaimCache();
        final List<Chunk> chunks = getSelection(world, player, false);
        final List<WorldVector3i> positions = toPositions(chunks);

        final String partyName;
        if (context.argsLength() > 1) {
            partyName = getParty(context.getString(1)).getName();
        } else {
            partyName = null;
        }

        ChatUtil.msg(player, ChatColor.GRAY, "Your request is being processed...");

        getOwnership().getExecutor().execute(new LockingCommandRunnable(context, sender) {
            @Override
            public void executeBeforeRelease(CommandContext context, CommandSender sender) throws CommandException {
                // Get owner
                MojangId owner;
                String ownerName = context.getString(0).trim();
                if (ownerName.equalsIgnoreCase(Claim.SERVER_OWNER_NAME)) {
                    owner = new MojangId(Claim.SERVER_OWNER_UUID, Claim.SERVER_OWNER_NAME);
                } else {
                    owner = CommandUtil.lookupUser(Rebar.getInstance().getNameResolver(), ownerName);
                }

                if (partyName != null) {
                    Party party = getOwnership().getPartyCache().get(partyName);
                    if (party == null) {
                        throw new CommandException("There's no party named '" + partyName + "'.");
                    }
                }

                claimCache.getClaimMap().save(positions, owner, partyName);
                claimCache.putAll(claimCache.getClaimMap().getAll(positions).values());

                ChatUtil.msg(sender, ChatColor.AQUA, positions.size(), " ", ChatColor.YELLOW, "chunks have been updated.");
            }
        });
    }

    @Command(aliases = {"force-unclaim"}, min = 0, max = 0, desc = "Unclaim chunks")
    @CommandPermissions("skcraft.ownership.management.force-claim")
    public void forceUnclaim(CommandContext context, CommandSender sender) throws CommandException {

        Player player = CommandUtil.checkPlayer(sender);
        World world = player.getWorld();

        final ClaimCache claimCache = getOwnership().getClaimCache();
        final List<Chunk> chunks = getSelection(world, player, false);
        final List<WorldVector3i> positions = toPositions(chunks);

        ChatUtil.msg(player, ChatColor.GRAY, "Your request is being processed...");

        getOwnership().getExecutor().execute(new LockingCommandRunnable(context, sender) {
            @Override
            public void executeBeforeRelease(CommandContext context, CommandSender sender) throws CommandException {
                claimCache.getClaimMap().remove(positions);
                claimCache.putAsUnclaimed(positions);

                ChatUtil.msg(sender, ChatColor.AQUA, positions.size(), " ", ChatColor.YELLOW, "chunks have been unclaimed.");
            }
        });
    }

}
