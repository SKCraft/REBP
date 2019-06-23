/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.economy;

import javax.persistence.Embeddable;

@Embeddable
public class PersonalWalletId {

    private String name;
    private String server;
    
    public PersonalWalletId() {
    }
    
    public PersonalWalletId(String name, String server) {
        this.name = name;
        this.server = server;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
    
    public int hashCode() {
        return name.hashCode() << 7 + server.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof PersonalWalletId)) {
            return false;
        }
        PersonalWalletId other = (PersonalWalletId) obj;
        return other.getServer().equalsIgnoreCase(getServer())
                && other.getName().equalsIgnoreCase(getName());
    }

}
