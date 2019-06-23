import au.com.bytecode.opencsv.CSVWriter
import com.google.common.io.Closer
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.annotation.Sender
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class ChunkExport extends AbstractCapsule {

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider));
    }

    @Command(permit = "skcraft.debug.exportchunks")
    public void exportChunks(@Sender Player sender) {
        sender.sendMessage("Test");

        Closer closer = Closer.create();
        try {
            FileWriter fw = closer.register(new FileWriter("loaded_chunks.csv"));
            CSVWriter writer = closer.register(new CSVWriter(fw));
            writer.writeNext(["World", "X", "Z", "InViewDistance"] as String[]);

            for (World world : server.getWorlds()) {
                for (Chunk chunk : world.getLoadedChunks()) {
                    var x = chunk.getX();
                    var z = chunk.getZ();

                    writer.writeNext([
                            world.getName(),
                            String.valueOf(x),
                            String.valueOf(z),
                            world.isChunkInUse(x, z) ? "Yes" : "No",
                    ] as String[]);
                }
            }
        } finally {
            closer.close();
        }
    }

}
