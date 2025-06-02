package com.gav.xplanetracker.database;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseMigration {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigration.class);

    public static void runMigrations(String dbPath) {
        logger.info("Migrating DB");
        final Flyway flyway = Flyway.configure()
                .dataSource("jdbc:sqlite:" + dbPath, null, null)
                .load();

        flyway.migrate();
    }
}
