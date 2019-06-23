/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package mechanics

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.annotation.Sender
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.FireworkMeta

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

public class MakeMeFabulous extends AbstractCapsule {

    private static final Random random = new Random();

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider));
    }

    @Command(permit = "*")
    public void makeFab(@Sender Player player) {
        World world = player.getWorld();
        for (int i = 0; i < 10; i++) {
            shootFireworksLater(player.getLocation().add(
                    random.nextInt(8) - 4, 5, random.nextInt(8) - 4),
                    i * 6 + random.nextInt(5));
        }
    }

    private void shootFireworksLater(Location location, int delay) {
        Rebar.getInstance().registerTimeout(new Runnable() {
            @Override
            void run() {
                Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(20);
                meta.addEffect(
                        FireworkEffect
                                .builder()
                                .with(FireworkEffect.Type.STAR)
                                .withColor(Color.fromRGB(255, 100, 100))
                                .withTrail()
                                .withFade(Color.fromRGB(255, 100, 100))
                                .build());
                firework.setFireworkMeta(meta);
            }
        }, delay);
    }

}
