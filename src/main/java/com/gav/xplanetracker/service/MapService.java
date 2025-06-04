package com.gav.xplanetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.IntlDateLineOffset;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.model.MapOptions;
import javafx.concurrent.Worker;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MapService {

    private static final Logger logger = LoggerFactory.getLogger(MapService.class);

    private static MapService INSTANCE;

    public static MapService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MapService();
        }

        return INSTANCE;
    }

    public void addMarker(WebEngine webEngine, double latitude, double longitude, String label, IntlDateLineOffset offset) {
        switch (offset) {
            case IntlDateLineOffset.EASTBOUND -> {
                if (longitude < 0) {
                    longitude += IntlDateLineOffset.EASTBOUND.getOffset();
                }
            }
            case IntlDateLineOffset.WESTBOUND -> {
                if (longitude > 0) {
                    longitude += IntlDateLineOffset.WESTBOUND.getOffset();
                }
            }
            default -> logger.debug("Flight does not cross the international date line");
        }

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String script = String.format("addMarkerToMap(%f, %f, %s);", latitude, longitude, mapper.writeValueAsString(label));
            webEngine.executeScript(script);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawActualRoute(WebEngine webEngine, List<FlightEvent> flightEvents, MapOptions mapOptions) {
        final List<double[]> latLongs = flightEvents.stream()
                .map(event -> {
                    final double[] latLong = new double[2];
                    latLong[0] = event.getLatitude();
                    latLong[1] = event.getLongitude();

                    return latLong;
                })
                .toList();

        if (!latLongs.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                final String jsonLatLongs = mapper.writeValueAsString(latLongs);

                webEngine.executeScript("drawActualRouteLine(" + jsonLatLongs + ");");
            } catch (JsonProcessingException e) {
                logger.error("Could not parse latLong data to JSON", e);
            }
        }
    }

    public IntlDateLineOffset getIntlDateLineOffset(double departureLongitude, double arrivalLongitude) {
        final double delta = arrivalLongitude - departureLongitude;

        if (delta > 180) {
            return IntlDateLineOffset.WESTBOUND;
        } else if (delta < -180) {
            return IntlDateLineOffset.EASTBOUND;
        }

        return IntlDateLineOffset.NONE;
    }

    public void drawBasicMap(WebView webView, Pane mapPanel) {
        final WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");
            }
        });

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
    }

    public void drawFlightRouteMap(WebView webView, Pane mapPanel, List<FlightEvent> flightEvents, MapOptions mapOptions) {
        final WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");

                if (flightEvents != null && !flightEvents.isEmpty()) {
                    this.drawActualRoute(webEngine, flightEvents, mapOptions);

                    if (mapOptions.showAircraftOnMap()) {
                        this.addAircraftToMap(webEngine, flightEvents);
                    }

                    if (mapOptions.showDepartureArrival()) {
                        this.addDepartureAndArrivalPins(webEngine, flightEvents);
                    }
                }
            }
        });

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
    }

    //TODO don't love this - using simbrief plan potentially better
    private void addDepartureAndArrivalPins(WebEngine webEngine, List<FlightEvent> flightEvents) {
        final double departureLongitude = flightEvents.getFirst().getLongitude();
        final double arrivalLongitude = flightEvents.getLast().getLongitude();
        final IntlDateLineOffset longitudeOffset = getIntlDateLineOffset(departureLongitude, arrivalLongitude);

        addMarker(
                webEngine,
                flightEvents.getFirst().getLatitude(),
                flightEvents.getFirst().getLongitude(),
                "Departure",
                longitudeOffset
        );

        addMarker(
                webEngine,
                flightEvents.getLast().getLatitude(),
                flightEvents.getLast().getLongitude(),
                "Arrival",
                longitudeOffset
        );
    }

    private void addAircraftToMap(WebEngine webEngine, List<FlightEvent> flightEvents) {
        final FlightEvent aircraftPositionEvent = flightEvents.getLast();
        final double[] latLong = new double[] {
                aircraftPositionEvent.getLatitude(),
                aircraftPositionEvent.getLongitude()
        };
        final double heading = aircraftPositionEvent.getHeading();

        final ObjectMapper mapper = new ObjectMapper();
        try {
            final String jsonLatLong = mapper.writeValueAsString(latLong);
            webEngine.executeScript(String.format("addRotatedPlaneMarker(%s, %f);", jsonLatLong, heading));
        } catch (JsonProcessingException e) {
            logger.error("Could not parse latLong data to JSON", e);
        }
    }

    public void drawSimbriefMap(WebView webView, Pane mapPanel, NavigraphFlightPlan simbriefFlightPlan) {
        final WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");

                final double departureLongitude = simbriefFlightPlan.getDeparture().getLongitude();
                final double arrivalLongitude = simbriefFlightPlan.getArrival().getLongitude();
                final IntlDateLineOffset idlLongitudeOffset = this.getIntlDateLineOffset(departureLongitude, arrivalLongitude);

                this.addMarker(
                        webEngine,
                        simbriefFlightPlan.getDeparture().getLatitude(),
                        simbriefFlightPlan.getDeparture().getLongitude(),
                        simbriefFlightPlan.getDeparture().getName(),
                        idlLongitudeOffset
                );

                simbriefFlightPlan.getWaypoints().forEach(waypoint -> {
                    this.addMarker(
                            webEngine,
                            waypoint.getLatitude(),
                            waypoint.getLongitude(),
                            waypoint.getName(),
                            idlLongitudeOffset
                    );

                    logger.debug("[{}, {}]", waypoint.getLatitude(), waypoint.getLongitude());
                });

                this.addMarker(
                        webEngine,
                        simbriefFlightPlan.getArrival().getLatitude(),
                        simbriefFlightPlan.getArrival().getLongitude(),
                        simbriefFlightPlan.getArrival().getName(),
                        idlLongitudeOffset
                );

                webEngine.executeScript("drawBasicRouteLine();");
                webEngine.executeScript("fitToAllMarkers();");
            }
        });

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
    }
}
