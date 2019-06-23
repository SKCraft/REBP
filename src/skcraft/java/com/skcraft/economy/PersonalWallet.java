/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import com.sk89q.rebar.services.Wallet;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity()
@Table(name = "wallets")
public class PersonalWallet implements Wallet {
    
    @EmbeddedId
    private PersonalWalletId id;
    private int amount;

    public PersonalWallet() {
    }

    public PersonalWallet(String name, String server) {
        id = new PersonalWalletId(name, server);
        amount = 0;
    }

    public PersonalWalletId getId() {
        return id;
    }

    public void setId(PersonalWalletId id) {
        this.id = id;
    }

    public String getName() {
        return id.getName();
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    @Override
    public int add(int amount) {
        setAmount(this.amount + amount);
        return this.amount;
    }

}
