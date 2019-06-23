/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class Emotes extends AbstractComponent {

    private static final Logger logger = Logger.getLogger(Emotes.class
            .getCanonicalName());

    private static final String PADDING = "\u2005";
    private static final Pattern emotePattern = Pattern
            .compile("\\p{InDingbats}");

    private Map<String, String> emotePatterns = new HashMap<String, String>();

    public static enum Images {
        SKCRAFT, EDUARDO1, EDUARDO2, EDUARDO3, EDUARDO4, EDUARDO5,
        CAKE, EXCLAMATION, HEART, INFO, LIGHT_BULB, ONLINE, OFFLINE, SHIELD,
        HAPPY, ROLL_EYES, SHY, REALLY_HAPPY, HIDE, THUMBS_UP,
        SECRET, GRINNING, TONGUE, SURPRISED, SLEEPING, POOP, THUMBS_DOWN,
        ASHAMED, COOL, CONFUSED, SAD, NINJA, CRYING, RELAXED, PLOTTING,
        ANGRY, WINKING, ROFL, LOL, LOVE, ALIEN, EXCITED, NERD,
        GRANDPA, WAT, SHIFTY, EMBARRASED, DEVIL, WHISTLING,
        BAZAAR, DOCKS, MARKET, INFO_BOOTH, UNIVERSITY;

        @Override
        public String toString() {
            return getEmoteChar(ordinal());
        }
    }

    private static String getEmoteChar(int index) {
        return Character.toString((char) (0x2701 + index));
    }

    public void loadEmotePatterns()
    {
        Map<String, String> patterns = new HashMap<String, String>();

        InputStream is = Emotes.class
                .getResourceAsStream("/resources/chat.xml");
        if (is != null)
        {
            try
            {
                // Unmarshal XML configuration
                JAXBContext jaxbContext = JAXBContext
                        .newInstance(ChatConfiguration.class, Emote.class, Macro.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                ChatConfiguration config = (ChatConfiguration) jaxbUnmarshaller
                        .unmarshal(is);

                // Load emotes into array
                int i = 0;
                for (Emote emote : config.getEmotesList()) {
                    for (Macro macro : emote.getMacrosList()) {
                        patterns.put(macro.getPattern(), getEmoteChar(i));
                    }
                    i++;
                }

                this.emotePatterns = patterns;

                logger.info("Rebar: Loaded " + patterns.size() + " emotes!");
            }
            catch (JAXBException e)
            {
                logger.log(Level.WARNING, "Rebar: Failed to load chat configuration.", e);
            }
        }
        else
        {
            logger.log(Level.WARNING, "Rebar: Failed to find chat configuration file.");
        }
    }

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());

        loadEmotePatterns();
    }

    @Override
    public void shutdown() {
    }

    public static String stripEmotes(String string) {
        return emotePattern.matcher(string).replaceAll("");
    }

    public String addEmotes(String message) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        String nextSpace = " ";
        int numReplaced = 0;

        for (String word : message.split(" ")) {
            String replacement = emotePatterns.get(word);
            if (numReplaced < 3 && replacement != null) {
                if (!first) {
                    builder.append(PADDING);
                }

                builder.append(replacement);
                nextSpace = PADDING;
                numReplaced++;
            } else {
                if (!first) {
                    builder.append(nextSpace);
                }

                builder.append(word);
                nextSpace = " ";
            }

            first = false;
        }
        return builder.toString();
    }

    public class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onSignChange(SignChangeEvent event) {
            for (int i = 0; i < event.getLines().length; i++) {
                event.setLine(i, stripEmotes(event.getLine(i)));
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            if (event.isCancelled()) return;

            String message = event.getMessage();

            // Scrub user-provided emote characters
            message = stripEmotes(message);

            event.setMessage(message);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            if (event.isCancelled()) return;

            String message = event.getMessage();

            // Scrub user-provided emote characters
            message = stripEmotes(message);

            // Now add emotes
            message = addEmotes(message);

            event.setMessage(message);
        }
    }

}
