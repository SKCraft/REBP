package mechanics

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.util.StringUtil
import com.sk89q.worldedit.blocks.BlockType
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.entity.*
import org.bukkit.entity.minecart.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent
import org.bukkit.event.vehicle.VehicleExitEvent
import org.bukkit.event.vehicle.VehicleMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.material.Wool
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue

class BetterMinecarts extends AbstractCapsule implements Listener {

    private static final String LAST_VELOCITY_KEY = "sk_lastVelocity";
    private static final String SWAP_KEY = "sk_swap";
    private static final double ANGLE_EPSILON = 30.0 * Math.PI / 180;

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    private static org.bukkit.util.Vector getVector(BlockFace face) {
        return new org.bukkit.util.Vector(face.getModX(), face.getModY(), face.getModZ());
    }

    private static Sign findRailMessageSign(Block rail, org.bukkit.util.Vector vector) {
        Sign found = null;
        org.bukkit.util.Vector testVec = vector.clone().setY(0);
        org.bukkit.util.Vector boxVec = testVec.clone().normalize();
        int bx = Math.abs(Math.round(boxVec.getZ())); // Flipped
        int bz = Math.abs(Math.round(boxVec.getX()));

        for (int x = -bx; x <= bx; x++) {
            for (int z = -bz; z <= bz; z++) {
                for (int y = -1; y <= 2; y++) {
                    Block test = rail.getRelative(x, y, z);

                    if (test.getType() == Material.WALL_SIGN || test.getType() == Material.SIGN_POST) {
                        Sign sign = (Sign) test.getState();

                        // Check if this is the right face
                        org.bukkit.material.Sign data = sign.getData() as org.bukkit.material.Sign;
                        if (getVector(data.getFacing()).angle(testVec) < ANGLE_EPSILON) {
                            break;
                        } else if (getVector(data.getFacing().getOppositeFace()).angle(testVec) < ANGLE_EPSILON) {
                            found = sign;
                            break;
                        }
                    }
                }
            }
        }

        return found;
    }

    private static boolean isPlate(Material mat) {
        return mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE;
    }

    private static boolean isRail(Material mat) {
        return mat == Material.RAILS || mat == Material.POWERED_RAIL || mat == Material.DETECTOR_RAIL;
    }

    private static Minecart spawnMinecart(Material mat, Location loc) {
        return mat == Material.STORAGE_MINECART ? loc.getWorld().spawn(loc, StorageMinecart.class) : loc.getWorld().spawn(loc, Minecart.class);
    }

    private static void teleportNearby(Entity entity, Location baseLoc) {
        teleportNearby(entity, baseLoc, false);
    }

    private static boolean isWoolColor(Block block, DyeColor color) {
        return block.getType() == Material.WOOL && ((Wool) block.getState().getData()).getColor() == color;
    }

    private static void playSoundLater(Location location, Sound sound, float volume, float pitch, int ticks) {
        Runnable runnable = new Runnable() {
            @Override
            void run() {
                location.getWorld().playSound(location, sound, volume, pitch);
            }
        };

        if (ticks == 0) {
            runnable.run();
        } else {
            Rebar.getInstance().registerTimeout(runnable, ticks);
        }
    }

    private static void boostVelocityLater(Vehicle vehicle, double factor, int ticks) {
        Runnable runnable = new Runnable() {
            @Override
            void run() {
                vehicle.setVelocity(vehicle.getVelocity().multiply(factor));
            }
        };

        if (ticks == 0) {
            runnable.run();
        } else {
            Rebar.getInstance().registerTimeout(runnable, ticks);
        }
    }

    private static void teleportNearby(Entity entity, Location baseLoc, boolean expanded) {
        int factor = expanded ? 5 : 1;

        List<LocationChoice> choices = new ArrayList<LocationChoice>();
        for (int x = -2 * factor; x <= 2 * factor; x++) {
            for (int z = -2 * factor; z <= 2 * factor; z++) {
                for (int y = -2 * factor; y <= 2 * factor; y++) {
                    Location loc = baseLoc.clone().add(x, y, z);
                    addLocation(choices, loc, -baseLoc.distance(loc) * 1 - Math.abs(baseLoc.getY() - loc.getY()) * 2);
                }
            }
        }

        Collections.sort(choices);
        entity.teleport(choices.get(0).location.add(0, 0.5, 0));
    }

    private static void addLocation(List<LocationChoice> locations, Location loc, double baseRank) {
        locations.add(new LocationChoice(loc, rankLocation(loc.getBlock(), baseRank)));
    }

