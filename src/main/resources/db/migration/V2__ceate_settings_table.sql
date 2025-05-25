CREATE TABLE settings (
    use_navigraph_api BOOLEAN NOT NULL,
    simbrief_username TEXT NOT NULL,

    CHECK (use_navigraph_api IN (0, 1))
);

-- will eventually be replaced with a UI
INSERT INTO settings (use_navigraph_api, simbrief_username)
    VALUES (1, 'Gavin194');