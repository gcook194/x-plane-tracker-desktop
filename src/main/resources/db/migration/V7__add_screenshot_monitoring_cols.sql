ALTER TABLE settings
    ADD monitor_screenshots NOT NULL DEFAULT 0 CHECK (monitor_screenshots IN (0, 1));

ALTER TABLE settings
    ADD screenshot_directory;