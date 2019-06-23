/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.claim.ClaimEntry;
import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.util.WandActor;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class OwnershipWand implements WandActor {

    private final ChunkOwnership ownership;
    private ClaimCache claimCache;
    
    public OwnershipWand(ChunkOwnership ownership) {
        this.ownership = ownership;
        this.claimCache = ownership.getClaimCache();
    }

    @Override
    public String getName() {
        return "Chunk Ownership Lookup";
    }

    @Override
    public String getHelp() {
        return "Hit blocks or place blocks to see the ownership of the chunk.";
    }
    
    private void perform(final Player player, final Chunk chunk) {
        ownership.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ClaimEntry entry = claimCache.getIfPresent(new WorldVector3i(chunk.getWorld().getName(), chunk.getX(), 0, chunk.getZ()));
                if (entry != null) {
                    ChatUtil.msg(player, getOwnershipLine(chunk, entry));
                } else {
                    ChatUtil.error(player, "Ownership information for the given chunk is not available.");
                }
            }
        });
    }

    private String getOwnershipLine(Chunk chunk, ClaimEntry entry) {
        StringBuilder s = new StringBuilder();

        s.append(ChatColor.GRAY);
        s.append("O: ");

        s.append(ChatColor.BLUE);
        s.append("(");
        s.append(chunk.getX());
        s.append(", ");
        s.append(chunk.getZ());
        s.append(") ");

        if (entry.getClaim() == null) {
            s.append(ChatColor.YELLOW);
            s.append("Owner: ");
            s.append(ChatColor.DARK_RED);
            s.append("NONE");
        } else {
            s.append(ChatColor.YELLOW);
            s.append("Owner: ");
            s.append(ChatColor.AQUA);
            s.append(entry.getClaim().getOwner().getName());
            s.append(ChatColor.GRAY);
            s.append(" / Party: ");

            String party = entry.getClaim().getParty();
            if (party == null) {
                s.append(ChatColor.DARK_RED);
                s.append("NONE");
            } else {
                s.append(ChatColor.AQUA);
                s.append(party);
            }
        }

        return s.toString();
    }

    @Override
    public boolean interact(Player player, Action action, Block block, PlayerInteractEvent event) {
        if (block == null) {
            return true;
        }
        
        perform(player, block.getChunk());
        return true;
    }

    @Override
    public boolean hasPermissionStill(Player player) {
        return true;
    }

    @Override
    public void destroy() {
    }

}
