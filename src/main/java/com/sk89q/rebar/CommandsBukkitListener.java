/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.rebar.util.ChatUtil;

public class CommandsBukkitListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        try {
            Rebar.getInstance().getCommandsManager().execute(
                    event.getMessage().split(" +"), player, player);
        } catch (NumberFormatException e) {
            ChatUtil.error(player, "The command expected you to enter a number but instead you entered words.");
        } catch (CommandPermissionsException e) {
            ChatUtil.error(player, "You do not have the sufficient permission to do this.");
        } catch (MissingNestedCommandException e) {
            ChatUtil.error(player, e.getUsage());
        } catch (CommandUsageException e) {
            ChatUtil.error(player, e.getMessage());
            ChatUtil.error(player, e.getUsage());
        } catch (WrappedCommandException e) {
            ChatUtil.error(player, "An error occurred while processing the command: " + e.getMessage());
            e.printStackTrace();
        } catch (UnhandledCommandException e) {
            return;
        } catch (CommandException e) {
            ChatUtil.error(player, e.getMessage());
        }
        
        event.setCancelled(true);
    }
    
}
