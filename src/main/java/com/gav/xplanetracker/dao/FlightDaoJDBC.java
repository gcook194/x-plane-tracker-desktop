package com.gav.xplanetracker.dao;

import com.gav.xplanetracker.database.DatabaseConnection;
import com.gav.xplanetracker.enums.FlightStatus;
import com.gav.xplanetracker.model.Flight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlightDaoJDBC {

    private static final Logger logger = LoggerFactory.getLogger(FlightDaoJDBC.class);

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
                "    status, \n" +
                "    navigraph_json \n" +
                ") VALUES (\n" +
                "    ?, -- created_at\n" +
                "    ?, -- started_at\n" +
                "    ?, -- user_id (UUID as TEXT)\n" +
                "    ?, -- aircraft_reg\n" +
                "    ?, -- aircraft_type\n" +
                "    ?, -- arrival_airport_icao\n" +
                "    ?, -- departure_airport_icao\n" +
                "    ?, -- flight_number_icao\n" +
                "    ?, -- status\n" +
                "    ?  -- navigraph flight plan json\n" +
                ")";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, flight.getCreatedAt().toString());
            ps.setString(2, flight.getStartedAt().toString());
            ps.setString(3, flight.getUserId().toString());
            ps.setString(4, flight.getAircraftReg());
            ps.setString(5, flight.getAircraftTypeIcao());
            ps.setString(6, flight.getArrivalAirportIcao());
            ps.setString(7, flight.getDepartureAirportIcao());
            ps.setString(8, flight.getFlightNumberIcao());
            ps.setString(9, flight.getStatus().toString());
            ps.setString(10, flight.getNavigraphJson());

            final int rowsInserted = ps.executeUpdate();

            if (rowsInserted == 0) {
                logger.warn("Nothing inserted - check query config");
            }
        } catch (SQLException e) {
            logger.error("Error while creating flight: ", e);
            throw new RuntimeException(e);
        }
    }

    public Flight getFlightByStatus(FlightStatus flightStatus) {
        final String SQL = "SELECT * FROM flight WHERE status = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            final List<Flight> flights = new ArrayList<>();

            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, flightStatus.name());

            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                final Flight flight = new Flight();
                flight.setId(rs.getInt("id"));
                flight.setUserId(UUID.fromString(rs.getString("user_id")));

                //TODO probably need null checks on these date operations
                flight.setCreatedAt(Instant.parse(rs.getString("created_at")));
                flight.setStartedAt(Instant.parse(rs.getString("started_at")));
//                flight.setCompletedAt(Instant.parse(rs.getString("completed_at")));
//                flight.setCancelledAt(Instant.parse(rs.getString("cancelled_at")));

                flight.setFlightNumberIcao(rs.getString("flight_number_icao"));
                flight.setAircraftTypeIcao(rs.getString("aircraft_type"));
                flight.setAircraftReg(rs.getString("aircraft_reg"));
                flight.setDepartureAirportIcao(rs.getString("departure_airport_icao"));
                flight.setArrivalAirportIcao(rs.getString("arrival_airport_icao"));
                flight.setStatus(FlightStatus.IN_PROGRESS); // TODO update enum to get name by string
                flight.setNavigraphJson(rs.getString("navigraph_json"));

                flights.add(flight);
            }

            if (flights.isEmpty()) {
                return null;
            }

            if (flights.size() > 1) {
                logger.warn("More than 1 flight returned - look into query");
            }

            return flights.getFirst();
        } catch (SQLException e) {
            logger.error("Error while getting flight by status: ", e);
            throw new RuntimeException(e);
        }
    }

    public void completeFlight(Flight flight) {
        final String SQL = "UPDATE flight SET status = ?, completed_at = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, FlightStatus.COMPLETED.name());
            ps.setString(2, Instant.now().toString());
            ps.setLong(3, flight.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error while stopping flight: ", e);
        }
    }

    public void cancelFlight(Flight flight) {
        final String SQL = "UPDATE flight SET status = ?, cancelled_at = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect()) {
            final PreparedStatement ps = connection.prepareStatement(SQL);
            ps.setString(1, FlightStatus.CANCELLED.name());
            ps.setString(2, Instant.now().toString());
            ps.setLong(3, flight.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error while cancelling flight: ", e);
        }
    }
}
