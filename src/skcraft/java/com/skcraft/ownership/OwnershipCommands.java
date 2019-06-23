/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.CompoundInventory;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.service.claim.ClaimAttemptException;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.claim.ClaimRequest;
import com.skcraft.cardinal.util.WorldVector3i;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class OwnershipCommands extends AbstractOwnershipCommands {

    public OwnershipCommands(ChunkOwnership ownership) {
        super(ownership);
    }

    @Command(aliases = {"claim"}, min = 0, max = 1, desc = "Buy chunks")
    public void claim(CommandContext context, CommandSender sender)
            throws CommandException {

        final ClaimCache claimCache = getOwnership().getClaimCache();
        final ChunkOwnershipConfig config = getOwnership().getConfiguration();

        final Player player = CommandUtil.checkPlayer(sender);
        final MojangId owner = new MojangId(player.getUniqueId(), player.getName());
        final World world = player.getWorld();

        final String partyName;
        if (context.argsLength() > 0) {
            partyName = getParty(context.getString(0)).getName();
        } else {
            partyName = null;
        }

        final List<Chunk> chunks = getSelection(world, player);
        final List<WorldVector3i> positions = toPositions(chunks);

        ChatUtil.msg(player, ChatColor.GRAY, "Your request is being processed...");

        getOwnership().getExecutor().execute(new LockingCommandRunnable(context, sender) {
            @Override
            public void executeBeforeRelease(CommandContext context, CommandSender sender) throws CommandException, InterruptedException, ExecutionException {
                // Then build a claim request based on the unclaimed chunks
                // Filter out chunks that are already owned by the player or other players
                ClaimRequest request = new ClaimRequest(claimCache, owner, partyName);
                request.addPositions(positions);
                try {
                    request.checkQuota(config.maxChunkClaim);
                    request.checkRemaining();
                } catch (ClaimAttemptException e) {
                    throw new CommandException(e.getMessage());
                }

                int gratisClaimCount = Math.max(0, config.freeChunkCount - request.getCurrentTotalOwnedCount());
                int cost = Math.max(0, request.getUnclaimed().size() - gratisClaimCount) * config.priceUnder1000;

                // Show report
                ChatUtil.msg(player, ChatColor.GOLD, "--- Chunk Purchase Report ---");
                ChatUtil.msg(player, ChatColor.GOLD, "Purchasing ", ChatColor.AQUA, request.getPositionCount(), ChatColor.GOLD, " chunk(s)");
                ChatUtil.msg(player, ChatColor.GOLD, "Note: ", ChatColor.AQUA, request.getAlreadyOwned().size(), ChatColor.GOLD, " selected owned by you already");
                ChatUtil.msg(player, ChatColor.GOLD, "Note: ", ChatColor.AQUA, request.getOwnedByOthers().size(), ChatColor.GOLD, " selected owned by others");
                ChatUtil.msg(player, ChatColor.GOLD, "Total cost: ", ChatColor.AQUA, cost, " ", config.paymentItemName);
                if (partyName == null) {
                    ChatUtil.msg(player, ChatColor.RED, "NOTE!! You didn't specify a friends list (/claim LISTHERE)");
                    ChatUtil.msg(player, ChatColor.RED, "A friends list gives other people of your choosing access!");
                    ChatUtil.msg(player, ChatColor.RED, "To set it later, use /ownership set-friends LIST");
                }

                // Check that the player can afford it
                CompoundInventory inven = new CompoundInventory(player.getInventory());
                if (inven.getCountOf(config.paymentItem) < cost) {
                    throw new CommandException("You cannot afford this purchase.");
                }

                getOwnership().getPrompter().prompt(player, new BuyPrompt(getOwnership(), request, player, cost));
            }
        });

    }

    @Command(aliases = {"unclaim"}, min = 0, max = 0, desc = "Unclaim chunks")
    public void unclaim(CommandContext context, CommandSender sender) throws CommandException {

        final Player player = CommandUtil.checkPlayer(sender);
        final MojangId owner = new MojangId(player.getUniqueId(), player.getName());
        World world = player.getWorld();

        final List<Chunk> chunks = getSelection(world, player);
        final List<WorldVector3i> positions = toPositions(chunks);
        final ClaimCache claimCache = getOwnership().getClaimCache();

        ChatUtil.msg(player, ChatColor.GRAY, "Your request is being processed...");

        getOwnership().getExecutor().execute(new LockingCommandRunnable(context, sender) {
            @Override
            public void executeBeforeRelease(CommandContext context, CommandSender sender) throws CommandException {
                // Filter chunks
                ClaimRequest request = new ClaimRequest(claimCache, owner, null);
                request.addPositions(positions);

                if (!request.hasClaimed()) {
                    throw new CommandException("You have no chunks in your selection that have been claimed by you.");
                }

                claimCache.getClaimMap().remove(request.getAlreadyOwned());
                claimCache.putAsUnclaimed(request.getAlreadyOwned());

                ChatUtil.msg(sender, "Selected chunks have been unregistered.");
            }
        });
    }

    @Command(aliases = {"ownership"}, desc = "Chunk ownership commands")
    @NestedCommand(OwnershipSubcommands.class)
    public void ownership(CommandContext context, CommandSender sender) throws CommandException {
    }

}
