CREATE TABLE flight (
    cancelled_at TEXT,
    completed_at TEXT,
    created_at TEXT,
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    started_at TEXT,
    user_id TEXT,
    aircraft_reg TEXT,
    aircraft_type TEXT,
    arrival_airport_icao TEXT,
    depasture_airport_icao TEXT,
    flight_number_icao TEXT,
    status TEXT CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'ERROR'))
);

CREATE TABLE flight_event (
    ground_speed REAL,
    latitude REAL,
    longitude REAL,
    pressure_altitude REAL,
    created_at TEXT,
    flight_id INTEGER,
    id INTEGER PRIMARY KEY AUTOINCREMENT
);
