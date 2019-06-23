/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Skeleton
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack

public class MoreWitherSkeletonSpawns extends AbstractCapsule implements Listener {

    private static final Random random = new Random();

    @Override
    void preBind() {
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);
        System.out.println("SKCraft More Wither Skeletons r9");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Skeleton &&
                (event.getEntity() as Skeleton).getSkeletonType() == Skeleton.SkeletonType.WITHER &&
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {

            for (int i = 0; i < 1; i++) {
                Skeleton newEntity = event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.SKELETON);
                newEntity.setSkeletonType(Skeleton.SkeletonType.WITHER);
                EntityEquipment equipment = newEntity.getEquipment();
                if (random.nextDouble() <= 0.5) {
                    ItemStack bow = new ItemStack(Material.BOW, 1);
                    bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
                    bow.addEnchantment(Enchantment.ARROW_DAMAGE, 5);
                    bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                    equipment.setItemInMainHand(bow);
                    equipment.setItemInMainHandDropChance(0);
                }
                if (random.nextBoolean()) {
                    equipment.setItemInOffHand(new ItemStack(Material.TORCH, 1));
                    equipment.setItemInOffHandDropChance(0);
                }
            }
        }
    }
}
