/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.command.ExecutionContext;
import com.sk89q.rebar.util.command.ProvideException;
import com.sk89q.rebar.util.command.annotation.Sender;
import com.sk89q.rebar.util.command.annotation.Text;
import com.sk89q.rebar.util.command.parametric.ArgumentStack;
import com.sk89q.rebar.util.command.parametric.Parameter;
import com.sk89q.rebar.util.command.parametric.ArgumentProvider;
import com.skcraft.rebar.Actor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CapsuleCommandModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Text.class)
                .toProvider(new ArgumentProvider<String>() {
                    @Override
                    protected String get(Actor actor, Parameter parameter, ArgumentStack args) {
                        return args.consume();
                    }
                });

        bind(Player.class)
                .annotatedWith(Sender.class)
                .toProvider(new ArgumentProvider<Player>() {
                    @Override
                    protected Player get(Actor actor, Parameter parameter, ArgumentStack args) {
                        CommandSender sender = provideCommandSender();
                        if (sender instanceof Player) {
                            return (Player) sender;
                        } else {
                            throw new ProvideException("This command can only be run by a player.");
                        }
                    }
                });
    }

    Player assumePlayer() {
        CommandSender sender = provideCommandSender();
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new ProvideException("You do not appear to be a player!");
        }
    }

    @Provides
    Actor provideActor() {
        return ExecutionContext.get().getActor();
    }

    @Provides
    CommandSender provideCommandSender() {
        return (CommandSender) ExecutionContext.get().getActor().getHandle();
    }

    @Provides
    CommandContext provideCommandContext() {
        return ExecutionContext.get().getContext();
    }

    @Provides
    String provideString() {
        return ExecutionContext.get().getArguments().next();
    }

    @Provides
    Player providePlayer() {
        try {
            return CommandUtil.matchSinglePlayer(provideCommandSender(), provideString());
        } catch (CommandException e) {
            throw new ProvideException(e.getMessage());
        }
    }

    @Provides
    Location provideLocation() {
        ArgumentStack params = ExecutionContext.get().getArguments();
        World world;
        try {
            params.peekDouble();
            world = providePlayer().getWorld();
        } catch (ProvideException e) {
            String value = params.next();
            world = Bukkit.getServer().getWorld(value);
            if (world == null) {
                throw new ProvideException("No world by the name of '" + value + "' was found.");
            }
        }
        double x = params.nextDouble();
        double y = params.nextDouble();
        double z = params.nextDouble();
        return new Location(world, x, y, z);
    }

    @Provides
    World provideWorld() {
        ArgumentStack params = ExecutionContext.get().getArguments();
        String value = params.next();
        if (value.equalsIgnoreCase("@here")) {
            return assumePlayer().getWorld();
        } else {
            World world = Bukkit.getServer().getWorld(value);
            if (world != null) {
                return world;
            } else {
                throw new ProvideException("No world by the name of '" + value + "' was found.");
            }
        }
    }

    @Provides
    Boolean provideBoolean() {
        return ExecutionContext.get().getArguments().nextBoolean();
    }

    @Provides
    Double provideDouble() {
        return ExecutionContext.get().getArguments().nextDouble();
    }

    @Provides
    Float provideFloat() {
        Double value = ExecutionContext.get().getArguments().nextDouble();
        if (value != null) {
            return (float) (double) value;
        } else {
            return null;
        }
    }

}
