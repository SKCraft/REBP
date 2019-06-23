/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.management;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ReflectionUtil;

public class MainThreadSamplerBean extends AbstractComponent implements DynamicMBean {

    private final int INTERVAL = 200;
    private final int BUCKET_SIZE = 5;
    private final int ROTATE_INTERVAL = 3000;

    private static final Logger logger = createLogger(MainThreadSamplerBean.class);

    private final Timer timer = new Timer("CPU Sampler", true);
    private final Map<String, Boolean> loggable = new HashMap<String, Boolean>();
    private final Map<String, int[]> data = new HashMap<String, int[]>();

    private Class<?> tileEntityClass;
    private Class<?> entityClass;
    private int bucket = -1;
    private int usedBuckets = 1;
    private long lastRotate = 0;
    private ThreadMXBean threadBean;
    private long threadId;

    @Override
    public void initialize() {
        getClasses();

        if (tileEntityClass != null && entityClass != null) {
            threadBean = ManagementFactory.getThreadMXBean();

            Rebar.getInstance().registerTimeout(new Runnable() {
                @Override
                public void run() {
                    startLogging(Thread.currentThread().getId());
                }
            }, 0);

            ManagementUtils.register(this, "com.sk89q.rebar.sampler:type=MainThreadSampler");
        } else {
            logger.warning("MainThreadSampler: Failed to get TE/Entity classes");
        }
    }

    private void getClasses() {
        try {
            World world = Bukkit.getWorlds().get(0);
            Entity entity = world.getEntities().get(0);
            Class<?> craftEntityClass = ReflectionUtil
                    .searchHierarchyForClass(entity, "CraftEntity");
            Method method = craftEntityClass.getMethod("getHandle");
            entityClass = method.getReturnType();
            logger.info("MainThreadSampler: Entity class: " + entityClass);

            for (Method m : world.getClass().getMethods()) {
                if (m.getName().equals("getTileEntityAt")) {
                    tileEntityClass = m.getReturnType();
                    break;
                }
            }
            logger.info("MainThreadSampler: TileEntity class: " + tileEntityClass);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to find classes", t);
            return;
        }
    }

    @Override
    public void shutdown() {
    }

    public void startLogging(long threadId) {
        this.threadId = threadId;
        timer.scheduleAtFixedRate(new Sampler(), INTERVAL, INTERVAL);
    }

    private boolean shouldLog(String className) {
        synchronized (loggable) {
            Boolean value = loggable.get(className);
            if (value != null) {
                return value;
            }

            try {
                Class<?> clazz = Class.forName(className);
                boolean shouldLog = entityClass.isAssignableFrom(clazz) ||
                        tileEntityClass.isAssignableFrom(clazz);
                loggable.put(className, shouldLog);
                return shouldLog;
            } catch (Throwable t) {
                loggable.put(className, false);
                return false;
            }
        }
    }

    private class Sampler extends TimerTask {
        @Override
        public void run() {
            synchronized (data) {
                long now = System.currentTimeMillis();
                if (now - lastRotate > ROTATE_INTERVAL) {
                    lastRotate = now;
                    bucket++;
                    usedBuckets++;
                    if (bucket >= BUCKET_SIZE) {
                        bucket = 0;
                    }
                    if (usedBuckets > BUCKET_SIZE) {
                        usedBuckets = BUCKET_SIZE;
                    }

                    for (int[] entries : data.values()) {
                        entries[bucket] = 0;
                    }
                }

                ThreadInfo threadInfo = threadBean.getThreadInfo(threadId, Integer.MAX_VALUE);
                if (threadInfo == null) {
                    cancel();
                    logger.info("CPU sampling stopped because getThreadInfo() == null");
                    return;
                }

                StackTraceElement[] stack = threadInfo.getStackTrace();
                if (stack == null) {
                    return;
                }

                for (StackTraceElement element : stack) {
                    String className = element.getClassName();
                    if (shouldLog(className)) {
                        int[] entry = data.get(className);
                        if (entry == null) {
                            entry = new int[BUCKET_SIZE];
                            data.put(className, entry);
                        }
                        entry[bucket] += INTERVAL;
                    }
                }
            }
        }
    }

    @Override
    public synchronized Double getAttribute(String name)
            throws AttributeNotFoundException {
        synchronized (data) {
            int[] entries = data.get(name);
            if (entries == null) {
                throw new AttributeNotFoundException("No such property: " + name);
            }

            double total = 0;
            for (int entry : entries) {
                total += entry;
            }

            return total / ROTATE_INTERVAL / BUCKET_SIZE;
        }
    }

    @Override
    public synchronized void setAttribute(Attribute attribute)
            throws InvalidAttributeValueException {
    }

    @Override
    public synchronized AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();

        synchronized (data) {
            for (String name : names) {
                int[] entries = data.get(name);
                if (entries == null) {
                    continue;
                }

                double total = 0;
                for (int entry : entries) {
                    total += entry;
                }

                total = total / ROTATE_INTERVAL / BUCKET_SIZE;

                list.add(new Attribute(name, total));
            }
        }

        return list;
    }

    @Override
    public synchronized AttributeList setAttributes(AttributeList change) {
        return new AttributeList();
    }

    @Override
    public Object invoke(String name, Object[] args, String[] sig)
            throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(name));
    }

    @Override
    public synchronized MBeanInfo getMBeanInfo() {
        synchronized (data) {
            MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[data.size()];

            int i = 0;
            for (String name : data.keySet()) {
                attrs[i] = new MBeanAttributeInfo(
                        name,
                        "java.lang.Double",
                        name, true,
                        // isReadable
                        false, // isWritable
                        false); // isIs
                i++;
            }

            MBeanOperationInfo[] opers = new MBeanOperationInfo[0];
            return new MBeanInfo(this.getClass().getName(), "Main Thread Sampling MBean",
                    attrs, null, opers, null);
        }
    }

}
