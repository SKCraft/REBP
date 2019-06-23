/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.playblock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk89q.rebar.Rebar;
import com.skcraft.cardinal.util.WorldVector3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaManager {
    private static final Logger logger = Logger.getLogger(MediaManager.class.getName());
    private final Set<UUID> viewers = new HashSet<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file;
    private PlayBlockData data = new PlayBlockData();

    public MediaManager(File file) {
        this.file = file;
    }

    public void load() {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            data = mapper.readValue(bis, PlayBlockData.class);
        } catch (FileNotFoundException e) {
            // This is normal
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read " + file.getAbsolutePath(), e);
        }
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            mapper.writeValue(file, data);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write " + file.getAbsolutePath());
        }
    }

    public Location getScreenLocation() {
        if (data.getLocation() != null && data.getCurrentMedia() != null) {
            World world = Bukkit.getWorld(data.getLocation().getWorldId());
            if (world != null) {
                return new Location(world, data.getLocation().getX(), data.getLocation().getY(), data.getLocation().getZ());
            }
        }
        return null;
    }

    public void check(Player player) {
        Location screenLocation = getScreenLocation();
        if (screenLocation != null && data.getCurrentMedia() != null) {
            boolean isViewer = viewers.contains(player.getUniqueId());
            if (screenLocation.getWorld().equals(player.getLocation().getWorld())) {
                double distanceSq = screenLocation.distanceSquared(player.getLocation());
                if (isViewer && distanceSq > data.getStopRadiusSq()) {
                    removeViewer(player);
                } else if (!isViewer && distanceSq <= data.getPlayRadiusSq()) {
                    addViewer(player);
                }
            } else {
                if (isViewer) {
                    removeViewer(player);
                }
            }
        }
    }

    public void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            check(player);
        }
    }

    public void setMedia(String uri) {
        if (uri != null) {
            data.setCurrentMedia(new Media(uri));
        } else {
            data.setCurrentMedia(null);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (viewers.contains(player.getUniqueId())) {
                sendPlayPacket(player);
            }
        }
        save(); // TODO: Background save
    }

    public void setLocation(Location location, int playRadius, int stopRadius) {
        stopRadius = Math.max(stopRadius, playRadius + 5);
        data.setLocation(new WorldVector3i(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        data.setPlayRadiusSq(playRadius * playRadius);
        data.setStopRadiusSq(stopRadius * stopRadius);
        checkAllPlayers();
        save();
    }

    public void addViewer(Player player) {
        if (!viewers.contains(player.getUniqueId())) {
            viewers.add(player.getUniqueId());
            sendPlayPacket(player);
        }
    }

    public void removeViewer(Player player) {
        if (viewers.contains(player.getUniqueId())) {
            viewers.remove(player.getUniqueId());
            sendPlayPacket(player, "");
        }
    }

    public void sendPlayPacket(Player player) {
        sendPlayPacket(player, data.getCurrentMedia() != null ? data.getCurrentMedia().getUri() : "");
    }

    private void sendPlayPacket(Player player, String uri) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeByte(2);
            dos.writeUTF(uri);
        } catch (IOException e) {
            throw new RuntimeException("This shouldn't have happened", e);
        }
        player.sendPluginMessage(Rebar.getInstance(), "PlayBlock", baos.toByteArray());
    }

}
