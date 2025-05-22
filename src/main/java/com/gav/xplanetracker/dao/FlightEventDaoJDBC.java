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
        final String SQL = "INSERT INTO flight_event (created_at, pressure_altitude, latitude, longitude, ground_speed) \n" +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, event.getCreatedAt().toString());
            ps.setDouble(2, event.getPressureAltitude());
            ps.setDouble(3, event.getLatitude());
            ps.setDouble(4, event.getLongitude());
            ps.setDouble(5, event.getGroundSpeed());

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
