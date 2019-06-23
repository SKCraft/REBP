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

@XmlRootElement(name = "chat", namespace = "com.skcraft.galvanized")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ChatConfiguration
{

    private List<Emote> emotesList = new ArrayList<Emote>();

    @XmlElementWrapper(name = "emotes")
    @XmlAnyElement(lax = true)
    public List<Emote> getEmotesList()
    {
        return emotesList;
    }

    public void setEmotesList(List<Emote> emotes)
    {
        this.emotesList = emotes;
    }

}
