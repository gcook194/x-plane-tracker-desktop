ALTER TABLE flight_event
    ADD engines_running BOOLEAN NOT NULL DEFAULT FALSE CHECK (engines_running IN (0, 1));