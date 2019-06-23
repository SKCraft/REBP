/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

public interface Payment {

    void deposit(TransactionEndPoint endPoint) throws TransactionException;

    Payment withdraw(TransactionEndPoint endPoint) throws TransactionException;
    
    boolean canAfford(TransactionEndPoint endPoint);
    
    int getAmountAfforded(TransactionEndPoint endPoint);
    
    boolean canDeposit(TransactionEndPoint endPoint);
    
    String toString();

}
