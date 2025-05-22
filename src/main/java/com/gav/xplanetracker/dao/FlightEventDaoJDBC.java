package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.model.FlightEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FlightEventDaoJDBC {

    private static FlightEventDaoJDBC INSTANCE;

    public static FlightEventDaoJDBC getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightEventDaoJDBC();
        }

        return INSTANCE;
    }

    public void create(final FlightEvent event) {
        final String SQL = "INSERT INTO flight_event (flight_id, created_at, pressure_altitude, latitude, longitude, ground_speed) \n" +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setLong(1, event.getFlightId());
            ps.setString(2, event.getCreatedAt().toString());
            ps.setDouble(3, event.getPressureAltitude());
            ps.setDouble(4, event.getLatitude());
            ps.setDouble(5, event.getLongitude());
            ps.setDouble(6, event.getGroundSpeed());

            final int rowsInserted = ps.executeUpdate();

            if (rowsInserted == 0) {
                System.out.println(this.getClass().getName() + ": Nothing inserted - check query config");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
