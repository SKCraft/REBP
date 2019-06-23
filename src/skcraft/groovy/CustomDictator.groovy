import com.skcraft.sidechannel.ThinkListener
import com.skcraft.sidechannel.events.EntityBenchmarkEvent
import com.skcraft.sidechannel.events.EntityThinkEvent
import com.skcraft.sidechannel.events.TileEntityBenchmarkEvent
import com.skcraft.sidechannel.events.TileEntityThinkEvent
import org.bukkit.entity.Entity
import org.bukkit.entity.Monster

class CustomDictator implements ThinkListener {
    private static final Random random = new XORShiftRandom();

    @Override
    void onEntityThink(EntityThinkEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Monster) {
            if (entity.getTarget() == null) {
                event.setCancelled(random.nextDouble() <= 0.5);
            }
        }
    }

    @Override
    void onEntityBenchmark(EntityBenchmarkEvent event) {
    }

    @Override
    void onTileEntityThink(TileEntityThinkEvent event) {
        Object te = event.getNativeTileEntity();

        if (te instanceof logisticspipes.proxy.cc.LogisticsTileGenericPipe_CC) {
            event.setCancelled(random.nextInt(100) <= 60);
        } else if (te instanceof gcewing.lighting.TEFloodlightBeam) {
            event.setCancelled(random.nextInt(100) <= 80);
        } else if (te instanceof com.eloraam.redpower.machine.TileTube) {
            event.setCancelled(random.nextInt(100) <= 50);
        } else if (te instanceof buildcraft.transport.TileGenericPipe) {
            event.setCancelled(random.nextInt(100) <= 50);
        }
    }

    @Override
    void onTileEntityBenchmark(TileEntityBenchmarkEvent event) {
    }

    private static class XORShiftRandom extends Random {
        private long seed = System.nanoTime();

        public XORShiftRandom() {
        }
        protected int next(int nbits) {
            // N.B. Not thread-safe!
            long x = this.seed;
            x ^= (x << 21);
            x ^= (x >>> 35);
            x ^= (x << 4);
            this.seed = x;
            x &= ((1L << nbits) -1);
            return (int) x;
        }
    }
}
