package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.enums.FlightStatus;
import com.gav.xplanetracker.model.Flight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FlightDaoJDBC {

    private static FlightDaoJDBC INSTANCE;

    public static FlightDaoJDBC getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightDaoJDBC();
        }
        return INSTANCE;
    }

    public void create(Flight flight) {
        final String SQL = "INSERT INTO flight (\n" +
                "    created_at,\n" +
                "    started_at,\n" +
                "    user_id,\n" +
                "    aircraft_reg,\n" +
                "    aircraft_type,\n" +
                "    arrival_airport_icao,\n" +
                "    departure_airport_icao,\n" +
                "    flight_number_icao,\n" +
                "    status\n" +
                ") VALUES (\n" +
                "    ?, -- created_at\n" +
                "    ?, -- started_at\n" +
                "    ?, -- user_id (UUID as TEXT)\n" +
                "    ?, -- aircraft_reg\n" +
                "    ?, -- aircraft_type\n" +
                "    ?, -- arrival_airport_icao\n" +
                "    ?, -- departure_airport_icao (typo in original, assuming \"departure\")\n" +
                "    ?, -- flight_number_icao\n" +
                "    ?  -- status\n" +
                ")";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, flight.getCreatedAt().toString());
            ps.setString(2, flight.getStartedAt().toString());
            ps.setString(2, flight.getUserId().toString());
            ps.setString(4, flight.getAircraftReg());
            ps.setString(5, flight.getAircraftTypeIcao());
            ps.setString(6, flight.getArrivalAirportIcao());
            ps.setString(7, flight.getDepartureAirportIcao());
            ps.setString(8, flight.getFlightNumberIcao());
            ps.setString(9, flight.getStatus().toString());

            final int rowsInserted = ps.executeUpdate();

            if (rowsInserted == 0) {
                System.out.println(this.getClass().getName() + ": Nothing inserted - check query config");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Flight getFlightByStatus(FlightStatus flightStatus) {
        final String SQL = "SELECT * FROM flight WHERE status = ?";

        return null;
    }
}
