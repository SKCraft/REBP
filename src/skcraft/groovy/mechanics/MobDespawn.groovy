/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.google.common.collect.ImmutableSet
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class MobDespawn extends AbstractCapsule implements Listener {

    private static final Random random = new Random();

    private static final Set<EntityType> DESPAWN_TYPES = ImmutableSet.builder()
            .add(EntityType.SKELETON)
            .add(EntityType.ZOMBIE)
            .add(EntityType.SPIDER)
            .add(EntityType.CAVE_SPIDER)
            .add(EntityType.CREEPER)
            .build();

    @Override
    void preBind() {
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);
        System.out.println("SKCraft Mob Despawn r5 ");
    }

    public boolean shouldDespawn(Entity entity) {
        return DESPAWN_TYPES.contains(entity.getType()) && entity.getCustomName() == null;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (shouldDespawn(entity)) {
                entity.remove();
            }
        }
    }
}
