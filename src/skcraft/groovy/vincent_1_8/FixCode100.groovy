package vincent_1_8

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.Bukkit
import org.bukkit.event.Listener

class FixCode100 extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);

        Runnable task = new CheckCode100Task();
        final int index = Rebar.getInstance().registerInterval(task, 0, 20 * 10);
        guard.add(new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().cancelTask(index);
            }
        });

    }

    public class CheckCode100Task implements Runnable {
        private final Random random = new Random();

        @Override
        void run() {
            try {
                Bukkit.getServer().getOfflinePlayer("Notch");
            } catch (NullPointerException e) {
                Bukkit.getServer().getServer().getUserCache().b();
            }
        }
    }
}
