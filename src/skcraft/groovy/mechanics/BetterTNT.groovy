package mechanics

import com.google.common.collect.Lists
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.services.WalletService
import com.sk89q.rebar.util.BlockUtil
import com.sk89q.skcraft.economy.CreditsPayment
import com.sk89q.skcraft.economy.Payment
import com.sk89q.skcraft.economy.TransactionEndPoint
import com.sk89q.skcraft.economy.TransactionException
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BukkitBindings
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.BookMeta

import static com.sk89q.rebar.util.ChatUtil.msg

class BetterTNT extends AbstractCapsule implements Listener {

    private static final int LICENSE_DISTANCE = 100;
    private static final int MAX_EXPLOSION_DISTANCE = 5;
    private static final String BOOK_AUTHOR = "License Board";
    private static final String DEMOLITION_TYPE = "Demolition";

    @Override
    void preBind() {
        BukkitBindings.bindListeners(getGuard(), this);
    }

    public static boolean isWithinCuboidRadius(Location origin, Location location, double radius) {
        if (Math.abs(origin.getX() - location.getX()) > radius) return false;
        if (Math.abs(origin.getY() - location.getY()) > radius) return false;
        if (Math.abs(origin.getZ() - location.getZ()) > radius) return false;
        return true;
    }

    @EventHandler
    void onExplosionPrime(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed) {
            if (hasNearbyLicense(entity.getLocation(), LICENSE_DISTANCE, DEMOLITION_TYPE)) {
                event.setRadius(20.0f);
                ((TNTPrimed) event.getEntity()).setYield(200.0f);
            }
        }
    }

    @EventHandler
    void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof TNTPrimed) {
            Iterator<Block> it = event.blockList().iterator();
            while (it.hasNext()) {
                Block block = it.next();
                if (!isWithinCuboidRadius(block.getLocation(), entity.getLocation(), MAX_EXPLOSION_DISTANCE)) {
                    it.remove();
                }
            }
        }
    }

    @EventHandler
    void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;

            Entity damager = entityEvent.getDamager();

            if (damager instanceof TNTPrimed) {
                if (!isWithinCuboidRadius(event.getEntity().getLocation(), damager.getLocation(),  MAX_EXPLOSION_DISTANCE)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public static boolean hasNearbyLicense(Location origin, double radius, String type) {
        double radiusSq = radius * radius;

        for (Player player : origin.getWorld().getPlayers()) {
            if (player.getWorld().equals(origin.getWorld()) && player.getLocation().distanceSquared(origin) <= radiusSq) {
                if (hasLicense(player, type)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasLicense(Player player, String type) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack slotItem = inventory.getItem(i);
            if (slotItem != null && slotItem.getType() == Material.WRITTEN_BOOK) {
                BookMeta meta = (BookMeta) slotItem.getItemMeta();
                if (meta.getAuthor().equals(BOOK_AUTHOR) && meta.getTitle().equals(getBookTitle(player, type))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getBookTitle(Player player, String type) {
        return type + " License for " + player.getName();
    }

    public static ItemStack createLicense(Player player, String type) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle(getBookTitle(player, type));
        meta.setAuthor(BOOK_AUTHOR);
        meta.setPages("To: " + player.getName() + "\nLicense: " + type);
        meta.setDisplayName(ChatColor.GOLD.toString() + type + " License for " + player.getName());
        meta.setLore(Lists.newArrayList("Have this license in your hotbar", "to have it take effect."));
        book.setItemMeta(meta);
        return book;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) return;

        Sign sign = BlockUtil.getState(block, Sign.class);

        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[License Printer]")) {
            return;
        }

        if (!player.isSneaking()) {
            msg(event.getPlayer(), ChatColor.RED, "Please sneak right click to purchase.");
            return;
        }

        ItemStack item = player.getItemInHand();
        Material itemType = item != null ? item.getType() : null;

        switch (itemType) {
            case Material.TNT:
                WalletService walletService = Rebar.getInstance().getServiceManager().load(WalletService.class);
                if (walletService == null) {
                    msg(player, ChatColor.RED, "Wallet service not enabled.");
                    break;
                }
                TransactionEndPoint buyerEndPoint = new TransactionEndPoint(walletService, player, null);
                Payment price = new CreditsPayment(5);

                if (!buyerEndPoint.canAfford(price)) {
                    msg(player, ChatColor.RED, "YOU CANNOT AFFORD THIS.");
                    break;
                }

                try {
                    buyerEndPoint.withdraw(price);
                } catch (TransactionException e) {
                    msg(player, ChatColor.RED, "An error occurred!");
                    e.printStackTrace();
                    break;
                }

                ItemStack licenseItem = createLicense(player, DEMOLITION_TYPE);

                if (player.getInventory().addItem(licenseItem).isEmpty()) {
                    msg(event.getPlayer(), ChatColor.YELLOW, "License purchased!");
                    player.updateInventory();
                } else {
                    player.getWorld().dropItem(player.getLocation(), licenseItem)
                }
                break;
            default:
                msg(event.getPlayer(), ChatColor.RED, "Please hold TNT to purchase a demolition license.");
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!ChatColor.stripColor(event.getLine(0)).equalsIgnoreCase("[License Printer]")) {
            return;
        }

        if (!Rebar.getInstance().hasPermission(event.getPlayer(), "skcraft.licensing")) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            return;
        }

        event.setLine(0, ChatColor.DARK_PURPLE.toString() + "[License Printer]");
        event.setLine(1, "Right click to");
        event.setLine(2, "purchase a");
        event.setLine(3, "license card.");

        msg(event.getPlayer(), ChatColor.YELLOW, "License printer created.");
    }

}
