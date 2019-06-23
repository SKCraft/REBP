/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.sk89q.minecraft.util.commands.Injector;

public class CommandsArgumentInjector implements Injector {

    private Object[] args;
    private Class<?>[] signature;

    public CommandsArgumentInjector(Object ... args) {
        this.args = args;
        signature = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            signature[i] = args[i].getClass();
        }
    }

    @Override
    public Object getInstance(Class<?> cls) throws InvocationTargetException,
            IllegalAccessException, InstantiationException {
        Constructor<?> constr;
        try {
            constr = cls.getConstructor(signature);
        } catch (SecurityException e) {
            return null;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        return constr.newInstance(args);
    }


}
