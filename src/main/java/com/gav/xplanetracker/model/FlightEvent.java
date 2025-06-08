package com.gav.xplanetracker.model;

import java.time.Instant;
import java.util.Objects;

public class FlightEvent {

    private long id;
    private Instant createdAt;
    private double pressureAltitude;
    private double latitude;
    private double longitude;
    private double groundSpeed;
    private long flightId;
    private double heading;
    private boolean enginesRunning;
    private double fuelQuantity;
    private double simTimeSeconds;

    public FlightEvent() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public double getPressureAltitude() {
        return pressureAltitude;
    }

    public void setPressureAltitude(double pressureAltitude) {
        this.pressureAltitude = pressureAltitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getGroundSpeed() {
        return groundSpeed;
    }

    public void setGroundSpeed(double groundSpeed) {
        this.groundSpeed = groundSpeed;
    }

    public long getFlightId() {
        return flightId;
    }

    public void setFlightId(long flightId) {
        this.flightId = flightId;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public boolean isEnginesRunning() {
        return enginesRunning;
    }

    public void setEnginesRunning(boolean enginesRunning) {
        this.enginesRunning = enginesRunning;
    }

    public double getFuelQuantity() {
        return fuelQuantity;
    }

    public void setFuelQuantity(double fuelQuantity) {
        this.fuelQuantity = fuelQuantity;
    }

    public double getSimTimeSeconds() {
        return simTimeSeconds;
    }

    public void setSimTimeSeconds(double simTimeSeconds) {
        this.simTimeSeconds = simTimeSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FlightEvent that = (FlightEvent) o;
        return id == that.id && Double.compare(pressureAltitude, that.pressureAltitude) == 0 && Double.compare(latitude, that.latitude) == 0 && Double.compare(longitude, that.longitude) == 0 && Double.compare(groundSpeed, that.groundSpeed) == 0 && flightId == that.flightId && Double.compare(heading, that.heading) == 0 && enginesRunning == that.enginesRunning && fuelQuantity == that.fuelQuantity && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, pressureAltitude, latitude, longitude, groundSpeed, flightId, heading, enginesRunning, fuelQuantity);
    }
}
