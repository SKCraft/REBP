/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.util;

public class ImageRequestManager {
    
    private int maxBusy = 3;
    private int busyCount = 0;
    
    public boolean request(ImageRequest request) throws BusyException {
        synchronized(this) {
            if (busyCount >= maxBusy) {
                throw new BusyException();
            } else {
                busyCount++;
            }
        }
        
        Thread thread = new Thread(new RequestWrapper(request));
        thread.start();
        
        return true;
    }
    
    private synchronized void reduceBusy() {
        busyCount--;
    }
    
    public static class BusyException extends Exception {
        private static final long serialVersionUID = -3435008216043668082L;
    }
    
    private class RequestWrapper implements Runnable {
        private ImageRequest request;
        
        public RequestWrapper(ImageRequest runnable) {
            this.request = runnable;
        }
        
        public void run() {
            try {
                request.run();
            } finally {
                reduceBusy();
            }
        }
    }
}
