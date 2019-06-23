import com.comphenix.protocol.events.PacketEvent
import com.skcraft.network.PacketMonitor

class CustomPacketHandler implements PacketMonitor {

    private int i = 0;

    @Override
    void onPacketSending(PacketEvent event) {
        if (i < 5) {
            System.out.println(event.getPlayer().getName());
            i++;
        }
    }

}
