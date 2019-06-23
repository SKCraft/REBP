/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.sk89q.rebar.LoaderException;
import com.sk89q.rebar.Rebar;

public class ApplicationManager {
    
    private MapPad mapPad;
    public Map<String, Class<? extends Application>> apps =
            new HashMap<String, Class<? extends Application>>();
    
    public ApplicationManager(MapPad mapPad) {
        this.mapPad = mapPad;
    }
    
    public void register(String name, Class<? extends Application> appClass) {
        apps.put(name.toLowerCase(), appClass);
    }
    
    public Collection<String> getAppNames() {
        return apps.keySet();
    }
    
    public boolean hasApp(String name) {
        return apps.containsKey(name.toLowerCase());
    }
    
    public Application create(String name, Player player) throws ApplicationException {
        Class<? extends Application> appClass = apps.get(name.toLowerCase());
        if (appClass == null) {
            return null;
        }
        return create(appClass, player);
    }
    
    public Application create(Class<? extends Application> appClass, Player player) throws ApplicationException {
        try {
            Constructor<? extends Application> constr = appClass.getConstructor(MapPad.class, Player.class);
            Application app = constr.newInstance(mapPad, player);
            Rebar.getInstance().getLoader().processHelpers(app);
            return app;
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(e);
        } catch (InstantiationException e) {
            throw new ApplicationException(e);
        } catch (IllegalAccessException e) {
            throw new ApplicationException(e);
        } catch (InvocationTargetException e) {
            throw new ApplicationException(e);
        } catch (SecurityException e) {
            throw new ApplicationException(e);
        } catch (NoSuchMethodException e) {
            throw new ApplicationException(e);
        } catch (LoaderException e) {
            throw new ApplicationException(e);
        }
    }
    
}
