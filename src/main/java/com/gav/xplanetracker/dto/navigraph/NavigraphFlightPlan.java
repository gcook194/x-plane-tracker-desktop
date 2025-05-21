package com.gav.xplanetracker.dto.navigraph;

public class NavigraphFlightPlan {

    private String icaoAirline;
    private String flightNumber;
    private String aircraftType;
    private String aircraftRegistration;
    private Airport departure;
    private Airport arrival;

    public NavigraphFlightPlan() {
        this.departure = new Airport();
        this.arrival = new Airport();
    }

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

    public Airport getDeparture() {
        return departure;
    }

    public void setDeparture(Airport departure) {
        this.departure = departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public void setArrival(Airport arrival) {
        this.arrival = arrival;
    }
}
