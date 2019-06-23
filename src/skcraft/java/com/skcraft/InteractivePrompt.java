/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.helpers.InjectComponent;

public class InteractivePrompt extends AbstractComponent {

    @InjectComponent private Sessions sessions;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());
    }

    @Override
    public void shutdown() {
    }

    public synchronized boolean hasPrompt(Player player) {
        PromptSession session = sessions.get(player, PromptSession.class);
        return session.getPrompt() != null;
    }

    public synchronized void prompt(Player player, Prompt prompt) {
        PromptSession session = sessions.get(player, PromptSession.class);
        session.setPrompt(prompt);
        prompt.start(player);
    }

    public class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            if (event.isCancelled()) return;

            Player player = event.getPlayer();
            PromptSession session = sessions.get(player, PromptSession.class);
            Prompt prompt = session.getPrompt();
            if (prompt != null) {
                try {
                    prompt.accept(player, event.getMessage().trim());
                } catch (PromptComplete e) {
                    session.setPrompt(null);
                }
                event.setCancelled(true);
                return;
            }
        }
    }

    public static class PromptSession {
        private Prompt prompt;

        public Prompt getPrompt() {
            return prompt;
        }

        public void setPrompt(Prompt prompt) {
            this.prompt = prompt;
        }
    }

}
