package com.gav.xplanetracker.model;

import com.gav.xplanetracker.enums.FlightStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Flight {

    private long id;
    private UUID userId;
    private String flightNumberIcao;
    private String departureAirportIcao;
    private String arrivalAirportIcao;
    private FlightStatus status; // not started, in progress, completed, cancelled, error (x-plane not running)
    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String aircraftTypeIcao;
    private String aircraftReg;
    private String navigraphJson;

    private List<FlightEvent> events;

    public Flight() {
        this.events = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFlightNumberIcao() {
        return flightNumberIcao;
    }

    public void setFlightNumberIcao(String flightNumberIcao) {
        this.flightNumberIcao = flightNumberIcao;
    }

    public String getDepartureAirportIcao() {
        return departureAirportIcao;
    }

    public void setDepartureAirportIcao(String departureAirportIcao) {
        this.departureAirportIcao = departureAirportIcao;
    }

    public String getArrivalAirportIcao() {
        return arrivalAirportIcao;
    }

    public void setArrivalAirportIcao(String arrivalAirportIcao) {
        this.arrivalAirportIcao = arrivalAirportIcao;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getAircraftTypeIcao() {
        return aircraftTypeIcao;
    }

    public void setAircraftTypeIcao(String aircraftTypeIcao) {
        this.aircraftTypeIcao = aircraftTypeIcao;
    }

    public String getAircraftReg() {
        return aircraftReg;
    }

    public void setAircraftReg(String aircraftReg) {
        this.aircraftReg = aircraftReg;
    }

    public String getNavigraphJson() {
        return navigraphJson;
    }

    public void setNavigraphJson(String navigraphJson) {
        this.navigraphJson = navigraphJson;
    }

    public List<FlightEvent> getEvents() {
        return events;
    }

    public void setEvents(List<FlightEvent> events) {
        this.events = events;
    }

    public void addEvent(FlightEvent event) {
        this.events.add(event);
    }

    public void addEvents(List<FlightEvent> events) {
        this.events.addAll(events);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return id == flight.id && Objects.equals(userId, flight.userId) && Objects.equals(flightNumberIcao, flight.flightNumberIcao) && Objects.equals(departureAirportIcao, flight.departureAirportIcao) && Objects.equals(arrivalAirportIcao, flight.arrivalAirportIcao) && status == flight.status && Objects.equals(createdAt, flight.createdAt) && Objects.equals(startedAt, flight.startedAt) && Objects.equals(completedAt, flight.completedAt) && Objects.equals(cancelledAt, flight.cancelledAt) && Objects.equals(aircraftTypeIcao, flight.aircraftTypeIcao) && Objects.equals(aircraftReg, flight.aircraftReg) && Objects.equals(events, flight.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, flightNumberIcao, departureAirportIcao, arrivalAirportIcao, status, createdAt, startedAt, completedAt, cancelledAt, aircraftTypeIcao, aircraftReg, events);
    }
}
