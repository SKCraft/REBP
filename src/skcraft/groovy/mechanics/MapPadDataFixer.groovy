/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class MapPadDataFixer extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getItemInHand();

        if (heldItem.getType() == Material.MAP && heldItem.getDurability() == 20) {
            ItemMeta meta = heldItem.getItemMeta();
            if (!meta.getDisplayName().equals("MapPad")) {
                meta.setDisplayName("MapPad");
                heldItem.setItemMeta(meta);
            }
        }
    }

}
