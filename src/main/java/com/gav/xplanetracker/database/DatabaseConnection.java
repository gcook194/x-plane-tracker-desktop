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

    //TODO this works for mac but not windows
    private static final Path DB_PATH = Paths.get(System.getProperty("user.home"),
            "Library", "Application Support", "x-plane-tracker", "flights.db");

    public static Connection connect() {
        try {
            final String os = System.getProperty("os.name").toLowerCase();

            return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("SQLite connection failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static void setupDatabase() throws IOException {
        if (Files.notExists(DB_PATH)) {
            Files.createDirectories(DB_PATH.getParent());

            try (InputStream dbStream = DatabaseConnection.class.getResourceAsStream("/db/sqlite/flights.db")) {
                if (dbStream == null) {
                    throw new FileNotFoundException("Database not found in resources.");
                }
                Files.copy(dbStream, DB_PATH);
            }
        }
    }
}
