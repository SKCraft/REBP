package vincent_1_8

import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent

class PistonDebugger extends AbstractCapsule implements Listener {

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    @EventHandler
    void onBlockPistonExtend(BlockPistonExtendEvent event) {
        System.out.println(event.getBlock());
    }

    @EventHandler
    void BlockPistonRetract(BlockPistonRetractEvent event) {
        System.out.println(event.getBlock());
    }

}
