/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.FurnaceAndDispenser;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultBoolean;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.InventoryUtil;
import com.skcraft.protection.SignProtection;
import com.sk89q.worldedit.blocks.BlockType;

public class BetterVehicles extends AbstractComponent {

    private LocalConfiguration config;
    @InjectComponent
    private SignProtection protection;

    @Override
	public void initialize() {
        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                update(entity);
            }
        }

        config = configure(new LocalConfiguration());
        Rebar.getInstance().registerEvents(new WorldListener());
        Rebar.getInstance().registerEvents(new VehicleListener());
        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @SettingBase("better-vehicles")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("anti-collision") @DefaultBoolean(true)
        public Boolean antiCollision;
        @Setting("custom-minecarts") @DefaultBoolean(true)
        public Boolean customMinecarts;
    }

    @Override
	public void shutdown() {
    }

    private void updateMinecraft(Minecart entity) {
        entity.setSlowWhenEmpty(false);
        entity.setFlyingVelocityMod(new Vector(1.05, 1, 1.05));
    }

    private void updateBoat(Boat entity) {
        entity.setMaxSpeed(0.6);
        entity.setOccupiedDeceleration(0.6);
        entity.setUnoccupiedDeceleration(0.99);
        entity.setWorkOnLand(true);
    }

    private void update(Entity entity) {
        if (entity instanceof Boat) {
            updateBoat((Boat) entity);
        } else if (entity instanceof Minecart) {
            updateMinecraft((Minecart) entity);
        }
    }

    private Sign findSign(Block track) {
        Sign sign = null;

        for (int i = -2; i >= -3; i--) {
            sign = detectSign(track.getRelative(0, i, 0));
            if (sign != null) return sign;
        }

        Block base = track.getRelative(0, -1, 0);

        sign = detectAttachedSign(base.getRelative(1, 0, 0), base);
        if (sign != null) return sign;
        sign = detectAttachedSign(base.getRelative(-1, 0, 0), base);
        if (sign != null) return sign;
        sign = detectAttachedSign(base.getRelative(0, 0, 1), base);
        if (sign != null) return sign;
        sign = detectAttachedSign(base.getRelative(0, 0, -1), base);
        if (sign != null) return sign;

        return null;
    }

    private Sign detectSign(Block block) {
        if (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
            return BlockUtil.getState(block, Sign.class);
        }

        return null;
    }

    private Sign detectAttachedSign(Block block, Block attachedTo) {
        if (block.getType() == Material.WALL_SIGN) {

            MaterialData materialData = block.getState().getData();

            if (!(materialData instanceof org.bukkit.material.Sign)) {
                return null;
            }

            // Make sure the sign is attached to the block
            if (!block.getRelative(((org.bukkit.material.Sign) materialData).getAttachedFace()).equals(attachedTo)) {
                return null;
            }

            return BlockUtil.getState(block, Sign.class);
        }

        return null;
    }

    private void transfer(Block source, StorageMinecart dest) {
        if (source.getType() != Material.CHEST) return;
        BlockState state = source.getState();
        if (!(state instanceof Chest)) return;
        Chest chest = (Chest) state;

        if (protection.canModify(source)) {
            return;
        }

        if (transfer(chest.getInventory(), dest.getInventory())) {
            chest.update();
        }
    }

    private void transfer(StorageMinecart source, Block dest) {
        if (dest.getType() != Material.CHEST) return;
        BlockState state = dest.getState();
        if (!(state instanceof Chest)) return;
        Chest chest = (Chest) state;

        if (transfer(source.getInventory(), chest.getInventory())) {
            chest.update();
        }
    }

    private boolean transfer(Inventory source, Inventory destination) {
        boolean needsUpdate = false;
        int startSlot = 0;

        for (int si = 0; si < source.getSize(); si++) {
            ItemStack sourceItem = source.getItem(si);

            if (sourceItem == null || sourceItem.getAmount() == 0) {
                continue;
            }

            int freeSlot = -1;
            boolean found = false;
            boolean hitPotentialSlot = false;

            int max = sourceItem.getMaxStackSize();
            if (max == -1) max = 1; // Safety

            for (int di = startSlot; di < destination.getSize(); di++) {
                ItemStack destItem = destination.getItem(di);

                // Found a free slot!
                if (destItem == null || destItem.getAmount() == 0) {
                    freeSlot = di;
                    hitPotentialSlot = true;
                    // But we want to wait to see if we find a partial stack
                } else if (destItem.getType() == sourceItem.getType()) {
                    int left = max - destItem.getAmount();

                    if (sourceItem.getAmount() < 0) { // Infinite stack
                        destination.setItem(di, sourceItem);
                        source.setItem(si, null);

                        needsUpdate = true;
                        found = true;
                        break;
                    } else if (destItem.getAmount() < 0) { // Infinite stack
                        source.setItem(si, null);

                        needsUpdate = true;
                        found = true;
                        break;
                    } else if (left > sourceItem.getAmount()) { // More than enough
                        destItem.setAmount(destItem.getAmount() + sourceItem.getAmount());
                        source.setItem(si, null);

                        needsUpdate = true;
                        found = true;
                        break;
                    } else if (left > 0) { // Some free space
                        sourceItem.setAmount(sourceItem.getAmount() - left);
                        destItem.setAmount(max);

                        needsUpdate = true;
                        found = true;
                        startSlot = di;
                        break;
                    }
                } else {
                    if (!hitPotentialSlot) {
                        hitPotentialSlot = destItem.getAmount() < destItem.getMaxStackSize();
                    }
                }

                if (!hitPotentialSlot) {
                    startSlot = di;
                }
            }

            // Looks like we did not find a partial stack
            if (!found) {
                if (freeSlot > -1) {
                    destination.setItem(freeSlot, sourceItem);
                    source.setItem(si, null);
                } else {
                    // Uh oh -- no free space at all!
                    break;
                }
            }
        }

        return needsUpdate;
    }

    private static void eject(Minecart minecart) {
        Entity passenger = minecart.getPassenger();
        if (passenger == null) return;
        minecart.eject();
        Block block = minecart.getLocation().getBlock();
        if (moveToIfFree(passenger, block.getRelative(1, 0, 0))) return;
        if (moveToIfFree(passenger, block.getRelative(-1, 0, 0))) return;
        if (moveToIfFree(passenger, block.getRelative(0, 0, 1))) return;
        if (moveToIfFree(passenger, block.getRelative(0, 0, -1))) return;
        if (moveToIfFree(passenger, block.getRelative(1, 0, 1))) return;
        if (moveToIfFree(passenger, block.getRelative(1, 0, -1))) return;
        if (moveToIfFree(passenger, block.getRelative(-1, 0, 1))) return;
        if (moveToIfFree(passenger, block.getRelative(-1, 0, -1))) return;
        passenger.teleport(block.getLocation());
    }

    private static boolean moveToIfFree(Entity entity, Block block) {
        Block below = block.getRelative(0, -1, 0);
        Block above = block.getRelative(0, 1, 0);
        if (!BlockType.canPassThrough(below.getTypeId())
                && BlockType.canPassThrough(block.getTypeId())
                && block.getType() != Material.RAILS
                && BlockType.canPassThrough(above.getTypeId())) {
            entity.teleport(block.getLocation().add(0.5, 0, 0.5));
            return true;
        }

        return false;
    }

    private static boolean isPlate(Material mat) {
        return mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE;
    }

    private static boolean isRail(Material mat) {
        return mat == Material.RAILS || mat == Material.POWERED_RAIL
                || mat == Material.DETECTOR_RAIL;
    }

    private static Minecart spawnMinecart(Material mat, Location loc) {
        return mat == Material.STORAGE_MINECART
                ? loc.getWorld().spawn(loc, StorageMinecart.class)
                : loc.getWorld().spawn(loc, Minecart.class);
    }

    public class WorldListener implements Listener {
        @EventHandler
        public void onWorldLoad(WorldLoadEvent event) {
            for (Entity entity : event.getWorld().getEntities()) {
                update(entity);
            }
        }
    }

    public class VehicleListener implements Listener {
        @EventHandler
        public void onVehicleCreate(VehicleCreateEvent event) {
            Vehicle vehicle = event.getVehicle();
            update(vehicle);
        }

        @EventHandler
        public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
            Vehicle vehicle = event.getVehicle();
            Entity collider = event.getEntity();

            if (!config.antiCollision) {
                return;
            }

            if (vehicle instanceof Minecart) {
                if (collider instanceof Player) {
                    Player player = (Player) collider;
                    Material mat = player.getLocation().getBlock().getType();
                    if (mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE) {
                        event.setCollisionCancelled(true);
                    }
                } else {
                    if (collider instanceof LivingEntity) {
                        if ((vehicle instanceof StorageMinecart
                                || vehicle instanceof PoweredMinecart
                                || vehicle.isEmpty())
                                && vehicle.getVelocity().lengthSquared() >= 0.09) {
                            collider.setVelocity(vehicle.getVelocity().multiply(1.5).add(new Vector(0, 0.6, 0)));
                            ((LivingEntity) collider).damage(5);
                        }
                    }
                    event.setCollisionCancelled(true);
                }
            }

            if (vehicle instanceof Minecart && collider instanceof Minecart) {
                if (collider.getVelocity().lengthSquared() > vehicle.getVelocity().lengthSquared()) {
                    if (!(vehicle instanceof StorageMinecart) && !(vehicle instanceof PoweredMinecart)
                            && vehicle.isEmpty()) {
                        vehicle.remove();
                    } else if (!(vehicle instanceof StorageMinecart || collider instanceof StorageMinecart)) {
                        vehicle.setVelocity(collider.getVelocity());
                    }
                } else {
                    if (!(vehicle instanceof StorageMinecart) && !(vehicle instanceof PoweredMinecart)
                            && !(collider instanceof StorageMinecart) && collider.isEmpty()) {
                        collider.remove();
                    } else if (!(vehicle instanceof StorageMinecart || collider instanceof StorageMinecart)) {
                        collider.setVelocity(vehicle.getVelocity());
                    }
                }
            }
        }

        @EventHandler
        public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
            Vehicle vehicle = event.getVehicle();

            if (!config.antiCollision) {
                return;
            }

            if (vehicle instanceof Minecart
                    && !(vehicle instanceof StorageMinecart)
                    && !(vehicle instanceof PoweredMinecart)
                    && event.getBlock().getRelative(0, 1, 0).getType() == Material.DISPENSER) {

                BlockState state = event.getBlock().getRelative(0, 1, 0).getState();

                if (state instanceof Dispenser) {
                    Dispenser dispenser = (Dispenser) state;
                    dispenser.getInventory().addItem(new ItemStack(Material.MINECART));
                    vehicle.remove();
                }
            }
        }

        @EventHandler
        public void onVehicleMove(VehicleMoveEvent event) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Vehicle vehicle = event.getVehicle();

            if (vehicle instanceof Minecart) {
                if (!config.customMinecarts) {
                    return;
                }

                Minecart minecart = (Minecart) vehicle;

                if (from.getBlockX() != to.getBlockX()
                        || from.getBlockY() != to.getBlockY()
                        || from.getBlockZ() != to.getBlockZ()) {

                    Block block = to.getBlock();
                    Block below = block.getRelative(0, -1, 0);
                    Vector vel = minecart.getVelocity();
                    Entity passenger = event.getVehicle().getPassenger();

                    if (below.getType() == Material.GRAVEL) {
                        minecart.setVelocity(vel.normalize().multiply(0.2));
                    } else if (below.getType() == Material.SANDSTONE) {
                        minecart.setVelocity(vel.normalize().multiply(0.2));
                    } else if (below.getType() == Material.SOUL_SAND) {
                        minecart.setVelocity(vel.normalize().multiply(0.1));
                    } else if (below.getType() == Material.NETHERRACK) {
                        minecart.setVelocity(vel.normalize().multiply(1));
                    } else if (below.getType() == Material.IRON_BLOCK
                            || below.getType() == Material.OBSIDIAN) {
                        eject(minecart);
                    } else if (below.getType() == Material.LAPIS_BLOCK) {
                        Location newLoc = vehicle.getLocation();
                        newLoc.setY(newLoc.getY() + 1.5);
                        vehicle.teleport(newLoc);
                        minecart.setVelocity(vel.add(new Vector(0, 0.5, 0)));
                    }

                    if (passenger == null && isRail(block.getType())
                            && !(vehicle instanceof StorageMinecart)
                            && !(vehicle instanceof PoweredMinecart)) {
                        boolean hasAdjacentPlate =
                            isPlate(block.getRelative(BlockFace.NORTH).getType())
                            || isPlate(block.getRelative(BlockFace.SOUTH).getType())
                            || isPlate(block.getRelative(BlockFace.WEST).getType())
                            || isPlate(block.getRelative(BlockFace.EAST).getType());

                        if (hasAdjacentPlate) {
                            for (Player player : block.getWorld().getPlayers()) {
                                Block playerBlock = player.getLocation().getBlock();
                                Location loc = player.getLocation();

                                if (isPlate(playerBlock.getType())) {
                                    double distSq = Math.pow(loc.getBlockX() - to.getBlockX(), 2)
                                            + Math.pow(loc.getBlockY() - to.getBlockY(), 2)
                                            + Math.pow(loc.getBlockZ() - to.getBlockZ(), 2);

                                    if (distSq <= 1) {
                                        minecart.setPassenger(player);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (minecart instanceof StorageMinecart) {
                        transfer(to.getBlock().getRelative(1, 0, 0), (StorageMinecart) minecart);
                        transfer(to.getBlock().getRelative(-1, 0, 0), (StorageMinecart) minecart);
                        transfer(to.getBlock().getRelative(0, 0, -1), (StorageMinecart) minecart);
                        transfer(to.getBlock().getRelative(0, 0, 1), (StorageMinecart) minecart);
                        transfer((StorageMinecart) minecart, to.getBlock().getRelative(0, -1, 1));
                        transfer((StorageMinecart) minecart, to.getBlock().getRelative(1, -1, 0));
                        transfer((StorageMinecart) minecart, to.getBlock().getRelative(-1, -1, 0));
                        transfer((StorageMinecart) minecart, to.getBlock().getRelative(0, -1, -1));
                    }

                    /*ringNoteBlock(to.getBlock().getRelative(1, -1, 0));
                    ringNoteBlock(to.getBlock().getRelative(-1, -1, 0));
                    ringNoteBlock(to.getBlock().getRelative(0, -1, 1));
                    ringNoteBlock(to.getBlock().getRelative(0, -1, -1));*/

                    Sign sign;

                    if (passenger != null && passenger instanceof Player
                            && (sign = findSign(to.getBlock())) != null) {
                        if (sign.getLine(0).equalsIgnoreCase("[Print]")) {
                            String text = sign.getLine(1)
                                    + sign.getLine(2)
                                    + sign.getLine(3);

                            ((Player) passenger).sendMessage(ChatColor.GOLD + " ** " + text);
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onVehicleDamage(VehicleDamageEvent event) {
            Vehicle vehicle = event.getVehicle();
            Entity attacker = event.getAttacker();

            if (!vehicle.isEmpty() && vehicle.getPassenger().equals(attacker)) {
                if (vehicle.getVelocity().length() < 0.2) {
                    vehicle.setVelocity(vehicle.getVelocity().normalize().multiply(0.2));
                }
            }

            if (attacker instanceof Arrow) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onVehicleExit(VehicleExitEvent event) {
            Vehicle vehicle = event.getVehicle();

            if (vehicle instanceof Minecart) {
                if (!config.customMinecarts) {
                    return;
                }

                Minecart minecart = (Minecart) vehicle;

                Block block = minecart.getLocation().getBlock();
                Block below = block.getRelative(0, -1, 0);

                if (below.getType() == Material.SANDSTONE) {
                    LivingEntity entity = event.getExited();

                    if (entity instanceof Player) {
                        ((Player) entity).sendMessage(ChatColor.GOLD + " ** You've exited at this stop");
                    }

                    vehicle.remove();
                }
            }
        }

        @EventHandler
        public void onVehicleDestroy(VehicleDestroyEvent event) {
            Vehicle vehicle = event.getVehicle();

            if (vehicle instanceof Boat) {
                if (event.getAttacker() == null) {
                    event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                    vehicle.remove();
                    vehicle.getWorld().dropItem(vehicle.getLocation(), new ItemStack(Material.BOAT, 1));
                }
                return;
            }
        }
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onBlockDispense(BlockDispenseEvent event) {
            if (event.getItem().getType() == Material.MINECART
                    || event.getItem().getType() == Material.STORAGE_MINECART) {
                if (!config.customMinecarts) {
                    return;
                }

                FurnaceAndDispenser dispenser = new FurnaceAndDispenser(Material.DISPENSER, event.getBlock().getData());
                BlockFace face = dispenser.getFacing();
                Block facing = event.getBlock().getRelative(face);
                Block aboveTwo = event.getBlock().getRelative(0, 2, 0);

                if (facing.getType() == Material.RAILS) {
                    Minecart minecart = spawnMinecart(event.getItem().getType(), facing.getLocation());
                    minecart.setVelocity((new Vector(face.getModX(), face.getModY(), face.getModZ())).multiply(2));
                    event.setCancelled(true);
                    return;
                } else if (aboveTwo.getType() == Material.RAILS) {
                    Minecart minecart = spawnMinecart(event.getItem().getType(), aboveTwo.getLocation());
                    minecart.setVelocity((new Vector(face.getModX(), face.getModY(), face.getModZ())).multiply(2));
                    event.setCancelled(true);
                }
            }
        }

        /*@EventHandler
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
            Block block = event.getBlock();
            int oldCurrent = event.getOldCurrent();
            int newCurrent = event.getNewCurrent();

            if (block.getType() == Material.DETECTOR_RAIL && oldCurrent == 0 && newCurrent > 0) {
                if (!config.customMinecarts) {
                    return;
                }

                Sign sign = findSign(block);
                if (sign != null && sign.getLine(0).equalsIgnoreCase("[Filter]")) {

                    for (Entity entity : event.getTriggers()) {
                        if (!(entity instanceof Minecart)) continue;

                        if (!satisfiesFilter(sign.getLines(), (Minecart) entity)) {
                            event.setNewCurrent(0);
                            return;
                        }
                    }
                }
            }
        }*/

        @EventHandler
        public void onSignChange(SignChangeEvent event) {
            if (event.isCancelled()) return;

            if (event.getLine(0).equalsIgnoreCase("[Print]")) {
                event.setLine(0, "[Print]");
                ChatUtil.msg(event.getPlayer(), ChatColor.GOLD, "Message printer created!");
            } else if (event.getLine(0).equalsIgnoreCase("[Filter]")) {
                event.setLine(0, "[Filter]");
                ChatUtil.msg(event.getPlayer(), ChatColor.GOLD, "Filter created!");
            }
        }
    }

    protected boolean satisfiesFilter(String[] lines, Minecart minecart) {
        for (int i = 1; i < lines.length; i++) {
            String[] words = lines[i].split(" ");
            for (String word : words) {
                if (word.equalsIgnoreCase("storage") && !(minecart instanceof StorageMinecart)) return false;
                if (word.equalsIgnoreCase("powered") && !(minecart instanceof PoweredMinecart)) return false;
                if (word.equalsIgnoreCase("minecart")
                        && (minecart instanceof PoweredMinecart || minecart instanceof StorageMinecart)) return false;
                if (word.equalsIgnoreCase("occupied") && minecart.getPassenger() == null) return false;
                if (word.equalsIgnoreCase("unoccupied") && minecart.getPassenger() != null) return false;
                if (word.equalsIgnoreCase("filled") && (!(minecart instanceof StorageMinecart)
                        || !InventoryUtil.hasItems(((StorageMinecart) minecart).getInventory()))) return false;
                if (word.equalsIgnoreCase("empty") && (minecart instanceof StorageMinecart
                        && InventoryUtil.hasItems(((StorageMinecart) minecart).getInventory()))) return false;
                if (word.equalsIgnoreCase("player")
                        && (minecart.getPassenger() == null || !(minecart.getPassenger() instanceof Player))) return false;
                if (word.equalsIgnoreCase("nonplayer")
                        && (minecart.getPassenger() == null || minecart.getPassenger() instanceof Player)) return false;
            }
        }

        return true;
    }

}
