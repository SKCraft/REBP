/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.management;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

public class ManagementUtils {

    private static final Logger logger = Logger.getLogger(ManagementUtils.class
            .getCanonicalName());

    private ManagementUtils() {
    }

    public static void register(Object obj, String name) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName n= new ObjectName(name);
            mbs.registerMBean(obj, n);
        } catch (MalformedObjectNameException e) {
            logger.log(Level.WARNING, "Invalid MBean name: " + name, e);
        } catch (InstanceAlreadyExistsException e) {
        } catch (MBeanRegistrationException e) {
            logger.log(Level.WARNING, "Failed to register MBean: " + name, e);
        } catch (NotCompliantMBeanException e) {
            logger.log(Level.WARNING, "Non-compliant MBean: " + name, e);
        }
    }

}
