/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.command.ExecutionContext;
import com.sk89q.rebar.util.command.ProvideException;
import com.skcraft.rebar.Actor;
import lombok.extern.java.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static com.sk89q.rebar.util.command.ExecutionContext.createContext;

@Log
public class ParameterProvider {

    private final Injector injector;

    public ParameterProvider(Injector injector) {
        this.injector = injector;
    }

    public Object[] provide(Actor actor, CommandContext context, Parameter[] parameters) throws CommandException {
        CommandContextStack args = new CommandContextStack(context);

        try (ExecutionContext c = createContext()) {
            Set<Character> usedFlags = new HashSet<>();
            c.setActor(actor);
            c.setContext(context);

            List<Object> invokeArgs = new ArrayList<>();
            for (Parameter parameter : parameters) {
                c.setArguments(parameter.transform(args));
                c.setCurrentParameter(parameter);
                provide(c, invokeArgs, parameter, false);
                usedFlags.add(parameter.getFlag()); // Keep track of flags
            }

            // Check for number of arguments
            if (args.hasNext()) {
                throw new CommandException("You've given too many arguments.");
            }

            for (char flag : context.getFlags()) {
                if (!usedFlags.contains(flag)) {
                    throw new CommandException("Unknown flag: " + flag);
                }
            }

            return invokeArgs.<Object[]>toArray();
        }
    }

    @SuppressWarnings("unchecked")
    private void provide(ExecutionContext c, List<Object> invokeArgs, Parameter parameter, boolean isSubstitution) throws CommandException {
        Annotation annotation = parameter.getBindingAnnotation();
        Key<Object> key = (Key<Object>)
                (annotation != null ? Key.get(parameter.getType(), annotation) : Key.get(parameter.getType()));

        try {
            invokeArgs.add(injector.getInstance(key));

        // The injector does not know how to inject this type of parameter
        } catch (ConfigurationException e) {
            log.log(Level.WARNING, "Not sure how to get a parameter: " + parameter, e);
            throw new CommandException("The command could not be run due to an internal server error.");

        // Injection failed
        } catch (ProvisionException e) {
            Throwable cause = e.getCause();

            // Failed because the arguments provided are wrong
            if (e.getCause() instanceof ProvideException) {
                if (isSubstitution) {
                    throw new CommandException("You didn't specify a parameter, so one was assumed, but this happened:\n-> " +
                            e.getCause().getMessage() + "\nSpecifying the missing parameter may fix it.");
                } else {
                    throw new CommandException(e.getCause().getMessage());
                }

            // Failed because the @Default given is wrong
            } else if (isSubstitution) {
                log.log(Level.WARNING, "Incorrect @Default value for: " + parameter, e);
                throw new CommandException("The command could not be run due to an internal server error.");

            // Failed because the argument stack is empty
            } else if (cause instanceof IndexOutOfBoundsException) {
                if (parameter.getDefaultValue() != null) {
                    c.setArguments(new CommandContextStack(new CommandContext("dummy " + parameter.getDefaultValue())));
                    provide(c, invokeArgs, parameter, true);
                } else if (parameter.isNullable()) {
                    invokeArgs.add(null);
                } else {
                    throw new CommandException("You've not given enough parameters.");
                }

            // Failed due to Java exception
            } else {
                log.log(Level.WARNING, "Failed to get parameters", e.getCause());
                throw new CommandException("The command could not be run due to an internal server error.");
            }
        }
    }

    public static Parameter[] parseParameters(Type[] types, Annotation[][] annotations) {
        Parameter[] parameters = new Parameter[types.length];
        for (int i = 0; i < types.length; i++) {
            parameters[i] = new Parameter(types[i], annotations[i]);
        }
        return parameters;
    }

    public static Parameter[] parseParameters(Method method) {
        return parseParameters(method.getGenericParameterTypes(), method.getParameterAnnotations());
    }

}
