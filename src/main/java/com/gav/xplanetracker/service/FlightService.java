package com.gav.xplanetracker.service;

import com.gav.xplanetracker.dao.FlightDaoJDBC;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightStatus;
import com.gav.xplanetracker.model.Flight;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class FlightService {

    private static FlightService INSTANCE;

    private final FlightDaoJDBC flightDao;

    public FlightService() {
        this.flightDao = FlightDaoJDBC.getInstance();
    }

    public static FlightService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightService();
        }
        return INSTANCE;
    }

    public Flight getOrCreateCurrentFlight(final NavigraphFlightPlan navigraphFlightPlan) {
        return this.getCurrentFlight()
                .orElse(startFlight(navigraphFlightPlan));
    }

    public Flight startFlight(final NavigraphFlightPlan navigraphFlightPlan) {
        System.out.println("Starting Flight");

        // TODO look at lombok, mapstruct or create a builder class
        final Flight flight = new Flight();
        flight.setStartedAt(Instant.now());
        flight.setFlightNumberIcao(navigraphFlightPlan.getIcaoAirline() + navigraphFlightPlan.getFlightNumber());
        flight.setDepartureAirportIcao(navigraphFlightPlan.getDepartureAirport());
        flight.setArrivalAirportIcao(navigraphFlightPlan.getArrivalAirport());
        flight.setAircraftTypeIcao(navigraphFlightPlan.getAircraftType());
        flight.setAircraftReg(navigraphFlightPlan.getAircraftRegistration());
        flight.setCreatedAt(Instant.now());
        flight.setStartedAt(Instant.now());
        flight.setStatus(FlightStatus.IN_PROGRESS);
        flight.setUserId(UUID.randomUUID()); //TODO eventually needs to be a real value

        // save to db
        flightDao.create(flight);

        // send message to aws

        return flight;
    }

    public Optional<Flight> getCurrentFlight() {
        final Flight flight = flightDao.getFlightByStatus(FlightStatus.IN_PROGRESS);
        System.out.println("Current flight found so won't create a new one");

        return Optional.ofNullable(flight);
    }
}
