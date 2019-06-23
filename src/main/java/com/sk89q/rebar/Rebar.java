/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.google.inject.Guice;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.Injector;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.rebar.capsule.CapsuleCommandModule;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.declarative.ConfigurationInjector;
import com.sk89q.rebar.event.Event;
import com.sk89q.rebar.event.EventManager;
import com.sk89q.rebar.event.RegisteredEvent;
import com.sk89q.rebar.service.ServiceManager;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandsArgumentInjector;
import com.sk89q.rebar.util.RebarCommandsManager;
import com.sk89q.rebar.util.ReflectionUtil;
import com.sk89q.rebar.util.command.parametric.ParameterProvider;
import com.sk89q.squirrelid.resolver.HttpRepositoryService;
import com.sk89q.squirrelid.resolver.ProfileService;
import com.sk89q.wepif.PermissionsProvider;
import com.sk89q.wepif.PermissionsResolverManager;
import com.skcraft.rebar.Actor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Rebar extends JavaPlugin implements PermissionsProvider {
    private static final Logger logger = Logger.getLogger(Rebar.class.getCanonicalName());
    private static Rebar instance;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ComponentLoader loader = new ComponentLoader();
    private final EventManager eventManager = new EventManager();
    private final ServiceManager serviceManager = new ServiceManager();
    private CommandsManager<CommandSender> commandsManager;
    private ConfigurationInjector configInjector;
    private EbeanServer ebean;
    private Configuration config;
    private final com.google.inject.Injector commandInjector = Guice.createInjector(new CapsuleCommandModule());
    private final ParameterProvider parameterProvider = new ParameterProvider(commandInjector);
    private final ProfileService nameResolver = HttpRepositoryService.forMinecraft();

    public static Rebar getInstance() {
        return instance;
    }

    public static Server server() {
        return instance.getServer();
    }

    public static String serverId() {
        return instance.getServer().getServerId();
    }

    public static OfflinePlayer offlinePlayer(String name) {
        return instance.getServer().getOfflinePlayer(name);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ParameterProvider getParameterProvider() {
        return parameterProvider;
    }

    public ProfileService getNameResolver() {
        return nameResolver;
    }

    @Override
    public void onLoad() {
        Rebar.instance = this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        logger.info("Rebar is loading...");

        getDataFolder().mkdirs();

        config = new Configuration(new File(getDataFolder(), "config.yml"));
        try {
            config.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Rebar: Failed to load configuration");
            logger.log(Level.SEVERE, "Rebar is shutting down the server.");
            getServer().shutdown();
            return;
        }

        boolean die = config.getBoolean("die-if-unloaded", true);

        configInjector = new ConfigurationInjector(config);

        PermissionsResolverManager.initialize(this);

        commandsManager = new RebarCommandsManager();

        for (String path : config.getStringList("components", null)) {
            try {
                logger.info("Rebar: Loading " + path);
                Class<?> cls = Class.forName(path);
                loader.load((Class<? extends Component>) cls);
                continue;
            } catch (ClassNotFoundException e) {
                logger.severe("Rebar: Failed to load component '" + path + "'; class not found");
            } catch (LoaderException e) {
                logger.log(Level.SEVERE, "Rebar: Failed to load component '" + path + "'", e);
            }

            if (die) {
                config.save();
                logger.log(Level.SEVERE, "Rebar is shutting down the server.");
                getServer().shutdown();
                return;
            }
        }

        setupDatabase();

        //registerEvents(new CommandsBukkitListener());

        if (!loader.initialize() && die) {
            config.save();
            logger.log(Level.SEVERE, "Rebar is shutting down the server.");
            getServer().shutdown();
            return;
        }

        try {
            logger.info("Trying to register commands...");

            SimpleCommandMap commandMap = (SimpleCommandMap) ReflectionUtil.field(
                    SimplePluginManager.class, getServer().getPluginManager(), "commandMap");
            Constructor<PluginCommand> constr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constr.setAccessible(true);

            for (Map.Entry<String, Method> entry : commandsManager.getMethods().get(null).entrySet()) {
                Command newCmd = constr.newInstance(entry.getKey(), this);
                //newCmd.setDescription(entry.getValue());
                commandMap.register(getDescription().getName(), newCmd);
                //logger.info("Rebar: Installed command " + entry.getKey());

                /*CommandDefinition def = new CommandDefinition(entry.getKey(), entry.getValue());
                logger.info("Rebar: Registered command " + entry.getKey());
                getServer().getPluginManager().registerCommand(def, this);*/
            }
        } catch (IllegalArgumentException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (NoSuchFieldException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        }

        config.save();

        logger.info("Rebar has successfully loaded!");
    }

    @Override
    public void onDisable() {
        loader.shutdown();
    }

    private void setupDatabase() {
        ServerConfig db = new ServerConfig();

        db.setDefaultServer(false);
        db.setRegister(false);
        db.setClasses(loader.getEntities());
        db.setName(getDescription().getName());
        getServer().configureDbConfig(db);

        DataSourceConfig ds = db.getDataSourceConfig();

        ds.setUrl(ds.getUrl());
        getDataFolder().mkdirs();

        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        ebean = EbeanServerFactory.create(db);
        Thread.currentThread().setContextClassLoader(previous);

        /*SpiEbeanServer serv = (SpiEbeanServer) ebean;
        DdlGenerator gen = serv.getDdlGenerator();
        gen.runScript(false, gen.generateCreateDdl());*/
    }

    @SuppressWarnings("unchecked")
    public <T> void registerEvents(T listener) {
        Class<?> cls = listener.getClass();

        for (Method method : cls.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation instanceof RegisteredEvent) {
                    RegisteredEvent registration = (RegisteredEvent) annotation;
                    registerRebarEvent((Class<? extends Event<T>>) registration.type(),
                            listener, registration.priority());
                }
            }
        }

        if (listener instanceof Listener) {
            getServer().getPluginManager().registerEvents((Listener) listener, this);
        }
    }

    private <T> void registerRebarEvent(Class<? extends Event<T>> type, T listener, short priority) {
        getEventManager().register(type, listener, priority);
    }

    public void registerCommands(Class<?> cls) {
        commandsManager.register(cls);
    }

    public void registerCommands(Class<?> cls, Object ... args) {
        Injector injector = new CommandsArgumentInjector(args);
        commandsManager.setInjector(injector);
        commandsManager.register(cls);
        commandsManager.setInjector(null);
    }

    public void populateConfig(Object obj) {
        configInjector.inject(obj);
    }

    public ComponentLoader getLoader() {
        return loader;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public EbeanServer getDatabase() {
        return ebean;
    }

    public Configuration getRebarConfiguration() {
        return config;
    }

    public CommandsManager<CommandSender> getCommandsManager() {
        return commandsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            Rebar.getInstance().getCommandsManager().execute(command.getName(), args, sender, sender);
        } catch (NumberFormatException e) {
            ChatUtil.error(sender, "The command expected you to enter a number but instead you entered words.");
        } catch (CommandPermissionsException e) {
            ChatUtil.error(sender, "You do not have the sufficient permission to do this.");
        } catch (MissingNestedCommandException e) {
            ChatUtil.error(sender, e.getUsage());
        } catch (CommandUsageException e) {
            ChatUtil.error(sender, e.getMessage());
            ChatUtil.error(sender, e.getUsage());
        } catch (WrappedCommandException e) {
            ChatUtil.error(sender, "An error occurred while processing the command: " + e.getMessage());
            e.printStackTrace();
        } catch (UnhandledCommandException e) {
            return false;
        } catch (CommandException e) {
            if (e.getMessage() != null)
                ChatUtil.error(sender, e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command,
            String alias, String[] args) {
        List<String> results = new ArrayList<String>();
        results.add("testing");
        return results;
    }

    public boolean hasPermission(Player sender, String perm) {
        return hasPermission(sender, sender.getWorld(), perm);
    }

    public boolean hasPermission(CommandSender sender, String perm) {
        if (!(sender instanceof Player)) {
            return true;
        }

        return hasPermission(sender, ((Player) sender).getWorld(), perm);
    }

    public boolean hasPermission(Actor actor, String perm) {
        return hasPermission((CommandSender) actor.getHandle(), perm);
    }

    public boolean hasPermission(CommandSender sender, World world, String perm) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (sender.isOp()) {
            return true;
        }

        // Invoke the permissions resolver
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
            if (provider == null) return false;
            return provider.hasPermission(world.getName(), player.getName(), perm);
        }

        return false;
    }

    public void checkPermission(CommandSender sender, String perm) throws CommandPermissionsException {
        if (!hasPermission(sender, perm)) {
            throw new CommandPermissionsException();
        }
    }

    public void checkPermission(CommandSender sender, World world, String perm)throws CommandPermissionsException {
        if (!hasPermission(sender, world, perm)) {
            throw new CommandPermissionsException();
        }
    }

    public void reload() {
        try {
            configInjector.reload();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Rebar: Failed to reload configuration");
        }
        loader.reload();
    }

    public int registerInterval(Runnable task, int delay, int period) {
        return getServer().getScheduler().scheduleSyncRepeatingTask(this, task, delay, period);
    }

    @SuppressWarnings("deprecation")
    public int registerThreadedInterval(Runnable task, int delay, int period) {
        return getServer().getScheduler().scheduleAsyncRepeatingTask(this, task, delay, period);
    }

    public int registerTimeout(Runnable task, int delay) {
        return getServer().getScheduler().scheduleSyncDelayedTask(this, task, delay);
    }

    @SuppressWarnings("deprecation")
    public int registerThreadedTimeout(Runnable task, int delay) {
        return getServer().getScheduler().scheduleAsyncDelayedTask(this, task, delay);
    }

    public void cancelTask(int taskId) {
        getServer().getScheduler().cancelTask(taskId);
    }

    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player,
            String permission) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.hasPermission(worldName, player, permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.inGroup(player, group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return new String[0];
        return provider.getGroups(player);
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name,
            String permission) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.hasPermission(worldName, name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return false;
        return provider.inGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        PermissionsProvider provider = getServiceManager().load(PermissionsProvider.class);
        if (provider == null) return new String[0];
        return provider.getGroups(player);
    }

}
