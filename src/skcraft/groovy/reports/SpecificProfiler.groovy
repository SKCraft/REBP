package reports
import com.sk89q.minecraft.util.commands.CommandException
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

import java.util.logging.Level
import java.util.logging.Logger

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods
import static com.sk89q.rebar.util.command.SimpleCommandGroup.group

class SpecificProfiler extends AbstractCapsule {

    private static final Logger logger = Logger.getLogger(SpecificProfiler.class.getCanonicalName());

    final int DELAY = 10000;
    boolean running = false;
    Timer timer = getGuard().add(new Timer());
    File file = new File("report.csv");

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), group().child('sp', fromMethods(this, provider)));
    }

    @Command(permit = "skcraft.specific-profiler")
    public synchronized void start(final CommandSender sender) {
        if (running) {
            throw new CommandException("The profiler is currently already running.")
        } else {
            running = true;
            com.skcraft.server.SpecificProfiler.getInstance().start();

            ChatUtil.msg(sender, ChatColor.YELLOW, "The profiler will stop in " + (DELAY / 1000) + " seconds.");

            timer.schedule(new TimerTask() {
                @Override
                void run() {
                    stop(true);
                    ChatUtil.msg(sender, ChatColor.YELLOW, "Report generated. Please see panel for report.");
                }
            }, DELAY);
        }
    }

    @Command(permit = "skcraft.specific-profiler")
    public synchronized void abort(CommandSender sender) {
        stop(false);
        ChatUtil.msg(sender, ChatColor.YELLOW, "Profiler report creation was aborted.");
    }

    public synchronized void stop(boolean addOwners) {
        if (running) {
            running = false;
            com.skcraft.server.SpecificProfiler.getInstance().stopAndDump(file);
            if (addOwners) {
                try {
                    callOwnersScript();
                } catch (Throwable t) {
                    logger.log(Level.WARNING, "Failed to add owners", t);
                }
            }
        }
    }

    private void callOwnersScript() {
        ProcessBuilder pb = new ProcessBuilder(
                "/usr/local/bin/python2.7",
                "/home/minecraft/bin/csv_chunk_owners.py",
                "report.csv");
        File tempFile = new File("temp_sp.log");
        try {
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.to(tempFile));
            Process p = pb.start();
            p.getInputStream().close();
            p.waitFor();
        } finally {
            tempFile.delete();
        }
    }

}
