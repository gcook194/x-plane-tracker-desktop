package com.gav.xplanetracker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static String getApplicationDataPath() {
        final String os = System.getProperty("os.name").toLowerCase();
        logger.debug("Operating System {}", os);

        if (os.toLowerCase().contains("mac")) {
            return Paths.get(
                    System.getProperty("user.home"),
                    "Library",
                    "Application Support",
                    "x-plane-tracker"
            ).toString();
        } else if (os.toLowerCase().contains("win")) {
            return Paths.get(
                    System.getenv("APPDATA"),
                    "x-plane-tracker",
                    "flights.db"
            ).toString();
        }

        throw new IllegalStateException(String.format("Operating System %s is not supported", os));
    }
}
