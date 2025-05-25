package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FlightEventDaoJDBC {

    private static final Logger logger = LoggerFactory.getLogger(FlightEventDaoJDBC.class);

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
                logger.warn("Nothing inserted - check query config");
            }
        } catch (SQLException e) {
            logger.error("Error while creating flight event: ", e);
            throw new RuntimeException(e);
        }
    }

    public List<FlightEvent> getFlightEvents(Flight flight) {
        final String SQL = "SELECT * FROM flight_event WHERE flight_id = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setLong(1, flight.getId());

            final ResultSet rs = ps.executeQuery();
            final List<FlightEvent> events = new ArrayList<>();

            while(rs.next()) {
                final FlightEvent event = new FlightEvent();
                event.setId(rs.getLong("id"));
                event.setFlightId(rs.getLong("flight_id"));
                event.setCreatedAt(Instant.parse(rs.getString("created_at")));
                event.setGroundSpeed(rs.getDouble("ground_speed"));
                event.setLatitude(rs.getDouble("latitude"));
                event.setLongitude(rs.getDouble("longitude"));
                event.setPressureAltitude(rs.getDouble("pressure_altitude"));

                events.add(event);
            }

            return events;
        } catch (SQLException e) {
            logger.error("Error while fetching flight events: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
