import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class DynmapTools extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            Block block = event.getClickedBlock();
            ItemStack heldItem = player.getItemInHand();
            if (heldItem != null && heldItem.getType() == Material.RABBIT_FOOT) {
                if (Rebar.getInstance().hasPermission(player, "skcraft.dynmap.mapper")) {
                    int factor = 1;
                    if (block.getWorld().getEnvironment() == World.Environment.NETHER) {
                        factor = 8;
                    }

                    player.chat("/dmarker addcorner " + block.getX() * factor + " " + block.getY() + " " + block.getZ() * factor + " " + Bukkit.getWorlds().get(0).getName());
                }
            }
        }
    }
}
