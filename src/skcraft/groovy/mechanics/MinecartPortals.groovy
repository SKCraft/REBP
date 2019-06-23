/**
 * For SKCraft
 * (c) sk89q
 */

package mechanics

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Minecart
import org.bukkit.entity.Vehicle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleMoveEvent

import java.lang.reflect.Field
/**
 * Allows Minecarts with passengers to travel through portals without having
 * to disembark from the Minecart.
 */
class MinecartPortals extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    /**
     * Returns whether the two locations are on different blocks.
     *
     * @param a location 1
     * @param b location 2
     * @return true if the two positions are different blocks
     */
    private static boolean crossesBlockBoundary(Location a, Location b) {
        return a.getBlockX() != b.getBlockX() || a.getBlockY() != b.getBlockY() || a.getBlockZ() != b.getBlockZ();
    }

    /**
     * Returns whether the given location has a portal block.
     *
     * @param location the location
     * @return true if there is a portal block
     */
    private static boolean hasPortalBlock(Location location) {
        return location.getBlock().getType() == Material.PORTAL;
    }

    /**
     * Returns whether the given material is a rail.
     *
     * @param mat the material
     * @return true if a rail
     */
    private static boolean isRail(Material mat) {
        return mat == Material.RAILS || mat == Material.POWERED_RAIL || mat == Material.DETECTOR_RAIL || mat == Material.ACTIVATOR_RAIL;
    }

    /**
     * Find the first world with the given environment.
     *
     * @param environment the environment
     * @return the world
     */
    private static World findFirstWorld(World.Environment environment) {
        for (World world : Bukkit.getServer().getWorlds()) {
            if (world.getEnvironment() == environment) {
                return world;
            }
        }

        throw new RuntimeException("Failed to get the Nether world");
    }

    /**
     * Create the default {@link TravelAgent}.
     *
     * @return the travel agent
     */
    private static TravelAgent createTravelAgent() {
        String className = Bukkit.getServer().getClass().getCanonicalName().replace("CraftServer", "CraftTravelAgent");
        Class<?> cls = Class.forName(className);
        Field field = cls.getField("DEFAULT");
        TravelAgent agent = field.get(null) as TravelAgent;
        if (agent == null) {
            throw new RuntimeException("No travel agent is available");
        }
        return agent;
    }

    /**
     * Calculate the target location after portal usage.
     *
     * @param from the location in the origin world
     * @param toWorld the target world
     * @param factor the distance factor for the X and Z components
     * @return the new location
     */
    private static Location calculatePortalLocation(Location from, World toWorld, double factor) {
        TravelAgent agent = createTravelAgent();
        Location to = from.clone();
        to.setX(to.getX() * factor);
        to.setZ(to.getZ() * factor);
        to.setWorld(toWorld);
        return agent.findOrCreate(to);
    }

    /**
     * Calculate the target location after portal usage.
     *
     * @param from the location in the origin world
     * @return the new location to teleport to
     */
    private static Location getPortalTargetLocation(Location from) {
        World overWorld = findFirstWorld(World.Environment.NORMAL);
        World nether = findFirstWorld(World.Environment.NETHER);

        World fromWorld = from.getWorld();

        if (fromWorld.equals(overWorld)) {
            return calculatePortalLocation(from, nether, 1 / 8.0);
        } else if (fromWorld.equals(nether)) {
            return calculatePortalLocation(from, overWorld, 8.0);
        } else{
            return null;
        }
    }

    /**
     * Find an adjacent block with a rail in it.
     *
     * @param base the base location
     * @return a location, or null if none was found
     */
    private static Location findAdjacentRail(Location base) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y <= 1; y++) {
                    Location test = base.clone().add(x, y, z);
                    if (isRail(test.getBlock().getType())) {
                        return test;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Teleport a vehicle through to a given location that is supposed to
     * be after usage of a portal.
     *
     * @param vehicle the vehicle
     * @param to the target location
     */
    private static void teleportThroughPortal(Vehicle vehicle, Location to) {
        Entity passenger = vehicle.getPassenger();
        assert passenger != null;

        Location railLocation = findAdjacentRail(to);

        if (railLocation != null) {
            def direction = railLocation.toVector().subtract(to.toVector());
            def velocity = direction.multiply(5);
            vehicle.eject();
            vehicle.remove();

            passenger.teleport(railLocation);

            Rebar.getInstance().registerTimeout(new Runnable() {
                @Override
                void run() {
                    Minecart minecart = to.getWorld().spawn(railLocation.add(0.5, 0.5, 0.5), Minecart.class);
                    minecart.setPassenger(passenger);
                    minecart.setVelocity(velocity);
                    railLocation.getWorld().playSound(minecart.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
                }
            }, 2);
        }
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof Minecart && crossesBlockBoundary(from, to) && hasPortalBlock(to)) {
            if (!vehicle.isEmpty()) {
                Location portalTo = getPortalTargetLocation(from);

                if (portalTo != null) {
                    teleportThroughPortal(vehicle, portalTo);
                }
            }
        }
    }

}
