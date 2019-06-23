/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.services;

import org.bukkit.OfflinePlayer;

public interface WalletService {
    
    void beginTransaction();
    
    void commitTransaction();
    
    Wallet getWallet(OfflinePlayer player);

    void saveWallet(OfflinePlayer player);
    
}
