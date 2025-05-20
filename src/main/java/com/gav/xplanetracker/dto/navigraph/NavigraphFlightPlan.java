package com.gav.xplanetracker.dto.navigraph;

public class NavigraphFlightPlan {

    private String icaoAirline;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String aircraftType;
    private String aircraftRegistration;

    public String getIcaoAirline() {
        return icaoAirline;
    }

    public void setIcaoAirline(String icaoAirline) {
        this.icaoAirline = icaoAirline;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(String departureAirport) {
        this.departureAirport = departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(String arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftRegistration() {
        return aircraftRegistration;
    }

    public void setAircraftRegistration(String aircraftRegistration) {
        this.aircraftRegistration = aircraftRegistration;
    }
}
