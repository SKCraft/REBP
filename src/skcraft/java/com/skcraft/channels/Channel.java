/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;

class Channel {

    private ChannelManager manager;
    private String id;
    private Set<String> members = new HashSet<String>();
    
    public Channel(ChannelManager manager, String id) {
        this.manager = manager;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Set<String> getMembersList() {
        return members;
    }

    public List<Player> getPlayersList() {
        List<Player> players = new ArrayList<Player>();
        
        for (String name : members) {
            Player player = Rebar.server().getPlayer(name);
            if (player != null) {
                players.add(player);
            }
        }
        
        return players;
    }
    
    public void add(String name) {
        for (Player player : getPlayersList()) {
            ChatUtil.msg(player, ChatColor.YELLOW + "~ " + name + " has joined #" + id + "");
        }
        
        members.add(name);
    }

    public void remove(String name) {
        members.remove(name);

        for (Player player : getPlayersList()) {
            ChatUtil.msg(player, ChatColor.YELLOW + "~ " + name + " has left #" + id + "");
        }
        
        if (members.size() == 0) {
            manager.remove(this);
        }
    }
    
}
