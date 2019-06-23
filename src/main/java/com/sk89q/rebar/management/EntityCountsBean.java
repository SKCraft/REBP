/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class EntityCountsBean extends AbstractComponent implements DynamicMBean {

    private static final Class<?>[] countClasses = new Class<?>[] {
        Entity.class,
        LivingEntity.class,
        Animals.class,
        Player.class,
        Bat.class,
        Blaze.class,
        CaveSpider.class,
        Chicken.class,
        Cow.class,
        Creature.class,
        Creeper.class,
        Enderman.class,
        Ghast.class,
        Item.class,
        Monster.class,
        Ocelot.class,
        Painting.class,
        Pig.class,
        PigZombie.class,
        Projectile.class,
        Sheep.class,
        Silverfish.class,
        Skeleton.class,
        Slime.class,
        Snowman.class,
        Spider.class,
        Squid.class,
        Vehicle.class,
        Villager.class,
        Zombie.class,
    };

    private Map<String, AtomicInteger> cache;
    private long cacheTime;

    @Override
    public void initialize() {
        ManagementUtils.register(this, "com.sk89q.rebar.management:type=EntityCounts");
    }

    @Override
    public void shutdown() {
    }

    private Map<String, AtomicInteger> buildMap() {
        Map<String, AtomicInteger> map = new HashMap<String, AtomicInteger>();
        for (Class<?> clazz : countClasses) {
            String name = clazz.getSimpleName();
            map.put(name, new AtomicInteger());
        }
        return map;
    }

    private synchronized Map<String, AtomicInteger> getEntityCounts() {
        long now = System.currentTimeMillis();
        if (now - cacheTime < 1000) {
            return cache;
        }
        cacheTime = now;

        Map<String, AtomicInteger> data = buildMap();

        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    for (Class<?> clazz : countClasses) {
                        if (clazz.isAssignableFrom(entity.getClass())) {
                            String name = clazz.getSimpleName();
                            AtomicInteger integer = data.get(name);
                            integer.incrementAndGet();
                        }
                    }
                }
            }
        }

        return cache = data;
    }

    @Override
    public synchronized Integer getAttribute(String name)
            throws AttributeNotFoundException {
        Map<String, AtomicInteger> counts = getEntityCounts();
        Integer value = counts.get(name).intValue();
        if (value != null) {
            return value;
        } else {
            throw new AttributeNotFoundException("No such property: " + name);
        }
    }

    @Override
    public synchronized void setAttribute(Attribute attribute)
            throws InvalidAttributeValueException {
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        Map<String, AtomicInteger> counts = getEntityCounts();
        for (String name : names) {
            Integer v = counts.get(name).intValue();
            if (v == null) {
                continue;
            }
            list.add(new Attribute(name, v));
        }
        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList change) {
        AttributeList list = new AttributeList();
        Map<String, AtomicInteger> counts = getEntityCounts();
        for (Entry<String, AtomicInteger> entry : counts.entrySet()) {
            list.add(new Attribute(entry.getKey(), entry.getValue().intValue()));
        }
        return list;
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig)
            throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        Map<String, AtomicInteger> counts = getEntityCounts();
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[counts.size()];
        int i = 0;
        for (Entry<String, AtomicInteger> entry : counts.entrySet()) {
            attrs[i] = new MBeanAttributeInfo(
                    entry.getKey(),
                    "java.lang.Integer",
                    entry.getKey(), true,
                    // isReadable
                    false, // isWritable
                    false); // isIs
            i++;
        }
        MBeanOperationInfo[] opers = new MBeanOperationInfo[0];
        return new MBeanInfo(this.getClass().getName(), "Entity Counts MBean",
                attrs, null, opers, null);
    }

}