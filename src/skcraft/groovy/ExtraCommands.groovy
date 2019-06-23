import com.sk89q.minecraft.util.commands.CommandException
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.util.CommandUtil
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class ExtraCommands extends AbstractCapsule {

    private Timer timer = getGuard().add(new Timer());

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider))
    }

    @Override
    void postUnbind() {
        System.out.println("bye!!");
    }

    @Command(permit = "skcraft.netherswap")
    public void netherSwap(CommandSender sender) {
        Player player = CommandUtil.checkPlayer(sender);
        World world = player.getWorld();
        Location loc = player.getLocation();

        if (world.getEnvironment() == World.Environment.NORMAL) {
            player.teleport(new Location(getWorld(World.Environment.NETHER), loc.getX() / 8, loc.getY(), loc.getZ() / 8));
        } else if (world.getEnvironment() == World.Environment.NETHER) {
            player.teleport(new Location(getWorld(World.Environment.NORMAL), loc.getX() * 8, loc.getY(), loc.getZ() * 8));
        } else {
            throw new CommandException("No teleport available for your world type.");
        }
    }

    private static World getWorld(World.Environment environment) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == environment) {
                return world;
            }
        }

        throw new CommandException("World not found with environment " + environment);
    }

}