    private static int rankLocation(Block block, double baseRank) {
        double rank = baseRank;
        int id = block.getTypeId();
        int belowId = block.getRelative(0, -1, 0).getTypeId();
        int aboveId = block.getRelative(0, 1, 0).getTypeId();
        boolean solid = !BlockType.canPassThrough(id);
        boolean belowSolid = !BlockType.canPassThrough(belowId);
        boolean aboveSolid = !BlockType.canPassThrough(aboveId)

        if (block.getType() == Material.RAILS || block.getType() == Material.POWERED_RAIL ||
                block.getType() == Material.ACTIVATOR_RAIL || block.getType() == Material.DETECTOR_RAIL) {
            rank -= 20;
        }

        if (block.getType() == Material.GOLD_PLATE || block.getType() == Material.IRON_PLATE ||
                block.getType() == Material.STONE_PLATE || block.getType() == Material.WOOD_PLATE) {
            rank -= 30;
        }

        if (id != 0) {
            rank -= 1;
        }

        if (aboveSolid) {
            rank -= 100;
        }

        if (solid) {
            rank -= 10;
        }

        if (belowSolid) {
            rank += 100;
        }

        return rank;
    }

    private static class LocationChoice implements Comparable<LocationChoice> {
        public final Location location;
        public final double ranking;

        LocationChoice(Location location, double ranking) {
            this.location = location
            this.ranking = ranking
        }

