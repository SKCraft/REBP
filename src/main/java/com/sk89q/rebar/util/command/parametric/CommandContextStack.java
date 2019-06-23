/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.rebar.util.command.ProvideException;

public class CommandContextStack extends AbstractArgumentStack {

    private final CommandContext context;
    private int index = 0;

    public CommandContextStack(CommandContext context) {
        this.context = context;
    }

    @Override
    public String next() {
        return context.getString(index++);
    }

    @Override
    public String peek() throws ProvideException {
        try {
            return context.getString(index);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public String consume() {
        if (index >= context.argsLength()) {
            throw new IndexOutOfBoundsException();
        }
        String s = Joiner.on(" ").join(context.getParsedPaddedSlice(index, 0));
        index = context.argsLength();
        return s;
    }

    @Override
    public boolean hasNext() {
        return index < context.argsLength();
    }

    ArgumentStack forValueFlag(char flag) {
        return new ValueFlag(flag);
    }

    ArgumentStack forFlag(char flag) {
        return new BooleanFlag(flag);
    }

    class BooleanFlag extends AbstractArgumentStack {
        private final char flag;
        private boolean hasNext = true;

        public BooleanFlag(char flag) {
            this.flag = flag;
        }

        @Override
        public String next() throws ProvideException {
            if (hasNext) {
                hasNext = false;
                return context.hasFlag(flag) ? "true" : "false";
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public String peek() throws ProvideException {
            if (hasNext) {
                hasNext = false;
                return context.hasFlag(flag) ? "true" : "false";
            } else {
                return "";
            }
        }

        @Override
        public String consume() throws ProvideException {
            return next();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }
    }

    class ValueFlag extends AbstractArgumentStack {
        private final char flag;
        private boolean hasNext = true;

        public ValueFlag(char flag) {
            this.flag = flag;
        }

        @Override
        public String next() throws ProvideException {
            if (hasNext) {
                hasNext = false;
                return context.getFlag(flag);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public String peek() throws ProvideException {
            if (hasNext) {
                return context.getFlag(flag);
            } else {
                return "";
            }
        }

        @Override
        public String consume() throws ProvideException {
            return next();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }
    }

}
