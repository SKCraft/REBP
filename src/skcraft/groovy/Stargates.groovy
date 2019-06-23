import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

class Stargates extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.addAttachment(Rebar.getInstance(), "stargate.use", true);
        player.addAttachment(Rebar.getInstance(), "stargate.create", true);
        player.addAttachment(Rebar.getInstance(), "stargate.destroy", true);
        player.addAttachment(Rebar.getInstance(), "stargate.free", true);
    }

}
