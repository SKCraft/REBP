/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.service.ServiceManager;
import com.sk89q.rebar.services.WalletService;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class PersonalWalletManager extends AbstractComponent implements WalletService {

    private static final Logger logger = Logger.getLogger(WalletService.class.getCanonicalName());
    private final Map<String, PersonalWallet> wallets = new HashMap<String, PersonalWallet>();
    private final ReentrantLock walletLock = new ReentrantLock();

    @Override
    public void load() {
        Rebar rebar = Rebar.getInstance();
        rebar.getServiceManager().register(
                WalletService.class, this, ServiceManager.PRIORITY_NORMAL);
        rebar.registerEvents(new PlayerListener());
    }

    @Override
    public void initialize() {
        reload();
    }

    @Override
    public void shutdown() {
        for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
            saveWallet(player);
        }
    }
    
    @Override
    public synchronized void reload() {
        logger.info("Refreshing wallets...");
        
        super.reload();
        
        wallets.clear();
        
        getDatabase().externalModification("wallets", true, true, true);
        
        for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
            loadWallet(player);
        }
    }
    
    public synchronized PersonalWallet loadWallet(OfflinePlayer player) {
        try {
            String serverId = Rebar.getInstance().getServer().getServerId();
            getDatabase().beginTransaction();
            PersonalWallet wallet = getDatabase().find(PersonalWallet.class,
                    new PersonalWalletId(player.getName().toLowerCase(), serverId));
            if (wallet == null) {
                wallet = new PersonalWallet(player.getName().toLowerCase(), serverId);
                getDatabase().save(wallet);
                wallet = getDatabase().find(PersonalWallet.class,
                        new PersonalWalletId(player.getName().toLowerCase(), serverId));
            }
            wallets.put(player.getName().toLowerCase(), wallet);
            getDatabase().commitTransaction();
            return wallet;
        } finally {
            getDatabase().endTransaction();
        }
    }

    @Override
    public synchronized PersonalWallet getWallet(OfflinePlayer player) {
        PersonalWallet wallet = wallets.get(player.getName().toLowerCase());
        if (wallet == null) return loadWallet(player);
        return wallet;
    }

    @Override
    public synchronized void saveWallet(OfflinePlayer player) {
        PersonalWallet wallet = getWallet(player);
        getDatabase().save(wallet);
    }
    
    public synchronized void removeWallet(Player player) {
        PersonalWallet wallet = wallets.get(player.getName().toLowerCase());
        if (wallet == null) return;
        getDatabase().update(wallet);
        wallets.remove(player.getName().toLowerCase());
    }
    
    public List<PersonalWallet> getRichest(int num) {
        String serverId = Rebar.getInstance().getServer().getServerId();
        return getDatabase()
                .find(PersonalWallet.class)
                .where().eq("server", serverId)
                .orderBy().desc("amount")
                .findPagingList(num).getPage(0).getList();
    }

    @Override
    public Collection<Class<?>> getEntities() {
        List<Class<?>> entities = new ArrayList<Class<?>>();
        entities.add(PersonalWallet.class);
        entities.add(PersonalWalletId.class);
        return entities;
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            removeWallet(event.getPlayer());
        }
    }

    @Override
    public void beginTransaction() {
        walletLock.lock();
        //getDatabase().beginTransaction();
    }

    @Override
    public void commitTransaction() {
        walletLock.unlock();
        //getDatabase().commitTransaction();
    }

}
