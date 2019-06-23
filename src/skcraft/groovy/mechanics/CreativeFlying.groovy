package mechanics
import com.google.common.collect.Maps
import com.sk89q.rebar.Rebar
import com.sk89q.worldguard.bukkit.util.Locations
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class CreativeFlying extends AbstractCapsule implements Listener {

    private final Map<String, Block> activated = Maps.newHashMap();
    private static final int DISTANCE_FACTOR = 60;
    private static final int MAX_DISTANCE = DISTANCE_FACTOR * 4;

    @Override
    void preBind() {
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);

        Runnable task = new DurabilityTask();
        final int index = Rebar.getInstance().registerInterval(task, 20, 20);
        guard.add(new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(index);
            }
        });

        System.out.println("Creative Flying r4 activated!");
    }

    public static boolean isTypeIn(Block from, Block to, Material type) {
        World world = from.getWorld();
        for (int x = from.getX(); x <= to.getX(); x++) {
            for (int z = from.getZ(); z <= to.getZ(); z++) {
                if (world.getBlockAt(x, from.getY(), z).getType() != type) {
                    return false;
                }
            }
        }

        return true;
    }

    public static int getBeaconLevel(Block beacon) {
        if (!isTypeIn(beacon.getRelative(-1, -1, -1), beacon.getRelative(1, -1, 1), Material.DIAMOND_BLOCK)) {
            return 0;
        }
        if (!isTypeIn(beacon.getRelative(-2, -2, -2), beacon.getRelative(2, -2, 2), Material.DIAMOND_BLOCK)) {
            return 1;
        }
        if (!isTypeIn(beacon.getRelative(-3, -3, -3), beacon.getRelative(3, -3, 3), Material.DIAMOND_BLOCK)) {
            return 2;
        }
        if (!isTypeIn(beacon.getRelative(-4, -4, -4), beacon.getRelative(4, -4, 4), Material.DIAMOND_BLOCK)) {
            return 3;
        }
        return 4;
    }

    public static int getMaxDurability(ItemStack item) {
        if (item == null) {
            return -1;
        }

        switch (item.getType()) {
            case Material.LEATHER_BOOTS:
                return 66;
            case Material.GOLD_BOOTS:
                return 92;
            case Material.CHAINMAIL_BOOTS:
            case Material.IRON_BOOTS:
                return 196;
            case Material.DIAMOND_BOOTS:
                return 430;
            default:
                return -1;
        }
    }

    public void activate(Player player, Block block) {
        activated.put(player.getName(), block);
        update(player, block, player.getLocation());
    }

    public boolean isValid(Player player, Block beacon, Location to) {
        if (!player.getWorld().equals(beacon.getWorld())) {
            return false;
        }

        if (to.getY() > 270) {
            return false;
        }

        if (to.getY() < -10) {
            return false;
        }

        Location beaconLocation = beacon.getLocation().clone();
        beaconLocation.setY(0);

        Location testLocation = to.clone();
        testLocation.setY(0);

        double distSq = beaconLocation.distanceSquared(testLocation);

        if (distSq > MAX_DISTANCE * MAX_DISTANCE) {
            return false;
        }

        int beaconLevel = getBeaconLevel(beacon);

        if (beaconLevel == 0) {
            return false;
        }

        int i = beaconLevel * DISTANCE_FACTOR;
        if (distSq > i * i) {
            return false;
        }

        return true;
    }

    public void giveFallProtection(Player player) {
        ItemStack stack = player.getInventory().getBoots();
        if (player.isFlying()) {
            if (stack != null && stack.getEnchantmentLevel(Enchantment.PROTECTION_FALL) != 4) {
                stack.addEnchantment(Enchantment.PROTECTION_FALL, 4);
                player.getInventory().setBoots(stack);
            }
        }
    }

    public void update(Player player, Block beacon, Location to)  {
        ItemStack stack = player.getInventory().getBoots();
        int maxDurability = getMaxDurability(stack);

        if (stack == null || stack.getType() == Material.AIR || maxDurability == -1) {
            giveFallProtection(player);

            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
            }

            return;
        }


        if (stack.getDurability() >= maxDurability || !isValid(player, beacon, to)) {
            if (player.getAllowFlight()) {
                giveFallProtection(player);

                player.setAllowFlight(false);
            }
        } else {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
        }
    }

    @EventHandler
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block beacon = activated.get(player.getName());
        if (Locations.isDifferentBlock(event.getFrom(), to)) {
            if (beacon != null) {
                update(player, beacon, to);
            } else {
                if (player.getAllowFlight()) {
                    player.setAllowFlight(false);
                }
            }
        }
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block beacon = activated.get(player.getName());
        if (beacon != null) {
            update(player, beacon, player.getLocation());
        } else {
            giveFallProtection(player);

            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BEACON) {
            activate(player, event.getClickedBlock());
            player.sendMessage(ChatColor.YELLOW.toString() + "Beacon level: " + getBeaconLevel(event.getClickedBlock()));
            player.sendMessage(ChatColor.YELLOW.toString() + "Flying jet boots activated within range of this beacon! You must be wearing boots to enter flying mode. Double tap jump to start flying.");
        }
    }

    public class DurabilityTask implements Runnable {
        private final Random random = new Random();

        @Override
        void run() {
            for (Map.Entry<String, Block> entry : activated.entrySet()) {
                if (random.nextDouble() <= 0.015555) {
                    Player player = Bukkit.getPlayerExact(entry.getKey());

                    if (player != null && player.getGameMode() == GameMode.SURVIVAL && player.isFlying()) {
                        ItemStack stack = player.getInventory().getBoots();
                        int maxDurability = getMaxDurability(stack);

                        if (maxDurability != -1 && stack.getDurability() < maxDurability) {
                            stack.setDurability((short) (stack.getDurability() + 1));
                            player.getInventory().setBoots(stack);
                            update(player, entry.getValue(), player.getLocation());
                        }
                    }
                }
            }
        }
    }

}
