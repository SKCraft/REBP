package mechanics

import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

class Bible extends AbstractCapsule implements Listener {

    private Timer timer = getGuard().add(new Timer());
    private Map<String, Long> lastUse = new HashMap<>();

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    private boolean mayUse(Player player) {
        String key = player.getName();
        Long lastTime = lastUse.get(key);
        long now = System.currentTimeMillis();
        lastUse.put(key, now);
        return lastTime == null || now - lastTime > 1000
    }

    private static void broadcastNear(Location location, String message) {
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) < 20 * 20) {
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastUse.remove(player.getName());
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack heldItem = player.getItemInHand();
            if (heldItem != null && heldItem.getType() == Material.BOOK) {
                if (mayUse(player)) {
                    broadcastNear(player.getLocation(), player.getName() + ": REPENT!!");
                }
            }
        }
    }

}
