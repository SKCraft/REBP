/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DestroyEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.UseEntityEvent;
import com.sk89q.worldguard.bukkit.util.Entities;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.service.claim.Claim;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.claim.ClaimEntry;
import com.skcraft.cardinal.service.party.Member;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.service.party.Rank;
import com.skcraft.cardinal.util.WorldVector3i;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;

public class OwnershipListener implements Listener {
    private static final Set<Material> FREELY_USABLE = new HashSet<>();
    private final ClaimCache claimCache;

    static {
        for (Material material : Material.values()) {
            if (material.name().endsWith("_PLATE")) {
                FREELY_USABLE.add(material);
            } else if (material.name().endsWith("_BUTTON")) {
                FREELY_USABLE.add(material);
            } else if (material.name().endsWith("_DOOR")) {
                FREELY_USABLE.add(material);
            } else if (material.name().endsWith("_FENCE_GATE")) {
                FREELY_USABLE.add(material);
            }
        }
        FREELY_USABLE.add(Material.TRIPWIRE);
        FREELY_USABLE.add(Material.LEVER);
        FREELY_USABLE.add(Material.TRAP_DOOR);
        FREELY_USABLE.add(Material.IRON_TRAPDOOR);
        FREELY_USABLE.add(Material.WORKBENCH);
        FREELY_USABLE.add(Material.ENCHANTMENT_TABLE);
    }

    public OwnershipListener(ClaimCache claimCache) {
        this.claimCache = claimCache;
    }

    private boolean mayModify(Player player, Location target) {
        if (player.isOp()) {
            return true;
        }
        ClaimEntry entry = claimCache.getIfPresent(new WorldVector3i(target.getWorld().getName(), target.getChunk().getX(), 0, target.getChunk().getZ()));
        if (entry == null) {
            ChatUtil.error(player, "Chunk ownership information for this chunk is not yet available.");
            return false;
        } else if (entry.getClaim() != null) {
            Claim claim = entry.getClaim();
            Party party = entry.getParty();
            if (claim.getOwner().getUuid().equals(player.getUniqueId())) {
                return true;
            }
            if (party != null && party.getMembers().contains(new Member(new MojangId(player.getUniqueId(), player.getName()), Rank.MEMBER))) {
                return true;
            }
            ChatUtil.msg(player, ChatColor.RED, "Hey! ", ChatColor.GRAY, "This area is owned by " + claim.getOwner().getName() + ".");
            return false;
        } else {
            return true;
        }
    }

    @EventHandler
    public void onPlaceBlock(PlaceBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> mayModify(player, target), true);
        }
    }

    @EventHandler
    public void onBreakBlock(final BreakBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> mayModify(player, target), true);
        }
    }

    @EventHandler
    public void onUseBlock(final UseBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> FREELY_USABLE.contains(target.getBlock().getType()) || mayModify(player, target), true);
        }
    }

    @EventHandler
    public void onDestroyEntityEvent(final DestroyEntityEvent event) {
        if (!Entities.isNPC(event.getEntity()) && !Entities.isNonHostile(event.getEntity())) {
            return;
        }

        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> mayModify(player, target), true);
        }
    }

    @EventHandler
    public void onUseEntity(final UseEntityEvent event) {
        if (!Entities.isNPC(event.getEntity()) && !Entities.isNonHostile(event.getEntity())) {
            return;
        }

        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> mayModify(player, target), true);
        }
    }

    @EventHandler
    public void onDamageEntity(final DamageEntityEvent event) {
        if (!Entities.isNPC(event.getEntity()) && !Entities.isNonHostile(event.getEntity())) {
            return;
        }

        Player player = event.getCause().getFirstPlayer();

        if (player != null) {
            event.filter(target -> mayModify(player, target), true);
        }
    }
}
