package com.gav.xplanetracker.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static final String URL = "jdbc:sqlite:src/main/resources/db/sqlite/flights.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            logger.error("SQLite connection failed: " + e.getMessage(), e);
            return null;
        }
    }
}
