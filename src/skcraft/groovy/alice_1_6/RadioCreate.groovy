package alice_1_6

import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.sk89q.rebar.util.command.annotation.Command
import com.sk89q.rebar.util.command.parametric.ParameterProvider
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import openblocks.client.radio.RadioManager.RadioStation
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack
import org.bukkit.entity.Player

import static com.sk89q.rebar.util.command.parametric.ParametricMethodExecutor.fromMethods

class RadioCreate extends AbstractCapsule {

    @Override
    void preBind() {
        ParameterProvider provider = Rebar.getInstance().getParameterProvider();
        BukkitBindings.bindCommands(getGuard(), fromMethods(this, provider));
    }

    @Command(as = 'createradio', permit = "skcraft.util.create-radio")
    public void createRadio(CommandSender sender, String name, String url, String desc) {
        ItemStack rawItem = Item.field_77698_e[15254].createStack(
            new RadioStation(url, name, new ArrayList()));
        def tag = rawItem.field_77990_d;
        def display = tag.func_74775_l("display");
        def lore = new net.minecraft.nbt.NBTTagList("Lore");
        lore.func_74742_a(new net.minecraft.nbt.NBTTagString("", desc));
        display.func_74782_a("Lore", lore);
        def item = CraftItemStack.asCraftMirror(rawItem);
        item.setDurability((short) 1);
        ((Player) sender).getInventory().addItem(item);
        ChatUtil.msg(sender, ChatColor.YELLOW, "Created radio crystal!");
    }

}
