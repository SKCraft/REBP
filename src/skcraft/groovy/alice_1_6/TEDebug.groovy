package alice_1_6

import bsh.Interpreter
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitUtil
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import gnu.trove.map.hash.TObjectIntHashMap
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

import static com.sk89q.rebar.util.command.SimpleCommandGroup.group
import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class TEDebug extends AbstractCapsule {

    private static final int ITEM_THRESHOLD = 100;
    private static final int ALERT_INTERVAL = 1000 * 60 * 15;
    private static final int CONDUIT_ID = 1281;
    private final Timer timer = getGuard().add(new Timer());

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), group().child('tedebug', fromMethods(this, provider)));

        timer.schedule(new TimerTask() {
            @Override
            void run() {
                Rebar.getInstance().registerTimeout(new Runnable() {
                    @Override
                    void run() {
                        alertPlayers();
                    }
                }, 0);
            }
        }, 0, ALERT_INTERVAL);
    }

    public TObjectIntHashMap<Vector> getStuffed() {
        TObjectIntHashMap<Vector> top = new TObjectIntHashMap<>();
        Interpreter interpreter = new Interpreter();
        interpreter.set("top", top);

        Object obj = interpreter.eval("""
        import thermalexpansion.part.conduit.*;
        import thermalexpansion.part.conduit.item.*;
        import com.sk89q.worldedit.Vector;

        for (var grid : GridTickHandler.tickingGrids) {
            for (var part : grid.nodeSet) {
                if (part instanceof ConduitItem) {
                    for (var item : part.myItems) {
                        top.adjustOrPutValue(new Vector((int) item.x, (int) item.y, (int) item.z), 1, 1);
                    }
                }
            }
        }""");

        return top;
    }

    @Command(as = 'alert', permit = "skcraft.performance.thermal-expansion")
    public void alertPlayers() {
        TObjectIntHashMap<Vector> top = getStuffed();
        ListMultimap<Player, String> messages = ArrayListMultimap.create();
        World mainWorld = Bukkit.getServer().getWorlds().get(0);
        List<Player> players = mainWorld.getPlayers();

        for (Vector pos : top.iterator()) {
            int count = top.get(pos);
            if (count >= ITEM_THRESHOLD) {
                Location loc = BukkitUtil.toLocation(mainWorld, pos);
                if (loc.getChunk().isLoaded() && loc.getBlock().getTypeId() == CONDUIT_ID) {
                    for (Player player : players) {
                        if (loc.distanceSquared(player.getLocation()) < 100 * 100) {
                            messages.put(player, ChatColor.AQUA.toString() + count + ChatColor.WHITE + " items at " + ChatColor.AQUA + pos.toString());
                        }
                    }
                }
            }
        }

        for (Player player : messages.keySet()) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
            ChatUtil.msg(player, ChatColor.RED, "══════════════════════════════════════════════");
            ChatUtil.msg(player,  ChatColor.RED, ChatColor.BOLD, "AUTOMATED WARNING:");
            ChatUtil.msg(player, ChatColor.RED, "There are 'STUFFED' Thermal Expansion item conduits near you containing " +
                    "items indefinitely bouncing inside. Please fix ASAP at:");
            for (String message : messages.get(player)) {
                ChatUtil.msg(player, message);
            }
            ChatUtil.msg(player, ChatColor.RED, "══════════════════════════════════════════════");
        }
    }

    @Command(permit = "skcraft.performance.thermal-expansion")
    public void stuffed(CommandSender sender) {
        TObjectIntHashMap<Vector> top = getStuffed();

        boolean found = false;
        for (Vector pos : top.iterator()) {
            int count = top.get(pos);
            if (count >= ITEM_THRESHOLD) {
                sender.sendMessage(ChatColor.AQUA.toString() + count + " items " + ChatColor.GRAY + pos.toString());
                found = true;
            }
        }

        if (!found) {
            sender.sendMessage(ChatColor.RED.toString() + "No conduits found with >= " + ITEM_THRESHOLD + " items.");
        }
    }

}
