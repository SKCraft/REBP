/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import com.skcraft.actionlists.Action;
import com.skcraft.actionlists.Context;

public class DenyAction implements Action<Context> {

    @Override
    public void apply(Context context) {
        context.cancel();
    }

}
