package mechanics

import com.sk89q.rebar.util.BlockUtil
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Cauldron

class BucketOnCauldron extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack heldItem = player.getItemInHand();
            if (heldItem != null && heldItem.getType() == Material.BUCKET && block.getType() == Material.CAULDRON) {
                Cauldron cauldron = BlockUtil.getMaterialData(block, Cauldron.class);
                if (!cauldron.isEmpty()) {
                    ItemStack newStack = new ItemStack(Material.WATER_BUCKET, heldItem.getAmount());
                    player.setItemInHand(newStack);
                }
            }
        }
    }

}
