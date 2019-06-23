package mechanics

import com.sk89q.rebar.capsule.AbstractCapsule;
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.ChatColor
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent;

public class EasierMinecraft extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        HumanEntity human = event.getEntity();
        if (human instanceof Player) {
            Player player = (Player) human;
            int previousLevel = player.getFoodLevel();
            int newLevel = event.getFoodLevel();
            if (newLevel - previousLevel < 0) {
                player.setSaturation(2);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location loc = player.getLocation();
        player.sendMessage(ChatColor.DARK_GRAY.toString() + "(You died at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ").");
    }

}
