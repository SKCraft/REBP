/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import com.sk89q.rebar.services.WalletService;
import com.sk89q.rebar.util.CompoundInventory;
import org.bukkit.OfflinePlayer;

public class TransactionEndPoint {

    private WalletService wallets;
    private OfflinePlayer player;
    private CompoundInventory inventory;
    
    public TransactionEndPoint(WalletService walletService, OfflinePlayer player, CompoundInventory inventory) {
        this.wallets = walletService;
        this.player = player;
        this.inventory = inventory;
    }

    public Payment withdraw(Payment payment) throws TransactionException {
        return payment.withdraw(this);
    }
    
    public boolean canAfford(Payment payment) {
        return payment.canAfford(this);
    }
    
    public boolean canDeposit(Payment payment) {
        return payment.canDeposit(this);
    }
    
    public int hasAmount(Payment payment) {
        return payment.getAmountAfforded(this);
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public CompoundInventory getInventory() {
        return inventory;
    }

    public WalletService getWalletService() {
        return wallets;
    }

}
