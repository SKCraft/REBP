/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.ScoreboardProxy;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Timer;
import java.util.TimerTask;

public class Shutdown extends AbstractComponent {

    private static final int RECORD_ID = 2263;

    @InjectComponent
    private ScoreboardProxy scoreboards;
    private ShutdownProcess proc;
    private boolean colorToggle = false;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    /**
     * Decorate text to make it visible.
     *
     * @param text the text
     * @return the new message
     */
    public static String makeVisible(String text) {
        return ChatColor.RED + "###### " +
                ChatColor.YELLOW + text +
                ChatColor.RED + " ######";
    }

    /**
     * Create a shutdown scoreboard and show it to users.
     *
     * @param timeLeft the time left in ms
     * @param clientUpdateRequired true if a client update is required
     * @return a scoreboard
     */
    private Scoreboard createScoreboard(long timeLeft, boolean clientUpdateRequired) {
        Score score;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("_shutdown", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("" + ChatColor.UNDERLINE + "Warning");

        score = objective.getScore("Shutting down in");
        score.setScore((int) (timeLeft / 1000));

        if (clientUpdateRequired) {
            String color = "" + (colorToggle ? ChatColor.AQUA : ChatColor.YELLOW);
            objective.getScore(color + "Client update").setScore(0);
            objective.getScore(color + "required!").setScore(0);
            colorToggle = !colorToggle;
        } else {
            objective.getScore(ChatColor.YELLOW + "NO UPDATE").setScore(0);
            objective.getScore(ChatColor.YELLOW + "required!").setScore(0);
        }


        return board;
    }

    /**
     * Perform a shutdown.
     */
    private void performShutdown(boolean clientUpdateRequired) {
        String message = clientUpdateRequired ?
                "Please restart your game and perform a client update." :
                "Server is shutting down. If it is being " +
                "restarted, wait 30 seconds before rejoining.";
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(message);
        }

        Rebar.server().shutdown();
    }

    /**
     * Display the countdown screen.
     *
     * @param timeLeft the time left in ms
     * @param clientUpdateRequired true if a client update is required
     */
    private void displayCountdown(final long timeLeft, final boolean clientUpdateRequired) {
        Rebar.getInstance().registerTimeout(new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    scoreboards.set(player, createScoreboard(timeLeft, clientUpdateRequired), this, 10000);
                }
            }
        }, 0);
    }

    /**
     * Hide the countdown screen.
     */
    private void hideCountdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboards.set(player, null, this, 10000);
        }
    }

    /**
     * Send the countdown message.
     *
     * @param timeLeft the time left in ms
     * @param clientUpdateRequired true if a client update is required
     */
    private void sendCountdown(long timeLeft, boolean clientUpdateRequired) {
        String message;

        if (timeLeft <= 2000) {
            message = ChatColor.RED + "--- SERVER SHUTDOWN IMMINENT ---";
        } else if (timeLeft <= 10000) {
            message = ChatColor.RED + "SERVER SHUTTING DOWN IN " +
                    ChatColor.GREEN + (timeLeft / 1000) + ChatColor.RED + " SECONDS.";
        } else {
            message = ChatColor.GRAY + "(Server shutdown in " + (timeLeft / 1000) +
                    " seconds. " + (clientUpdateRequired ? "Client update REQUIRED." : "No client update.") + ")";
        }

        Rebar.server().broadcastMessage(message);
    }

    public synchronized void initiateShutdown(int delaySeconds,
            boolean clientUpdateRequired) {
        if (proc != null) {
            proc.abort();
            proc = null;
        }

        proc = new ShutdownProcess(delaySeconds, clientUpdateRequired);

        for (Player player : Rebar.server().getOnlinePlayers()) {
            player.playEffect(player.getLocation().add(0, 2, 0), Effect.RECORD_PLAY, RECORD_ID);
        }
    }

    public synchronized boolean abortShutdown() {
        if (proc != null) {
            proc.abort();
            proc = null;
            hideCountdown();

            for (Player player : Rebar.server().getOnlinePlayers()) {
                player.playEffect(player.getLocation().add(0, 2, 0), Effect.RECORD_PLAY, 0);
            }

            return true;
        }

        return false;
    }

    public static class Commands {
        private Shutdown component;

        public Commands(Shutdown component) {
            this.component = component;
        }

        @Command(aliases = {"shutdown", "stop"}, desc = "Stop the server",
                 usage = "[<time>]", flags = "c",
                 min = 0, max = 1)
        @CommandPermissions("skcraft.shutdown")
        public void stop(CommandContext context, CommandSender sender) {
            component.abortShutdown();

            if (context.argsLength() == 0) {
                component.performShutdown(context.hasFlag('c'));
            } else {
                int delay = Math.max(5, context.getInteger(0));
                Rebar.server().broadcastMessage(
                        makeVisible("Server is shutting down in " + delay
                                + " seconds."));
                component.initiateShutdown(delay, context.hasFlag('c'));
            }
        }

        @Command(aliases = {"abort"}, desc = "Abort stopping the server", min = 0, max = 0)
        @CommandPermissions("skcraft.shutdown")
        public void abort(CommandContext context, CommandSender sender) {
            if (component.abortShutdown()) {
                Rebar.server().broadcastMessage(makeVisible("Server shutdown was ABORTED."));
            } else {
                ChatUtil.error(sender, "Server was not shutting down.");
            }
        }
    }

    private class ShutdownProcess extends TimerTask {
        private Timer timer = new Timer();
        private final boolean clientUpdateRequired;
        private long shutdownTime;
        private long lastUpdate;

        public ShutdownProcess(int delaySeconds, boolean clientUpdateRequired) {
            this.clientUpdateRequired = clientUpdateRequired;
            lastUpdate = 0;
            shutdownTime = System.currentTimeMillis() + delaySeconds * 1000;
            timer.schedule(this, 0, 1000);
        }

        public void abort() {
            timer.cancel();
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long elapsedSinceUpdate = now - lastUpdate;
            final long timeLeft = shutdownTime - now;

            displayCountdown(timeLeft, clientUpdateRequired);

            if (timeLeft <= 0) {
                abort(); // Kill the timer
                Rebar.getInstance().registerTimeout(new Runnable() {
                    @Override
                    public void run() {
                        performShutdown(clientUpdateRequired);
                    }
                }, 0);
            }

            // Send a message occasionally
            if (elapsedSinceUpdate >= 1000) {
                lastUpdate = now;

                if (timeLeft <= 10000 || (timeLeft / 1000) % 5 == 0) {
                    Rebar.getInstance().registerTimeout(new Runnable() {
                        @Override
                        public void run() {
                            sendCountdown(timeLeft, clientUpdateRequired);
                        }
                    }, 0);
                }
            }
        }
    }

}
