ALTER TABLE flight
    ADD
        simulator TEXT NOT NULL DEFAULT "X_Plane_12";

ALTER TABLE flight_event
    ADD heading REAL;