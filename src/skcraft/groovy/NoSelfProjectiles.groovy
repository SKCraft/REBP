/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

import com.sk89q.rebar.capsule.AbstractCapsule;
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class NoSelfProjectiles extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = event.getDamager() as Projectile;
            if (projectile.getShooter().equals(event.getEntity())) {
                System.out.println("(NoSelfProjectiles) " + event.getEntity().toString() + " almost hurt him/herself with a " + projectile.toString());
                event.setCancelled(true);
            }
        }
    }

}
