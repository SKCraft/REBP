/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.compat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.nijiko.permissions.PermissionHandler;
import com.sk89q.wepif.PermissionsProvider;
import com.sk89q.rebar.Rebar;

public class WEPIFPermissionHandler extends PermissionHandler {
    
    private PermissionsProvider provider;
    
    public WEPIFPermissionHandler(PermissionsProvider provider) {
        this.provider = provider;
    }

    @Override
    public void setDefaultWorld(String world) {
        return;
    }

    @Override
    public boolean checkWorld(String world) {
        return true;
    }

    @Override
    public boolean loadWorld(String world) throws Exception {
        return true;
    }

    @Override
    public void forceLoadWorld(String world) throws Exception {
        return;
    }

    @Override
    public Set<String> getWorlds() {
        Set<String> worldNames = new HashSet<String>();
        
        for (World world : Rebar.server().getWorlds()) {
            worldNames.add(world.getName());
        }
        
        return worldNames;
    }

    @Override
    public void load() throws Exception {
    }

    @Override
    public void reload() {
    }

    @Override
    public boolean reload(String world) {
        return true;
    }

    @Override
    public void saveAll() {
    }

    @Override
    public void save(String world) {
    }

    @Override
    public void closeAll() {
    }

    @Override
    public boolean has(Player player, String node) {
        return provider.hasPermission(player.getName(), node);
    }

    @Override
    public boolean has(String worldName, String playerName, String node) {
        return provider.hasPermission(playerName, node);
    }

    @Override
    public boolean permission(Player player, String node) {
        return provider.hasPermission(player.getName(), node);
    }

    @Override
    public boolean permission(String worldName, String playerName, String node) {
        return provider.hasPermission(playerName, node);
    }

    @Override
    public void addUserPermission(String world, String user, String node) {
    }

    @Override
    public void removeUserPermission(String world, String user, String node) {
    }

    @Override
    public void addGroupPermission(String world, String user, String node) {
    }

    @Override
    public void removeGroupPermission(String world, String user, String node) {
    }

    @Override
    public String getGroupProperName(String world, String group) {
        return "UNKNOWN";
    }

    @Override
    public String getUserPrefix(String world, String user) {
        return "";
    }

    @Override
    public String getUserSuffix(String world, String user) {
        return "";
    }

    @Override
    public String getPrimaryGroup(String world, String user) {
        return "UNKNOWN";
    }

    @Override
    public boolean canUserBuild(String world, String user) {
        return false;
    }

    @Override
    public String getGroupRawPrefix(String world, String group) {
        return "UNKNOWN";
    }

    @Override
    public String getGroupRawSuffix(String world, String group) {
        return "UNKNOWN";
    }

    @Override
    public boolean canGroupRawBuild(String world, String group) {
        return false;
    }

    @Override
    public Set<String> getTracks(String world) {
        return new HashSet<String>();
    }

    @Override
    public boolean inGroup(String world, String user, String group) {
        return provider.inGroup(user, group);
    }

    @Override
    public boolean inGroup(String world, String user, String groupWorld,
            String group) {
        return provider.inGroup(user, group);
    }

    @Override
    public boolean inSingleGroup(String world, String user, String group) {
        return provider.inGroup(user, group);
    }

    @Override
    public boolean inSingleGroup(String world, String user, String groupWorld,
            String group) {
        return provider.inGroup(user, group);
    }

    @Override
    public String[] getGroups(String world, String name) {
        return provider.getGroups(name);
    }

    @Override
    public Map<String, Set<String>> getAllGroups(String world, String name) {
        return new HashMap<String, Set<String>>();
    }

    @Override
    public int compareWeights(String firstWorld, String first,
            String secondWorld, String second) {
        return 0;
    }

    @Override
    public int compareWeights(String world, String first, String second) {
        return 0;
    }

    @Override
    public String getRawInfoString(String world, String entryName, String path,
            boolean isGroup) {
        return "";
    }

    @Override
    public Integer getRawInfoInteger(String world, String entryName,
            String path, boolean isGroup) {
        return 0;
    }

    @Override
    public Double getRawInfoDouble(String world, String entryName, String path,
            boolean isGroup) {
        return 0.0;
    }

    @Override
    public Boolean getRawInfoBoolean(String world, String entryName,
            String path, boolean isGroup) {
        return false;
    }

    @Override
    public String getInfoString(String world, String entryName, String path,
            boolean isGroup) {
        return "";
    }

    @Override
    public String getInfoString(String world, String entryName, String path,
            boolean isGroup, Comparator<String> comparator) {
        return "";
    }

    @Override
    public Integer getInfoInteger(String world, String entryName, String path,
            boolean isGroup) {
        return 0;
    }

    @Override
    public Integer getInfoInteger(String world, String entryName, String path,
            boolean isGroup, Comparator<Integer> comparator) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Double getInfoDouble(String world, String entryName, String path,
            boolean isGroup) {
        return 0.0;
    }

    @Override
    public Double getInfoDouble(String world, String entryName, String path,
            boolean isGroup, Comparator<Double> comparator) {
        return 0.0;
    }

    @Override
    public Boolean getInfoBoolean(String world, String entryName, String path,
            boolean isGroup) {
        return false;
    }

    @Override
    public Boolean getInfoBoolean(String world, String entryName, String path,
            boolean isGroup, Comparator<Boolean> comparator) {
        return false;
    }

    @Override
    public void addUserInfo(String world, String name, String path, Object data) {
    }

    @Override
    public void removeUserInfo(String world, String name, String path) {
    }

    @Override
    public void addGroupInfo(String world, String name, String path, Object data) {
    }

    @Override
    public void removeGroupInfo(String world, String name, String path) {
    }

    @Override
    public String getGroupPermissionString(String world, String group,
            String path) {
        return "";
    }

    @Override
    public int getGroupPermissionInteger(String world, String group, String path) {
        return 0;
    }

    @Override
    public boolean getGroupPermissionBoolean(String world, String group,
            String path) {
        return false;
    }

    @Override
    public double getGroupPermissionDouble(String world, String group,
            String path) {
        return 0;
    }

    @Override
    public String getUserPermissionString(String world, String group,
            String path) {
        return "";
    }

    @Override
    public int getUserPermissionInteger(String world, String group, String path) {
        return 0;
    }

    @Override
    public boolean getUserPermissionBoolean(String world, String group,
            String path) {
        return false;
    }

    @Override
    public double getUserPermissionDouble(String world, String group,
            String path) {
        return 0;
    }

    @Override
    public String getPermissionString(String world, String group, String path) {
        return "";
    }

    @Override
    public int getPermissionInteger(String world, String group, String path) {
        return 0;
    }

    @Override
    public boolean getPermissionBoolean(String world, String group, String path) {
        return false;
    }

    @Override
    public double getPermissionDouble(String world, String group, String path) {
        return 0;
    }

    @Override
    public String getGroup(String world, String group) {
        return "";
    }

    @Override
    public String getGroupPrefix(String world, String group) {
        return "";
    }

    @Override
    public String getGroupSuffix(String world, String group) {
        return "";
    }

    @Override
    public boolean canGroupBuild(String world, String group) {
        return false;
    }

}
