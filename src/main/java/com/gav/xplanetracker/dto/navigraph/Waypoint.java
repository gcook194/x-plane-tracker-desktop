package com.gav.xplanetracker.dto.navigraph;

public class Waypoint {

    private String name;
    private String stageOfFlight;
    private String viaAirway;
    private double latitude;
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStageOfFlight() {
        return stageOfFlight;
    }

    public void setStageOfFlight(String stageOfFlight) {
        this.stageOfFlight = stageOfFlight;
    }

    public String getViaAirway() {
        return viaAirway;
    }

    public void setViaAirway(String viaAirway) {
        this.viaAirway = viaAirway;
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
}
