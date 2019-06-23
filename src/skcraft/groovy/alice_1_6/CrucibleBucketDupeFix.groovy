/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package alice_1_6

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

class CrucibleBucketDupeFix extends AbstractCapsule implements Listener {

    private int CRUCIBLE_ID = 2408;

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
            if (heldItem != null && heldItem.getType() == Material.WATER_BUCKET && block.getTypeId() == CRUCIBLE_ID) {
                event.setCancelled(true);
                //InventoryUtil.reduceHeldItemSlot(player);
            }
        }
    }
}
