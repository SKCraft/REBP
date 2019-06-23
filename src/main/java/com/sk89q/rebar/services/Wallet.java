/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.services;

public interface Wallet {

    public int getAmount();

    public void setAmount(int amount);
    
    public int add(int amount);
    
}
