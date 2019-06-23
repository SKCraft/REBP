/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionParser {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern classicColors = Pattern.compile("&[A-Fa-z0-9klmnor]");

    public String format(Context context, String string) {
        Matcher m = classicColors.matcher(string);
        string = m.replaceAll(COLOR_CHAR + "$1");
        return string;
    }

}
