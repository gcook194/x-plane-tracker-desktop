package com.gav.xplanetracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:src/main/resources/db/sqlite/flights.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("SQLite connection failed: " + e.getMessage());
            return null;
        }
    }
}
