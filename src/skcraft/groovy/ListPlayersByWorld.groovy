import com.google.common.base.Joiner
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.util.CommandUtil
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class ListPlayersByWorld extends AbstractCapsule {

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider))
    }

    @Override
    void postUnbind() {
    }

    @Command(permit = "*")
    public void whoIsHere(CommandSender sender) {
        Player player = CommandUtil.checkPlayer(sender);
        World world = player.getWorld();
        List<Player> players = world.getPlayers();

        players.remove(player);

        if (players.isEmpty()) {
            ChatUtil.msg(player, ChatColor.GREEN, "No one else is in this world!");
        } else {
            List<String> names = new ArrayList<>();
            for (Player p : players) {
                names.add(p.getName());
            }
            ChatUtil.msg(player, ChatColor.YELLOW, "With you: " + Joiner.on(", ").join(names));
        }
    }

}
