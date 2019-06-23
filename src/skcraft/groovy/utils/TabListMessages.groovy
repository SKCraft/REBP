/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package utils



import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.google.common.io.Closer
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.yaml.snakeyaml.Yaml

import java.lang.reflect.InvocationTargetException
import java.util.logging.Level
import java.util.logging.Logger

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class TabListMessages extends AbstractCapsule implements Listener {

    private static final Logger logger = Logger.getLogger(TabListMessages.class.getName());
    private ProtocolManager protocolManager;
    private WrappedChatComponent header = null;
    private WrappedChatComponent footer = null;

    private static String replaceColors(String message) {
        return message.replaceAll("(?i)&([a-f0-9klmnor])", "\u00A7\$1");
    }

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider))
        load();
        System.out.println("SKCraft Server Tab List r7");
    }

    public void sendHeaderFooter(Player player) {
        if (header != null && footer != null) {
            PacketContainer packetContainer = protocolManager.
                    createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

            packetContainer.getChatComponents().
                    write(0, header).
                    write(1, footer);

            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                logger.log(Level.WARNING, "Cannot send packet " + packetContainer, e);
            }
        }
    }

    public void load() {
        Closer closer = Closer.create();
        File file = new File("tablist.yml");
        try {
            Yaml yaml = new Yaml();
            FileReader fr = closer.register(new FileReader(file));
            Map<Object, Object> map = (Map<Object, Object>) yaml.load(fr);
            header = WrappedChatComponent.fromText(replaceColors(String.valueOf(map.get("header"))));
            footer = WrappedChatComponent.fromText(replaceColors(String.valueOf(map.get("footer"))));

            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                sendHeaderFooter(player);
            }
        } catch (Exception e) {
            header = null;
            footer = null;
            logger.log(Level.WARNING, "Cannot read tab list info", e);
        } finally {
            closer.close();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendHeaderFooter(event.getPlayer());
    }

    @Command(permit = "skcraft.tablist.reload")
    public void reloadTabList(CommandSender sender) {
        load();
        ChatUtil.msg(sender, ChatColor.YELLOW, "Reloaded the tab list messages.");
    }
}
