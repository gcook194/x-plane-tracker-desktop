package com.gav.xplanetracker.model;

public class MapOptions {

    private boolean showDepartureArrival;
    private boolean showAircraftOnMap;

    public boolean showDepartureArrival() {
        return showDepartureArrival;
    }

    public MapOptions setShowDepartureArrival(boolean showDepartureArrival) {
        this.showDepartureArrival = showDepartureArrival;
        return this;
    }

    public boolean showAircraftOnMap() {
        return showAircraftOnMap;
    }

    public MapOptions setShowAircraftOnMap(boolean showAircraftOnMap) {
        this.showAircraftOnMap = showAircraftOnMap;
        return this;
    }
}
