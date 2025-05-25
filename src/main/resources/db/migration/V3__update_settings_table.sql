ALTER TABLE settings
    ADD x_plane_host TEXT NOT NULL DEFAULT 'http://localhost:8086/api/v2/datarefs';

--UPDATE settings
--    SET x_plane_host = 'http://localhost:8086/api/v2/datarefs';