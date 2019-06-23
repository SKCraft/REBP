import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class BlockCommand extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();

        if (message.matches("/(about|plugins|pl|ver(sion)?|timings|rl|reload|\\?)( .*)?")) {
            event.setMessage("/_doesnotexist");
        } else if (message.matches("/[^ ]*:[^ ]*( .*)?")) {
            event.setMessage("/_doesnotexist");
        }
    }

}