        @Override
        int compareTo(LocationChoice o) {
            if (o.ranking > ranking) {
                return 1;
            } else if (o.ranking < ranking) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static org.bukkit.util.Vector getLastVelocity(Vehicle vehicle) {
        for (MetadataValue value : vehicle.getMetadata(LAST_VELOCITY_KEY)) {
            return value.value() as org.bukkit.util.Vector;
        }

        return vehicle.getVelocity();
    }

    private static void restoreVelocity(Vehicle vehicle) {
        vehicle.setVelocity(getLastVelocity(vehicle));
    }

    private static boolean shouldRoadKill(Entity entity) {
        return (entity instanceof LivingEntity) &&
                !(entity instanceof Player) && !(entity instanceof Tameable && (entity as Tameable).isTamed()) &&
                !(entity instanceof Villager);
    }

    private static boolean isRegularMinecart(Entity entity) {
        return entity instanceof Minecart &&
                !(entity instanceof StorageMinecart) &&
                !(entity instanceof PoweredMinecart) &&
                !(entity instanceof ExplosiveMinecart) &&
                !(entity instanceof SpawnerMinecart) &&
                !(entity instanceof HopperMinecart) &&
                !(entity instanceof CommandMinecart);
    }

    private static boolean hasAdjacentPlate(Block block) {
        return isPlate(block.getRelative(BlockFace.NORTH).getType()) || isPlate(block.getRelative(BlockFace.SOUTH).getType()) || isPlate(block.getRelative(BlockFace.WEST).getType()) || isPlate(block.getRelative(BlockFace.EAST).getType());
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        Entity target = event.getTarget();
        if (target instanceof Player && target.getVehicle() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        Vehicle vehicle = event.getVehicle();
        Entity other = event.getEntity();

        if (vehicle instanceof Minecart) {
            if (other instanceof Player) {
                Player player = (Player) other;
                Material mat = player.getLocation().getBlock().getType();
                if (mat == Material.STONE_PLATE || mat == Material.WOOD_PLATE) {
                    event.setCollisionCancelled(true);
                }
            }

            /*if (!vehicle.isEmpty() && isRegularMinecart(other)) {
                if (other.isEmpty() && isRegularMinecart(other)) {
                    other.remove();
                    event.setCollisionCancelled(true);
                    org.bukkit.util.Vector velocity = vehicle.getVelocity();
                    if (velocity.lengthSquared() <= 0.1) {
                        restoreVelocity(vehicle);
                    }
                }
            }*/

            if (!vehicle.isEmpty() && shouldRoadKill(other) && other.getVehicle() == null) {
                other.setVelocity(vehicle.getVelocity().clone().multiply(2).setY(0.6));
                (other as LivingEntity).damage(20);
                event.setCollisionCancelled(true);
                restoreVelocity(vehicle);
                vehicle.setVelocity(vehicle.getVelocity().multiply(1.6));
            }

            /*if (!vehicle.isEmpty() && other instanceof Player) {
                teleportNearby(other, other.getLocation(), true);
                event.setCollisionCancelled(true);
                restoreVelocity(vehicle);
            }*/

            if (isRegularMinecart(other) && isRegularMinecart(vehicle) && !vehicle.isEmpty() && !other.isEmpty() &&
                    vehicle.getPassenger() instanceof Player && other.getPassenger() instanceof Player &&
                    !vehicle.hasMetadata(SWAP_KEY) && !other.hasMetadata(SWAP_KEY)) {
                Entity p1 = vehicle.getPassenger();
                Entity p2 = other.getPassenger();
                Location loc1 = vehicle.getLocation();
                Location loc2 = other.getLocation();
                org.bukkit.util.Vector vel1 = getLastVelocity(vehicle);
                org.bukkit.util.Vector vel2 = getLastVelocity(other);
                vehicle.remove();
                other.remove();
                p1.teleport(loc2);
                p2.teleport(loc1);
                Vehicle newVehicle1 = spawnMinecart(Material.MINECART, loc2);
                Vehicle newVehicle2 = spawnMinecart(Material.MINECART, loc1);
                newVehicle1.setPassenger(p1);
                newVehicle2.setPassenger(p2);
                newVehicle1.setVelocity(vel1.multiply(1.2));
                newVehicle2.setVelocity(vel2.multiply(1.2));
                newVehicle1.setMetadata(SWAP_KEY, new FixedMetadataValue(Rebar.getInstance(), (Boolean) true));
                newVehicle2.setMetadata(SWAP_KEY, new FixedMetadataValue(Rebar.getInstance(), (Boolean) true));
            }
        }
    }

    @EventHandler
    public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
        Vehicle vehicle = event.getVehicle();
        World world = vehicle.getWorld();

        if (isRegularMinecart(vehicle) && event.getBlock().getType() == Material.DISPENSER) {
            BlockState state = event.getBlock().getState();

            if (state instanceof Dispenser) {
                Dispenser dispenser = (Dispenser) state;
                dispenser.getInventory().addItem(new ItemStack(Material.MINECART));
                if (!vehicle.isEmpty()) {
                    Entity passenger = vehicle.getPassenger();
                    vehicle.setPassenger(null);
                    Rebar.getInstance().registerTimeout(new Runnable() {
                        @Override
                        void run() {
                            teleportNearby(passenger, vehicle.getLocation());
                        }
                    }, 2);
                }
                vehicle.remove();
                world.playSound(dispenser.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1f, 1f);
            }
        }
    }

    private static void setLastVelocity(Minecart minecart) {
        if (minecart.getVelocity().lengthSquared() > 0.1) {
            minecart.setMetadata(LAST_VELOCITY_KEY, new FixedMetadataValue(Rebar.getInstance(), minecart.getVelocity()));
        }
        minecart.removeMetadata(SWAP_KEY, Rebar.getInstance());
    }

    private void onMinecartBlockMove(VehicleMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        World world = to.getWorld();
        Vehicle vehicle = event.getVehicle();
        Minecart minecart = (Minecart) vehicle;
        Block block = to.getBlock();
        Entity passenger = event.getVehicle().getPassenger();
        Block below = block.getRelative(0, -1, 0);

        setLastVelocity(minecart);

        // Plate
        if (passenger == null && isRail(block.getType()) && isRegularMinecart(vehicle) && hasAdjacentPlate(block)) {
            for (Player player : block.getWorld().getPlayers()) {
                Block playerBlock = player.getLocation().getBlock();
                Location loc = player.getLocation();

                if (isPlate(playerBlock.getType())) {
                    double distSq = Math.pow(loc.getBlockX() - to.getBlockX(), 2)
                    +Math.pow(loc.getBlockY() - to.getBlockY(), 2)
                    +Math.pow(loc.getBlockZ() - to.getBlockZ(), 2);

                    if (distSq <= 2) {
                        player.teleport(minecart.getLocation().add(0, 1, 0));
                        minecart.setPassenger(player);
                        break;
                    }
                }
            }
        }

        // Station
        if (isWoolColor(below, DyeColor.GREEN)) {
            org.bukkit.util.Vector velocity = minecart.getVelocity();

            if (velocity.lengthSquared() > 0.2 * 0.2) {
                minecart.setVelocity(velocity.multiply(0.4));
            }

            if (velocity.lengthSquared() < 0.2 * 0.2) {
                minecart.setVelocity(velocity.normalize().multiply(0.2));
            }
        } else if (isWoolColor(from.getBlock().getRelative(0, -1, 0), DyeColor.GREEN)) {
            boostVelocityLater(minecart, 1.1, 0);
            boostVelocityLater(minecart, 1.2, 10);
            boostVelocityLater(minecart, 1.2, 20);
            boostVelocityLater(minecart, 1.5, 30);
            boostVelocityLater(minecart, 2, 40);
            boostVelocityLater(minecart, 2, 60);
            boostVelocityLater(minecart, 5, 80);
        }

        // High speed
        if (below.getType() == Material.GOLD_BLOCK && minecart.getPassenger() instanceof Player) {
            if (minecart.getMaxSpeed() != 1.1) {
                minecart.setMaxSpeed(1.1);
                minecart.setDisplayBlock(new MaterialData(Material.FIRE));
                if (passenger instanceof Player) {
                    ChatUtil.msg(passenger, ChatColor.GRAY, ChatColor.ITALIC, "High speed minecart engaged");
                }
            }
        }

        // Normal speed
        if (isWoolColor(below, DyeColor.GRAY)) {
            if (minecart.getMaxSpeed() != 0.4) {
                minecart.setMaxSpeed(0.4);
                minecart.setDisplayBlock(null);
                if (passenger instanceof Player) {
                    ChatUtil.msg(passenger, ChatColor.GRAY, ChatColor.ITALIC, "Normal speed minecart engaged");
                }
            }
        }

        // Speed up
        if (isWoolColor(below, DyeColor.YELLOW)) {
            if (passenger == null && isRegularMinecart(minecart)) {
                minecart.remove();
            } else {
                if (minecart.getMaxSpeed() <= 0.4) {
                    boostVelocityLater(minecart, 1.1, 0);
                    boostVelocityLater(minecart, 1.2, 10);
                    boostVelocityLater(minecart, 1.2, 20);
                    boostVelocityLater(minecart, 1.5, 30);
                    boostVelocityLater(minecart, 2, 40);
                    boostVelocityLater(minecart, 2, 60);
                    boostVelocityLater(minecart, 5, 80);
                } else {
                    vehicle.setVelocity(vehicle.getVelocity().multiply(5));
                }
            }
        }

        // Message
        if (passenger != null && passenger instanceof Player) {
            Sign sign = findRailMessageSign(block, vehicle.getVelocity());

            if (sign != null) {
                Player player = (Player) passenger;
                Block signBlock = sign.getBlock();
                BlockFace face = (sign.getBlock().getState().getData() as org.bukkit.material.Sign).getAttachedFace();

                if (signBlock.getRelative(face).getType() == Material.GOLD_BLOCK) {
                    String[] parts = StringUtil.joinString(sign.getLines(), " ").split("\\|", 2);
                    if (parts.length == 1) {
                        player.sendTitle(parts[0].trim(), "");
                    } else {
                        player.sendTitle(parts[0].trim(), parts[1].trim());
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD.toString() + " ** " + StringUtil.joinString(sign.getLines(), " ").trim());
                }
            }
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof Minecart) {
            if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
                onMinecartBlockMove(event);
            }
        }

    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof Minecart) {
            Minecart minecart = (Minecart) vehicle;

            Block block = minecart.getLocation().getBlock();
            Block below = block.getRelative(0, -1, 0);

            if (isWoolColor(below, DyeColor.GREEN)) {
                LivingEntity entity = event.getExited();

                if (entity instanceof Player) {
                    ((Player) entity).sendMessage(ChatColor.BLUE.toString() + " ** You've exited at this stop");
                }

                vehicle.setPassenger(null);
                //teleportNearby(entity, vehicle.getLocation());
                vehicle.remove();
            }
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() == Material.MINECART || event.getItem().getType() == Material.STORAGE_MINECART) {
            Block block = event.getBlock();
            World world = block.getWorld();
            org.bukkit.material.Dispenser dispenser = (org.bukkit.material.Dispenser) block.getState().getData();
            BlockFace face = dispenser.getFacing();
            Block facing = event.getBlock().getRelative(face);
            Block aboveTwo = event.getBlock().getRelative(0, 2, 0);

            if (facing.getType() == Material.RAILS || facing.getType() == Material.POWERED_RAIL || facing.getType() == Material.DETECTOR_RAIL) {
                Minecart minecart = spawnMinecart(event.getItem().getType(), facing.getLocation().add(0.5, 0.5, 0.5));
                event.setCancelled(true);
                world.playSound(block.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
            } else if (aboveTwo.getType() == Material.RAILS) {
                Minecart minecart = spawnMinecart(event.getItem().getType(), aboveTwo.getLocation().add(0.5, 0.5, 0.5));
                minecart.setVelocity((new org.bukkit.util.Vector(face.getModX(), face.getModY(), face.getModZ())).multiply(1.1));
                event.setCancelled(true);
                world.playSound(block.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1f, 1f);
            }

        }
    }
}
