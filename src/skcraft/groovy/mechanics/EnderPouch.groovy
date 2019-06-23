package mechanics

import com.lishid.openinv.internal.v1_11_R1.SpecialEnderChest
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class EnderPouch extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.ENDER_CHEST) {
            block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.ENDER_CHEST, 1));
            block.setType(Material.AIR);
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            ItemStack heldItem = player.getItemInHand();
            if (heldItem != null && (heldItem.getType() == Material.ENDER_CHEST && player.isSneaking() || heldItem.getType() == Material.EYE_OF_ENDER)) {
                SpecialEnderChest chest = Bukkit.getServer().getPluginManager().getPlugin("OpenInv").getPlayerEnderChest(player, true);
                player.openInventory(chest.getBukkitInventory());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equals("container.enderchest")) {
            event.setCancelled(false)
        }
    }

}
