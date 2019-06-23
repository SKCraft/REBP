/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure;

import java.io.File;
import java.util.logging.Logger;

public class WorldRemover {

    private static final Logger logger = Logger.getLogger(WorldRemover.class.getCanonicalName());
    private File path;
    
    public WorldRemover(File path) {
        if (path.getName().matches("^\\$\\$.+@.+$")) {
            this.path = path;
        } else {
            if (!path.getName().matches("^@.+$")) {
                throw new RuntimeException("Sanity check failed! Can't delete this: " + path.getAbsolutePath());
            }
            String tempId = "$$" + System.currentTimeMillis() + path.getName();
            logger.info("WorldRemover: Renaming to '" + tempId + "'");
            File newPath = new File(path.getParentFile(), tempId);
            path.renameTo(newPath);
            this.path = newPath;
        }
        
        new Thread(new DirectoryRemover()).start();
    }
    
    private class DirectoryRemover implements Runnable {

        public void run() {
            if (!path.getName().matches("^\\$\\$.+@.+$")) {
                throw new RuntimeException("Sanity check failed! Bad path: " + path.getAbsolutePath());
            }
            logger.info("WorldRemover: Removing '" + path.getAbsolutePath() + "'");
            delete(path);
            logger.info("WorldRemover: Finish removing '" + path.getAbsolutePath() + "'");
        }
    }

    private void delete(File f) {
        if (!f.getPath().contains("$$")) {
            throw new RuntimeException("Sanity check failed! Bad path while deleting: " + path.getAbsolutePath());
        }
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        f.delete();
    }

}
