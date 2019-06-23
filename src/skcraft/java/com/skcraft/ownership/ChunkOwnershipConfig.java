/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.ListStructureType;
import com.sk89q.rebar.config.declarative.DefaultBoolean;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.DefaultString;
import com.sk89q.rebar.config.declarative.ListType;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.config.types.ClassLoaderBuilder;

import java.util.List;
import java.util.Set;

@SettingBase("chunk-ownership")
class ChunkOwnershipConfig extends ConfigurationBase {

    @Setting("database.dsn")
    public String dsn;
    
    @Setting("database.username")
    public String username;
    
    @Setting("database.password")
    public String password;
    
    @Setting("free-chunks") @DefaultInt(4)
    public Integer freeChunkCount;
    
    @Setting("payment-item.id") @DefaultInt(-1)
    public Integer paymentItem;
    
    @Setting("payment-item.name") @DefaultString("UNCONFIGURED")
    public String paymentItemName;
    
    @Setting("price.under-1000") @DefaultInt(20)
    public Integer priceUnder1000;
    
    @Setting("price.over-1000") @DefaultInt(10)
    public Integer priceOver1000;
    
    @Setting("max-claim.max-distance-from-origin") @DefaultInt(20000)
    public Integer maxDistance;
    
    @Setting("max-claim.min-distance-from-origin") @DefaultInt(200)
    public Integer minDistance;
    
    @Setting("max-claim.chunks-in-direction") @DefaultInt(10)
    public Integer maxChunkLengthClaim;
    
    @Setting("max-claim.chunks") @DefaultInt(10)
    public Integer maxChunkClaim;
    
    @Setting("max-claim.total-chunks") @DefaultInt(100)
    public Integer maxOwnedChunks;

    @Setting("unprotected.freely-usable-blocks") @ListType(Integer.class)
    public Set<Integer> ignoredBlocks;

    @Setting("unprotected.below-y-items") @ListType(Integer.class)
    public Set<Integer> unprotectedItems;

    @Setting("unprotected.below-y") @DefaultInt(40)
    public Integer unprotectedBelowY;

    @Setting("activity.force-all-active") @DefaultBoolean(true)
    public Boolean allActive;

    @Setting("activity.restricted-classes") @ListStructureType(ClassLoaderBuilder.class)
    public List<Class<?>> restrictedClasses;
}
