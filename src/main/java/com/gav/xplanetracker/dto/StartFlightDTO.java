package com.gav.xplanetracker.dto;

import com.gav.xplanetracker.enums.FlightStatus;

import java.util.UUID;

public record StartFlightDTO(UUID userId, String flightNumberIcao, String departureAirportIcao, String arrivalAirportIcao, FlightStatus status, String aircraftTypeIcao, String aircraftReg) {
}
