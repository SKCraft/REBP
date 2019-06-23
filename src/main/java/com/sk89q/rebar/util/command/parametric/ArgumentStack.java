/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.sk89q.rebar.util.command.ProvideException;

public interface ArgumentStack {

    String next() throws ProvideException;

    boolean nextBoolean() throws ProvideException;

    int nextInt() throws ProvideException;

    double nextDouble() throws ProvideException;

    String peek() throws ProvideException;

    boolean peekBoolean() throws ProvideException;

    int peekInt() throws ProvideException;

    double peekDouble() throws ProvideException;

    String consume() throws ProvideException;

    boolean hasNext();

}
