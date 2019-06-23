package alice_1_6

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent

public class CommandPerms extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);

    }

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.addAttachment(Rebar.getInstance(), "cofh.command.CommandHandler", true);
    }

    @EventHandler
    void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/cofh") && !message.startsWith("/cofh friend")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED.toString() + "Sorry, you can't do that.");
        }
    }

}
