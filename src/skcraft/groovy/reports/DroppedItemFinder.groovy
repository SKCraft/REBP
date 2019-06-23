package alice_1_6

import com.sk89q.rebar.Rebar
import com.sk89q.worldedit.Vector
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import gnu.trove.map.hash.TObjectIntHashMap
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Item

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class DroppedItemFinder extends AbstractCapsule {

    private static final int ITEM_THRESHOLD = 10;

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider));
    }

    @Command(as = 'finditems', permit = "skcraft.performance.dropped-item-finder")
    public void findDroppedItems(CommandSender sender) {
        int total = 0;

        for (World world : Bukkit.getServer().getWorlds()) {
            TObjectIntHashMap<Vector> top = new TObjectIntHashMap<>();
            for (Item item : world.getEntitiesByClass(Item.class)) {
                total++;
                Location loc = item.getLocation();
                top.adjustOrPutValue(new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), 1, 1);
            }

            for (Vector pos : top.iterator()) {
                int count = top.get(pos);
                if (count >= ITEM_THRESHOLD) {
                    sender.sendMessage(ChatColor.AQUA.toString() + count + " items " + ChatColor.BLUE + world.getName() + " " + ChatColor.GRAY + pos.toString());
                }
            }
        }

        sender.sendMessage(ChatColor.AQUA.toString() + total + " total item entitie(s)");
    }

}
