/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Emote {

    private List<Macro> macrosList = new ArrayList<Macro>();

    @XmlElementWrapper(name = "macros")
    @XmlAnyElement(lax = true)
    public List<Macro> getMacrosList() {
        return macrosList;
    }

    public void setMacrosList(List<Macro> macrosList) {
        this.macrosList = macrosList;
    }

}
