import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Stairs
import org.dynmap.DynmapAPI

class RoadTools21 extends AbstractCapsule implements Listener {

    private static final Random random = new Random();
    private static final Set<Material> PATHIFY = ImmutableSet.of(Material.COBBLESTONE, Material.GRAVEL, Material.STONE, Material.DOUBLE_STEP, Material.DIRT, Material.GRASS);
    private static final Set<Material> COVER_BLOCK = ImmutableSet.of(Material.LONG_GRASS, Material.RED_ROSE, Material.YELLOW_FLOWER, Material.DOUBLE_PLANT,
            Material.DEAD_BUSH, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.SNOW);
    private static final List<BlockFace> CARDINAL_FACES = ImmutableList.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    @Override
    void preBind() {
        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);
        System.out.println("SKCraft Road Tools r16");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!player.isOp()) return;
        if (item == null || item.getType() != Material.STICK) return;
        if (!item.getItemMeta().getDisplayName().equalsIgnoreCase("Roads")) return;

        Block block = player.getTargetBlock(null as Set, 100);
        if (block != null) {
            if (COVER_BLOCK.contains(block.getType())) {
                block = block.getRelative(0, -1, 0);
            }

            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        for (int y = 1; y >= -3; y--) {
                            Block target = block.getRelative(x, y, z);
                            if (y > 0) {
                                target.setType(Material.AIR);
                            } else {
                                if (target.getType() == Material.AIR || COVER_BLOCK.contains(target.getType())) {
                                    target.setType(Material.GRASS);
                                }
                            }
                            transformBlock(target, x == 0 || z == 0);
                        }
                    }
                }
            } else {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        for (int y = 1; y >= -3; y--) {
                            Block target = block.getRelative(x, y, z);
                            placeStairs(target);
                        }
                    }
                }
            }
        } else {
            ChatUtil.msg(player, ChatColor.RED, "No block in sight!");
        }
    }

    public void transformBlock(Block block, boolean center) {
        Block above = block.getRelative(0, 1, 0);
        if (COVER_BLOCK.contains(block.getType())) {
            block.setType(Material.AIR);
        }

        if (block.getType() == Material.AIR) {
            int surroundingSolid = 0;
            if (isWallBlock(block.getRelative(1, 0, 0).getType()) && !isWallBlock(block.getRelative(1, 1, 0).getType())) surroundingSolid++;
            if (isWallBlock(block.getRelative(-1, 0, 0).getType()) && !isWallBlock(block.getRelative(-1, 1, 0).getType())) surroundingSolid++;
            if (isWallBlock(block.getRelative(0, 0, 1).getType()) && !isWallBlock(block.getRelative(0, 1, 1).getType())) surroundingSolid++;
            if (isWallBlock(block.getRelative(0, 0, -1).getType()) && !isWallBlock(block.getRelative(0, 1, -1).getType())) surroundingSolid++;

            /*if (surroundingSolid == 1 && center) {
                block.setType(Material.COBBLESTONE_STAIRS);
                for (BlockFace face : CARDINAL_FACES) {
                    if (isWallBlock(block.getRelative(face).getType())) {
                        BlockState state = block.getState();
                        Stairs data = (Stairs) state.getData();
                        data.setFacingDirection(face.getOppositeFace());
                        state.setData(data);
                        state.update();
                    }
                }
            } else*/ if (surroundingSolid >= 3) {
                block.setType(Material.GRASS_PATH);
            }
        }

        if (PATHIFY.contains(block.getType()) && above.getType() == Material.AIR) {
            block.setType(Material.GRASS_PATH);
        }

        triggerRender(block);
    }

    public void placeStairs(Block block) {
        Block below = block.getRelative(0, -1, 0);
        BlockFace face = null;

        if (block.getType() == Material.AIR && below.getType() == Material.GRASS_PATH) {
            if (block.getRelative(1, 0, 0).getType() == Material.GRASS_PATH) {
                face = BlockFace.EAST;
            } else if (block.getRelative(-1, 0, 0).getType() == Material.GRASS_PATH) {
                face = BlockFace.WEST;
            } else if (block.getRelative(0, 0, -1).getType() == Material.GRASS_PATH) {
                face = BlockFace.NORTH;
            } else if (block.getRelative(0, 0, 1).getType() == Material.GRASS_PATH) {
                face = BlockFace.SOUTH;
            }
        }

        if (face != null) {
            block.setType(Material.COBBLESTONE_STAIRS);
            BlockState state = block.getState();
            Stairs data = (Stairs) state.getData();
            data.setFacingDirection(face);
            state.setData(data);
            state.update();
        }
    }

    public static void triggerRender(Block block) {
        DynmapAPI api = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
        api.triggerRenderOfBlock(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static boolean isWallBlock(Material material) {
        return material.isSolid() && !material.isTransparent() && material.isOccluding() && !material.name().contains("STAIRS");
    }
}
