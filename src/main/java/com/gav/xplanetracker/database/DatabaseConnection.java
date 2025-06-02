package com.gav.xplanetracker.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String URL = "jdbc:sqlite:src/main/resources/db/sqlite/flights.db";

    public static Connection connect() {
        try {
            final Path databasePath = getDatabasePath();
            return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        } catch (SQLException e) {
            logger.error("SQLite connection failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static void setupDatabase() throws IOException {
        final Path databasePath = getDatabasePath();
        if (Files.notExists(databasePath)) {
            Files.createDirectories(databasePath.getParent());

            try (InputStream dbStream = DatabaseConnection.class.getResourceAsStream("/db/sqlite/flights.db")) {
                if (dbStream == null) {
                    throw new FileNotFoundException("Database not found in resources.");
                }
                Files.copy(dbStream, databasePath);
            }
        }
    }

    // TODO Test on Windows
    // TODO Dynamically fetch install location on Windows
    public static Path getDatabasePath() {
        final String os = System.getProperty("os.name").toLowerCase();

        if (os.toLowerCase().contains("mac")) {
            return Paths.get(System.getProperty("user.home"),
                    "Library", "Application Support", "x-plane-tracker", "flights.db");
        } else if (os.toLowerCase().contains("win")) {
            return Paths.get(System.getenv("APPDATA"),"x-plane-tracker", "flights.db");
        }

        throw new IllegalArgumentException("Operating System " + os + " Not supported.");
    }
}
