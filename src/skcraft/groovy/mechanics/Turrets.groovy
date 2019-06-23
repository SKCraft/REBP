/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.metadata.FixedMetadataValue

public class Turrets extends AbstractCapsule implements Listener, Runnable {

    private static final String METADATA_SHOT_KEY = "rebar.turrets.shot";
    private static final int SEARCH_RADIUS = 45;
    private static final int SHOOT_RADIUS = SEARCH_RADIUS + 6;
    private static final int SHOOT_RADIUS_SQ = SHOOT_RADIUS * SHOOT_RADIUS;
    private static final int MAX_Y_RADIUS = 32;
    private final Random random = new Random();
    private final Map<EnderCrystal, TurretData> activeTurrets = new WeakHashMap<>();
    private final Set<Chunk> chunksToCheck = new HashSet<>();

    @Override
    void preBind() {
        System.out.println("SKCraft Turrets r37 loading...");

        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);

        final int index = Rebar.getInstance().registerInterval(this, 1, 1);
        guard.add(new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(index);
            }
        });

        for (World world : Bukkit.getServer().getWorlds()) {
            for (EnderCrystal entity : world.getEntitiesByClass(EnderCrystal.class)) {
                activeTurrets.put(entity, new TurretData());
            }
        }
    }

    public static boolean isValidTurret(Entity entity) {
        return entity.getLocation().getBlock().getRelative(0, -2, 0).getType() == Material.BEACON;
    }

    public static boolean shouldTarget(Entity entity) {
        return (entity instanceof Monster &&
                !(entity instanceof Wither) &&
                !(entity instanceof Blaze) &&
                !(entity instanceof Enderman) &&
                !(entity instanceof Guardian)) ||
                entity instanceof Slime ||
                (entity instanceof Horse && (entity as Horse).getVariant() == Horse.Variant.SKELETON_HORSE && !(entity as Horse).isTamed());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        chunksToCheck.add(event.getChunk());
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            event.blockList().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null && event.getItem().getType() == Material.END_CRYSTAL) {
            chunksToCheck.add(event.getClickedBlock().getRelative(event.getBlockFace()).getChunk());
        }
    }

    public void scanChunk(Chunk chunk) {
        if (!chunk.isLoaded()) {
            return;
        }
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof EnderCrystal) {
                if (!activeTurrets.containsKey(entity)) {
                    activeTurrets.put(entity, new TurretData());
                }
            }
        }
    }

    @Override
    void run() {
        for (Chunk chunk : chunksToCheck) {
            scanChunk(chunk);
        }
        chunksToCheck.clear();

        long now = System.nanoTime();
        Iterator<Map.Entry<EnderCrystal, TurretData>> it = activeTurrets.iterator();

        while (it.hasNext()) {
            Map.Entry<EnderCrystal, TurretData> entry = it.next();
            EnderCrystal turret = entry.getKey();
            TurretData data = entry.getValue();

            if (turret.isDead()) {
                it.remove();
                continue;
            }

            boolean isValid = isValidTurret(turret);
            boolean thinking = now >= data.nextThink;
            boolean justKilled = false;

            if (!isValid && data.valid) {
                turret.setBeamTarget(null);
            }

            data.valid = isValid;

            if (!isValid) {
                continue;
            }

            if (data.target != null && (data.target.isDead() || !data.target.getWorld().equals(turret.getWorld()))) {
                data.target = null;
                justKilled = true;
            }

            if (thinking) {
                data.nextThink = now + random.nextDouble() * 5e+8 + 5e+8;

                if (data.target == null) {
                    if (!justKilled) {
                        LivingEntity bestCandidate = null;
                        double bestDistanceSq = Double.MAX_VALUE;

                        for (Entity candidate : turret.getNearbyEntities(SEARCH_RADIUS, MAX_Y_RADIUS, SEARCH_RADIUS)) {
                            if (shouldTarget(candidate)) {
                                LivingEntity livingEntity = (LivingEntity) candidate;
                                if (livingEntity.hasLineOfSight(turret)) {
                                    double distanceSq = candidate.getLocation().distanceSquared(turret.getLocation());
                                    if (distanceSq < bestDistanceSq) {
                                        bestCandidate = livingEntity;
                                        bestDistanceSq = distanceSq;
                                    }
                                }
                            }
                        }

                        if (bestCandidate != null) {
                            data.target = bestCandidate;
                        }
                    }
                } else {
                    turret.getWorld().playSound(turret.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 1f, 4f);
                    data.target.getWorld().playSound(data.target.getLocation(), Sound.ENTITY_LIGHTNING_IMPACT, 1f, 4f);
                    data.target.damage(5);
                    data.target.setMetadata(METADATA_SHOT_KEY, new FixedMetadataValue(Rebar.getInstance(), true));

                    if (data.target.getFireTicks() < 5) {
                        data.target.setFireTicks(2000);
                    }
                }
            }

            if (data.target != null) {
                turret.setBeamTarget(data.target.getLocation().subtract(0, 2, 0));
            } else if (justKilled) {
                turret.setBeamTarget(null);
            }
        }
    }

    private static class TurretData {
        public boolean valid = false;
        public LivingEntity target;
        public long nextThink = 0;
    }
}
