package com.gav.xplanetracker.service;

import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.model.Flight;

import java.time.Instant;

public final class FlightService {

    private static FlightService INSTANCE;
    private final NavigraphService navigraphService;

    public FlightService() {
        this.navigraphService = NavigraphService.getInstance();
    }

    public static FlightService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlightService();
        }
        return INSTANCE;
    }

    public Flight startFlight() {
        System.out.println("Starting Flight");

        final NavigraphFlightPlan navigraphFlightPlan = navigraphService.getFlightPlan();

        final Flight flight = new Flight();
        flight.setStartedAt(Instant.now());
        flight.setFlightNumberIcao(navigraphFlightPlan.getIcaoAirline() + navigraphFlightPlan.getFlightNumber());
        flight.setDepartureAirportIcao(navigraphFlightPlan.getDepartureAirport());
        flight.setArrivalAirportIcao(navigraphFlightPlan.getArrivalAirport());
        flight.setAircraftTypeIcao(navigraphFlightPlan.getAircraftType());
        flight.setAircraftReg(navigraphFlightPlan.getAircraftRegistration());

        // save to db

        // send message to aws

        return flight;
    }
}
