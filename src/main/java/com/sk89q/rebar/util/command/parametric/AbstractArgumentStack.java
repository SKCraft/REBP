/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.sk89q.rebar.util.command.ProvideException;

abstract class AbstractArgumentStack implements ArgumentStack {

    @Override
    public boolean nextBoolean() throws ProvideException {
        String value = next();
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y");
    }

    @Override
    public int nextInt() throws ProvideException {
        String value = next();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ProvideException("Wanted a number, but got '" + value + "'");
        }
    }

    @Override
    public double nextDouble() throws ProvideException {
        String value = next();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ProvideException("Wanted a number, but got '" + value + "'");
        }
    }

    @Override
    public boolean peekBoolean() throws ProvideException {
        String value = peek();
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y");
    }

    @Override
    public int peekInt() throws ProvideException {
        String value = peek();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ProvideException("Wanted a number, but got '" + value + "'");
        }
    }

    @Override
    public double peekDouble() throws ProvideException {
        String value = peek();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ProvideException("Wanted a number, but got '" + value + "'");
        }
    }

}
