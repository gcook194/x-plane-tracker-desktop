package com.gav.xplanetracker.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
            String userDir = System.getProperty("user.dir");
            String dbPath = userDir + File.separator + "db" + File.separator + "sqlite" + File.separator + "flights.db";
            System.out.println(dbPath);

            return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("SQLite connection failed: " + e.getMessage(), e);
            return null;
        }
    }
}
