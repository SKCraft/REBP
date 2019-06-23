/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EnderPearlHomes extends AbstractComponent implements Listener {

    private static Random random = new Random();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public void teleportToSpawn(final Player player) {
        Rebar.getInstance().registerTimeout(() -> {
            Location loc = Bukkit.getServer().getWorlds().get(0).getSpawnLocation();
            PlayerUtil.safeTeleport(player, loc);
        }, 0);
    }

    public void teleportToBed(final Player player) {
        Rebar.getInstance().registerTimeout(() -> {
            reallyTeleportToBed(player);
        }, 0);
    }


    public void reallyTeleportToBed(final Player player) {
        Rebar.getInstance().registerTimeout(() -> {
            Location bedLoc = player.getBedSpawnLocation();
            if (bedLoc == null) {
                ChatUtil.msg(player, ChatColor.GOLD, "Use a bed to set a home to able to return there when throwing an ender pearl up.");
            } else {
                ChatUtil.msg(player, ChatColor.GOLD, "To home you go!");
                final Location betterLoc = PlayerUtil.findAdjacentFreePosition(bedLoc);
                PlayerUtil.safeTeleport(player, betterLoc);
            }
        }, 0);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity ent = event.getEntity();

        if (ent instanceof Enderman) {
            for (int i = 0; i < random.nextInt(5) + 2; i++) {
                event.getDrops().add(new ItemStack(Material.ENDER_PEARL, 1));
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        TeleportCause cause = event.getCause();
        Location to = event.getTo();

        if (cause == TeleportCause.ENDER_PEARL) {
            if (to.distanceSquared(player.getLocation()) < 3) {
                event.setCancelled(true);

                if (player.getFireTicks() > 0) {
                    player.sendMessage(ChatColor.RED + "The flames interfere with the Ender transmission.");
                } else {
                    if (to.getBlock().getType() == Material.WATER || to.getBlock().getType() == Material.STATIONARY_WATER) {
                        teleportToSpawn(player);
                    } else {
                        teleportToBed(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location bedLoc = player.getBedSpawnLocation();

        if (bedLoc != null) {
            Location betterLoc = PlayerUtil.findAdjacentFreePosition(bedLoc);
            if (betterLoc != null) {
                event.setRespawnLocation(betterLoc);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getType() == Material.BED_BLOCK || event.getClickedBlock().getTypeId() == 791)) {
            player.setBedSpawnLocation(event.getClickedBlock().getLocation(), true);
            player.sendMessage(ChatColor.GOLD +
                    "You have set your home location to this bed. When you die, you will respawn here.");
        }
    }

    public static class Commands {
        public Commands(EnderPearlHomes component) {
        }

        @Command(aliases = {"sethome"}, desc = "Set your home")
        public void sethome(CommandContext context, CommandSender sender) {
            ChatUtil.msg(sender, ChatColor.YELLOW,
                    "To set a home, sleep in a bed during the day or night. (How do you craft a bed? Use /recipe bed)");
        }

        @Command(aliases = {"home"}, desc = "Go to your home")
        public void home(CommandContext context, CommandSender sender) {
            ChatUtil.msg(sender, ChatColor.YELLOW,
                    "To go to your home (set with a bed), throw an ender pearl (get those from slaying Endermen) at your feet.");
        }

    }
}
