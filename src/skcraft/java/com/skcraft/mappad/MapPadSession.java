/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft.mappad;

public class MapPadSession {
    
    private Application application;
    
    public MapPadSession() {
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void replaceApplication(Application application) {
        getApplication().quit();
        setApplication(application);
    }
    
}
