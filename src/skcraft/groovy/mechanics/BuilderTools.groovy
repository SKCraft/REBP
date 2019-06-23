/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.google.common.base.Strings
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class BuilderTools extends AbstractCapsule implements Listener {

    private static final String SIMPLE_STACK_NAME = "Simple Stack";

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
        System.out.println("SKCraft Builder Tools r10");
    }

    @SuppressWarnings("GrDeprecatedAPIUsage")
    @EventHandler
    void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack handItem = event.getItem();

        if (handItem != null && handItem.getType() == Material.BLAZE_ROD) {
            String name = Strings.nullToEmpty(handItem.getItemMeta().getDisplayName());

            if (name.equalsIgnoreCase(SIMPLE_STACK_NAME)) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Block clickedBlock = event.getClickedBlock();
                    Block target = clickedBlock.getRelative(event.getBlockFace());
                    ItemStack testItem = new ItemStack(clickedBlock.getType(), 1);

                    if (target.getType() != Material.AIR) {
                        ChatUtil.error(player, "Cannot place stairs there.");
                    } else if (!isStairs(clickedBlock.getType())) {
                        ChatUtil.error(player, "Can only use the stick on stairs.");
                    } else if (!inventory.removeItem(testItem.clone()).isEmpty()) {
                        ChatUtil.error(player, "You are missing the item in your inventory.");
                    } else {
                        BlockState originalState = target.getState();
                        target.setType(clickedBlock.getType());
                        target.setData(clickedBlock.getData());
                        BlockState newState = target.getState();

                        BlockPlaceEvent firedEvent = new BlockPlaceEvent(target, originalState, clickedBlock, testItem, player, true, EquipmentSlot.HAND);
                        Bukkit.getPluginManager().callEvent(firedEvent);
                        if (!firedEvent.isCancelled()) {
                            player.getWorld().playSound(target.getLocation(), Sound.BLOCK_WOOD_PLACE, 1f, 1f);
                            player.getWorld().playEffect(target.getLocation(), Effect.PARTICLE_SMOKE, null);
                        } else {
                            originalState.update();
                        }
                    }
                } else {
                    ChatUtil.error(player, "Use the stick on a block.");
                }
            }
        }
    }

    public static boolean isStairs(Material material) {
        return material.name().endsWith("_STAIRS");
    }
}
