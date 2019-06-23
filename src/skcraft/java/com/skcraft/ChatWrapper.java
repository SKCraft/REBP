/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.helpers.InjectComponent;
import com.skcraft.util.URLShortener;

public class ChatWrapper extends AbstractComponent {

    public static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 55;
    @InjectComponent
    private URLShortener shortener;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    public class Listener implements org.bukkit.event.Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            if (event.isCancelled()) return;

            String formattedStr = String.format(event.getFormat(),
                    event.getPlayer().getDisplayName(), event.getMessage());
            String[] lines = wordWrap(formattedStr, GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH);
            for (Player recipient : event.getRecipients()) {
                recipient.sendMessage(lines);
            }
            event.getRecipients().clear();
        }
    }

    public String shortenUrl(String url) {
        return shortener.shorten(url);
    }

    private boolean isUrl(String str) {
        return str.startsWith("http://") || str.startsWith("https://");
    }

    public String[] wordWrap(String rawString, int lineLength) {
        // A null string is a single line
        if (rawString == null) {
            return new String[] {""};
        }

        // A string shorter than the lineWidth is a single line
        if (rawString.length() <= lineLength && !rawString.contains("\n")) {
            return new String[] {rawString};
        }

        char[] rawChars = (rawString + ' ').toCharArray(); // add a trailing space to trigger pagination
        StringBuilder word = new StringBuilder();
        StringBuilder line = new StringBuilder();
        List<String> lines = new LinkedList<String>();
        int lineColorChars = 0;

        for (int i = 0; i < rawChars.length; i++) {
            char c = rawChars[i];

            // skip chat color modifiers
            if (c == ChatColor.COLOR_CHAR) {
                word.append(ChatColor.getByChar(rawChars[i + 1]));
                lineColorChars += 2;
                i++; // Eat the next character as we have already processed it
                continue;
            }

            if (c == ' ' || c == '\n') {
                if (line.length() == 0 && word.length() > lineLength) { // special case: extremely long word begins a line
                    String wordStr = word.toString();
                    if (isUrl(wordStr)) {
                        line.append(shortenUrl(wordStr));
                    } else {
                        for (String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
                            lines.add(partialWord);
                        }
                    }
                } else if (line.length() + word.length() - lineColorChars == lineLength) { // Line exactly the correct length...newline
                    line.append(' ');
                    line.append(word);
                    lines.add(line.toString());
                    line = new StringBuilder();
                    lineColorChars = 0;
                } else if (line.length() + 1 + word.length() - lineColorChars > lineLength) { // Line too long...break the line
                    String wordStr = word.toString();
                    if (word.length() > lineLength && isUrl(wordStr)) {
                        String shortened = shortenUrl(wordStr);

                        if (line.length() + 1 + shortened.length() - lineColorChars > lineLength) {
                            lines.add(line.toString());
                            line = new StringBuilder(shortened);
                            lineColorChars = 0;
                        } else {
                            if (line.length() > 0) {
                                line.append(' ');
                            }
                            line.append(shortened);
                        }
                    } else {
                        for (String partialWord : wordStr.split("(?<=\\G.{" + lineLength + "})")) {
                            lines.add(line.toString());
                            line = new StringBuilder(partialWord);
                        }
                        lineColorChars = 0;
                    }
                } else {
                    if (line.length() > 0) {
                        line.append(' ');
                    }
                    line.append(word);
                }
                word = new StringBuilder();

                if (c == '\n') { // Newline forces the line to flush
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            } else {
                word.append(c);
            }
        }

        if(line.length() > 0) { // Only add the last line if there is anything to add
            lines.add(line.toString());
        }

        // Iterate over the wrapped lines, applying the last color from one line to the beginning of the next
        if (lines.get(0).length() == 0 || lines.get(0).charAt(0) != ChatColor.COLOR_CHAR) {
            lines.set(0, ChatColor.WHITE + lines.get(0));
        }
        for (int i = 1; i < lines.size(); i++) {
            final String pLine = lines.get(i-1);
            final String subLine = lines.get(i);

            char color = pLine.charAt(pLine.lastIndexOf(ChatColor.COLOR_CHAR) + 1);
            if (subLine.length() == 0 || subLine.charAt(0) != ChatColor.COLOR_CHAR) {
                lines.set(i, ChatColor.getByChar(color) + subLine);
            }
        }

        return lines.toArray(new String[lines.size()]);
    }

}
