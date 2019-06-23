/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.security;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.Unlisted;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.IOUtil;

@Unlisted
public class LockdownMode extends AbstractComponent {

    private Logger logger = createLogger(LockdownMode.class);

    private State state = State.OFF;
    private File stateFile;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(LockdownCommands.class, this);

        stateFile = new File(Rebar.getInstance().getDataFolder(), "lockdown_state.txt");

        FileInputStream in = null;
        DataInputStream dataStream = null;
        try {
            in = new FileInputStream(stateFile);
            dataStream = new DataInputStream(in);
            String stateString = dataStream.readUTF();
            state = State.valueOf(stateString);
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Could not load lockdown state from file", e);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load lockdown state from file", e);
        } finally {
            IOUtil.close(dataStream);
            IOUtil.close(in);
        }
    }

    @Override
    public void shutdown() {
    }

    public State getState() {
        return state;
    }

    public boolean isMinimum(State state) {
        return this.state.ordinal() >= state.ordinal();
    }

    public void setState(State state) {
        this.state = state;

        FileOutputStream out = null;
        DataOutputStream dataStream = null;
        try {
            out = new FileOutputStream(stateFile);
            dataStream = new DataOutputStream(out);
            dataStream.writeUTF(state.name());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not write lockdown state to file", e);
        } finally {
            IOUtil.close(dataStream);
            IOUtil.close(out);
        }
    }

    public static class LockdownCommands {

        private LockdownMode lockdownMode;

        public LockdownCommands(LockdownMode lockdownMode) {
            this.lockdownMode = lockdownMode;
        }

        @Command(aliases = {"lockdown"}, min = 1, max = 1, desc = "Set lockdown mode")
        @CommandPermissions("skcraft.lockdown")
        public void lockdown(CommandContext context, CommandSender sender) throws CommandException {
            String stateStr = context.getString(0);
            State state;

            if (stateStr.equalsIgnoreCase("off")) {
                state = State.OFF;
            } else if (stateStr.equalsIgnoreCase("mods") || stateStr.equalsIgnoreCase("moderators")) {
                state = State.SECURE_MODERATORS;
            } else if (stateStr.equalsIgnoreCase("all") || stateStr.equalsIgnoreCase("everyone")) {
                state = State.SECURE_EVERYONE;
            } else {
                ChatUtil.msg(sender, ChatColor.YELLOW, "Lockdown mode set to ",
                        ChatColor.AQUA, lockdownMode.getState().name(), ChatColor.YELLOW, ".");
                throw new CommandException("Available modes: off | mods | all");
            }

            if (sender instanceof Player) {
                if (lockdownMode.getState().ordinal() > state.ordinal()) {
                    throw new CommandException("You can only elevate the lockdown state from in-game.");
                }
            }

            lockdownMode.setState(state);

            ChatUtil.msg(sender, ChatColor.YELLOW, "Lockdown mode set to ",
                    ChatColor.AQUA, state.name(), ChatColor.YELLOW, ".");
        }
    }

    public enum State {
        OFF,
        SECURE_MODERATORS,
        SECURE_EVERYONE;
    }

}
