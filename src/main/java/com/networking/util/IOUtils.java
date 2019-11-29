package com.networking.util;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

    public static void close(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            try {
                for (Closeable closeable : closeables) {
                    if (closeable != null) {
                        closeable.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
