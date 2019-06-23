import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.command.CommandSender

import static com.sk89q.rebar.util.command.SimpleCommandGroup.group
import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

public class TestCapsule extends AbstractCapsule {

    private Timer timer = getGuard().add(new Timer());

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), group().child('testing', fromMethods(this, provider), ['t']));
    }

    @Override
    void postUnbind() {
        System.out.println("bye!!");
    }

    @Command(permit = "*")
    public void test(CommandSender sender) {
        sender.sendMessage("hi!!!!!!");
    }

}
