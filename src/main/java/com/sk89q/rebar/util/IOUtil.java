/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class IOUtil {

    private IOUtil() {
    }

    public static boolean close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    public static void append(File file, String text) throws IOException {
        FileWriter writer = null;
        BufferedWriter out = null;

        try {
            writer = new FileWriter(file, true);
            out = new BufferedWriter(writer);
            out.write(text);
            out.flush();
        } finally {
            close(writer);
            close(out);
        }
    }

}
