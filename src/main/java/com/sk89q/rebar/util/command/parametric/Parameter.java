/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.sk89q.rebar.util.command.ProvideException;
import com.sk89q.rebar.util.command.annotation.Default;
import com.sk89q.rebar.util.command.annotation.Flag;
import lombok.Data;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Data
public class Parameter {

    private final Type type;
    private Annotation bindingAnnotation;
    private boolean nullable;
    private String defaultValue;
    private Flag flagAnnotation;

    public Parameter(Type type, Annotation[] annotations) {
        this.type = type;

        for (Annotation annotation : annotations) {
            if (annotation instanceof Nullable) {
                nullable = true;
            } else if (annotation instanceof Flag) {
                flagAnnotation = (Flag) annotation;
                nullable = true;
            } else if (annotation instanceof Default) {
                defaultValue = ((Default) annotation).value();
            } else if (this.bindingAnnotation == null) {
                this.bindingAnnotation = annotation;
            }
        }
    }

    public Character getFlag() {
        try {
            return flagAnnotation != null ? flagAnnotation.value().charAt(0) : null;
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    public boolean isFlag() {
        return flagAnnotation != null;
    }

    public boolean isValueFlag() {
        return flagAnnotation != null && flagAnnotation.value().matches("^.:$");
    }

    public ArgumentStack transform(CommandContextStack rootStack) {
        if (flagAnnotation != null) {
            String value = flagAnnotation.value();
            if (value.length() < 1) {
                throw new ProvideException("Invalid @Flag annotation");
            } else if (value.matches("^.:$")) {
                return rootStack.forValueFlag(value.charAt(0));
            } else {
                return rootStack.forFlag(value.charAt(0));
            }
        }
        return rootStack;
    }

}
