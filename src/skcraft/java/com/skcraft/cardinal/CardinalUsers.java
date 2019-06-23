/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.cardinal;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.wepif.PermissionsProvider;
import com.skcraft.cardinal.event.user.RefreshUsersEvent;
import com.skcraft.cardinal.service.hive.Hive;
import com.skcraft.cardinal.service.hive.SessionRejectedException;
import com.skcraft.cardinal.service.hive.permission.Context;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.util.event.EventBus;
import com.skcraft.cardinal.util.event.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CardinalUsers extends AbstractComponent implements Listener, PermissionsProvider {

    private static final Logger logger = Logger.getLogger(CardinalUsers.class.getCanonicalName());

    private Hive hive;

    @Override
    public void initialize() {
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        cardinal.getInstance(EventBus.class).register(this);
        hive = cardinal.getInstance(Hive.class);
        Rebar.getInstance().registerEvents(this);
        Rebar.getInstance().getServiceManager().register(PermissionsProvider.class, this, (short) 0);
    }

    @Override
    public void shutdown() {

    }

    @Subscribe
    public void onUserRefresh(RefreshUsersEvent event) {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            event.getHive().get(new MojangId(player.getUniqueId(), player.getName()));
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            logger.log(Level.INFO, "Checking Cardinal status for " + event.getUniqueId());
            hive.login(new MojangId(event.getUniqueId(), event.getName()));
        } catch (SessionRejectedException e) {
            event.disallow(Result.KICK_OTHER, e.getMessage());
        } catch (Throwable t) {
            event.disallow(Result.KICK_OTHER, "It seems SKCraft is having trouble and I can't check the whitelist right now. Please try again later :(");
            logger.log(Level.SEVERE, "Could not login user", t);
        }
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getServer().getPlayerExact(name);
        if (player != null) {
            Context context = new Context.Builder().build();
            return hive.getSubject(new MojangId(player.getUniqueId(), player.getName())).hasPermission(permission, context);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        Context context = new Context.Builder().build();
        return hive.getSubject(new MojangId(player.getUniqueId(), player.getName())).hasPermission(permission, context);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(player, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return false;
    }

    @Override
    public String[] getGroups(String player) {
        return new String[0];
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return false;
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return new String[0];
    }
}
