package com.gav.xplanetracker.database;

import org.flywaydb.core.Flyway;

public class DatabaseMigration {
    public static void runMigrations(String dbPath) {
        System.out.println("Migrating DB");
        final Flyway flyway = Flyway.configure()
                .dataSource("jdbc:sqlite:" + dbPath, null, null)
                .load();

        flyway.migrate();
    }
}
