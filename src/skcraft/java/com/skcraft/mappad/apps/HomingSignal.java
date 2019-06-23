/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.apps;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.CompoundInventory;
import com.sk89q.rebar.util.MapCanvasUtil;
import com.skcraft.mappad.AbstractApplication;
import com.skcraft.mappad.ImageResource;
import com.skcraft.mappad.MapPad;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import java.util.HashMap;
import java.util.Map;

public class HomingSignal extends AbstractApplication {

    private final int HOMING_MAX = 10;
    private final int IMG_HOMING_X = 9;
    private final int IMG_HOMING_Y = 67;

    private static ImageResource bg = new ImageResource("/resources/mappad_homingsignal.png");
    private static Map<String, String> authorized = new HashMap<String, String>();

    private long lastDraw = 0;
    private boolean hasDrawnInitial = false;

    public HomingSignal(MapPad mapPad, Player player) {
        super(mapPad, player);
    }

    @Override
    public void draw(MapCanvas canvas) {
        if (!hasDrawnInitial) {
            MapCanvasUtil.clear(canvas, (byte) 34);
            bg.draw(canvas, 0, 0);
            drawTitle(canvas, "Homing Signal");
            hasDrawnInitial = true;

            MapCanvasUtil.drawText(canvas, 5, 25, "Idle (see >help)", MapPalette.DARK_GRAY);
        }

        long now = System.currentTimeMillis();

        if (now - lastDraw < 250) {
            return;
        }

        lastDraw = now;

    }

    @Override
    public void accept(CommandContext context) throws CommandException {
        if (context.matches("help")) {
            print("Use this to teleport to other people by typing >tp <name> but the other person must first >accept <your name>. One ender pearl is required for the teleporting user.");
        } else if (context.matches("tp")) {
            if (context.argsLength() != 1) {
                throw new CommandException("Use >tp <name>");
            }
            Player target = CommandUtil.matchSinglePlayer(getPlayer(), context.getString(0));
            String otherAuthorized = authorized.get(target.getName());
            if (otherAuthorized == null || !otherAuthorized.equals(getPlayer().getName())) {
                throw new CommandException("Ask the other user to authorize you to teleport to them by having him/her use >accept <your name>.");
            }
            CompoundInventory inven = new CompoundInventory(getPlayer().getInventory());
            if (inven.getCountOf(Material.ENDER_PEARL) == 0) {
                throw new CommandException("You need one ender pearl to initiate a signal lock.");
            }
            hasDrawnInitial = false;

            if (inven.getCountOf(Material.ENDER_PEARL) == 0) {
                printError("error: You need one ender pearl to initiate a homing signal lock.");
            } else if (!otherAuthorized.equals(getPlayer().getName())) {
                printError("error: The person has de-authorized the lock and you cannot teleport to him/her.");
            } else {
                World world = getPlayer().getWorld();

                int slot = inven.first(Material.ENDER_PEARL);
                ItemStack stack = inven.getItem(slot);
                if (stack.getAmount() == 1) {
                    inven.setItem(slot, null);
                } else {
                    stack.setAmount(stack.getAmount() - 1);
                    inven.setItem(slot, stack);
                }

                authorized.remove(target.getName());

                world.playEffect(getPlayer().getLocation(), Effect.ENDER_SIGNAL, 0);

                getPlayer().teleport(target);

                world.playSound(target.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
                world.playEffect(target.getLocation(), Effect.ENDER_SIGNAL, 0);
                world.strikeLightningEffect(target.getLocation().subtract(0, 1, 0));

                print("Now teleporting...");
                target.sendMessage(ChatColor.GOLD + getPlayer().getName() + " has teleported to you.");

            }
        } else if (context.matches("accept")) {
            if (context.argsLength() != 1) {
                throw new CommandException("Use >accept <name>");
            }
            Player target = CommandUtil.matchSinglePlayer(getPlayer(), context.getString(0));
            authorized.put(getPlayer().getName(), target.getName());
            print("Player '" + target.getName() + "' has been authorized to teleport to you for ONE TIME only. Use >cancel to remove this authorization.");
        } else if (context.matches("cancel")) {
            authorized.remove(getPlayer().getName());
            print("No one can teleport to you.");
        } else {
            throw new CommandException("Unknown command! Try >help");
        }
    }

    @Override
    public void quit() {
    }
}
