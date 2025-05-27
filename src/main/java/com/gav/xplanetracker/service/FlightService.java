package com.gav.xplanetracker.service;

import com.gav.xplanetracker.dao.FlightDaoJDBC;
import com.gav.xplanetracker.dao.FlightEventDaoJDBC;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightStatus;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);

    private static FlightService INSTANCE;

    private final FlightDaoJDBC flightDao;
    private final FlightEventDaoJDBC flightEventDao;

    public static FlightService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightService();
        }
        return INSTANCE;
    }

    public FlightService() {
        this.flightDao = FlightDaoJDBC.getInstance();
        this.flightEventDao = FlightEventDaoJDBC.getInstance();
    }

    public Flight getOrCreateCurrentFlight(final NavigraphFlightPlan navigraphFlightPlan, final String aircraftReg) {
        return getActiveFlight()
                .orElseGet(() -> startFlight(navigraphFlightPlan, aircraftReg));
    }

    public Flight startFlight(final NavigraphFlightPlan navigraphFlightPlan, String aircraftReg) {
        logger.info("Starting flight");

        // TODO look at lombok, mapstruct or create a builder class
        final Flight flight = new Flight();
        flight.setStartedAt(Instant.now());
        flight.setFlightNumberIcao(navigraphFlightPlan.getIcaoAirline() + navigraphFlightPlan.getFlightNumber());
        flight.setDepartureAirportIcao(navigraphFlightPlan.getDeparture().getIcaoCode());
        flight.setArrivalAirportIcao(navigraphFlightPlan.getArrival().getIcaoCode());
        flight.setAircraftTypeIcao(navigraphFlightPlan.getAircraftType());
        flight.setCreatedAt(Instant.now());
        flight.setStartedAt(Instant.now());
        flight.setStatus(FlightStatus.IN_PROGRESS);
        flight.setUserId(UUID.randomUUID()); //TODO eventually needs to be a real value

        if (aircraftReg != null) {
            flight.setAircraftReg(aircraftReg);
        } else {
            // This is a fallback and isn't necessarily reliable because I am lazy
            flight.setAircraftReg(navigraphFlightPlan.getAircraftRegistration());
        }

        flightDao.create(flight);

        // TODO send message to aws

        return flight;
    }

    public Optional<Flight> getActiveFlight() {
        return Optional.ofNullable(flightDao.getFlightByStatus(FlightStatus.IN_PROGRESS));
    }

    public void completeActiveFlight() {
        getActiveFlight().ifPresent(this::completeFlight);
    }

    private void completeFlight(Flight flight) {
        flightDao.completeFlight(flight);
    }

    public List<FlightEvent> getFlightEvents(Flight flight) {
        return flightEventDao.getFlightEvents(flight);
    }
}
