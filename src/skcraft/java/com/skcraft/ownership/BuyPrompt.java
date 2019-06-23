/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CompoundInventory;
import com.skcraft.Prompt;
import com.skcraft.PromptComplete;
import com.skcraft.cardinal.service.claim.Claim;
import com.skcraft.cardinal.service.claim.ClaimAttemptException;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.claim.ClaimRequest;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.util.WorldVector3i;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

class BuyPrompt implements Prompt {
    private final ChunkOwnership component;
    private final ClaimRequest existingRequest;
    private final Player player;
    private final int cost;

    public BuyPrompt(ChunkOwnership component, ClaimRequest existingRequest, Player player, int cost) {
        this.component = component;
        this.existingRequest = existingRequest;
        this.player = player;
        this.cost = cost;
    }

    private ChunkOwnership getComponent() {
        return component;
    }

    @Override
    public void accept(final CommandSender sender, String message) throws PromptComplete {
        // No / cancel
        if (message.trim().equals("no") || message.trim().equals("cancel")) {
            ChatUtil.msg(sender, ChatColor.YELLOW, "Purchase cancelled.");
            throw new PromptComplete();

        // Yes
        } else if (message.trim().equals("yes")) {
            getComponent().getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        purchase();
                        ChatUtil.msg(sender, ChatColor.YELLOW, "Purchase completed.");
                    } catch (CommandException e) {
                        ChatUtil.error(sender, e.getMessage());
                    }
                }
            });
            throw new PromptComplete();

        // No response
        } else {
            ChatUtil.msg(sender, ChatColor.GREEN,
                    "Would you like to make this purchase? Type 'yes' or 'no'.");
        }
    }

    private void purchase() throws CommandException {
        ChunkOwnershipConfig config = getComponent().getConfiguration();
        ClaimCache claimCache = getComponent().getClaimCache();

        ClaimRequest request = new ClaimRequest(component.getClaimCache(), existingRequest.getOwner(), existingRequest.getParty());
        request.addPositions(existingRequest.getUnclaimed());
        request.addPositions(existingRequest.getAlreadyOwned());

        try {
            request.checkQuota(config.maxChunkClaim);
            request.checkRemaining();
        } catch (ClaimAttemptException e) {
            throw new CommandException(e.getMessage());
        }

        if (request.getUnclaimed().size() + request.getAlreadyOwned().size() == 0) {
            // But it could turn out that there are no unclaimed chunks left
            throw new CommandException("It seems that someone else has claimed all of the areas that you requested.");
        }

        if (request.getParty() != null) {
            Party party = component.getPartyCache().get(request.getParty());
            if (party == null) {
                throw new CommandException("The party that you specified (" + request.getParty() + ") does not exist.");
            }
        }

        CompoundInventory inven = new CompoundInventory(player.getInventory());
        if (inven.getCountOf(config.paymentItem) < cost) {
            throw new CommandException("You cannot afford this purchase anymore.");
        }

        List<WorldVector3i> positions = Lists.newArrayList();
        positions.addAll(request.getUnclaimed());
        positions.addAll(request.getAlreadyOwned());
        claimCache.getClaimMap().save(positions, request.getOwner(), request.getParty());
        claimCache.putAll(claimCache.getClaimMap().getAll(positions).values());

        ItemStack item = new ItemStack(component.getConfiguration().paymentItem, cost);
        inven.removeSingleItem(item);

    }

    @Override
    public void start(CommandSender sender) {
        ChatUtil.msg(sender, ChatColor.GREEN,
                "Would you like to make this purchase? Type 'yes' or 'no'.");
    }
}
