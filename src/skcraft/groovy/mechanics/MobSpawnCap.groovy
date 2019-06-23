/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.google.common.collect.ImmutableSet
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent

public class MobSpawnCap extends AbstractCapsule implements Listener {

    private static final int CHUNK_RADIUS = 5;

    private static final Set<EntityType> MOB_TYPES = ImmutableSet.builder()
            .add(EntityType.SKELETON)
            .add(EntityType.ZOMBIE)
            .add(EntityType.SPIDER)
            .add(EntityType.CAVE_SPIDER)
            .add(EntityType.CREEPER)
            .add(EntityType.ENDERMAN)
            .add(EntityType.WITCH)
            .add(EntityType.SLIME)
            .add(EntityType.MAGMA_CUBE)
            .add(EntityType.GHAST)
            .add(EntityType.PIG_ZOMBIE)
            .build();

    @Override
    void preBind() {
        System.out.println("SKCraft Mob Spawn Cap r7");
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (isAffected(entity)) {
            int count = getMobCount(entity.getLocation().getChunk());
            int playerCount = world.getPlayers().size();
            int threshold = (int) Math.ceil(world.getMonsterSpawnLimit() / (double) playerCount);

            if (count > threshold) {
                event.setCancelled(true);
            }
        }
    }

    public static boolean isAffected(Entity entity) {
        return entity.getCustomName() == null && (MOB_TYPES.contains(entity.getType()) || !(entity instanceof Animals));
    }

    public static int getMobCount(Chunk root) {
        int chunkX = root.getX();
        int chunkZ = root.getZ();
        int count = 0;

        for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
            for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                Chunk chunk = root.getWorld().getChunkAt(chunkX + x, chunkZ + z);
                if (chunk.isLoaded()) {
                    for (Entity test : chunk.getEntities()) {
                        if (isAffected(test)) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
