package com.gav.xplanetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.enums.IntlDateLineOffset;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.model.MapOptions;
import javafx.concurrent.Worker;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

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
                final IntlDateLineOffset idlLongitudeOffset = getIntlDateLineOffset(flightEvents.getFirst().getLongitude(), flightEvents.getLast().getLongitude());
                if (mapOptions.showDepartureArrival()) {
                    this.addMarker(
                            webEngine,
                            flightEvents.getFirst().getLatitude(),
                            flightEvents.getFirst().getLongitude(),
                            null,
                            idlLongitudeOffset
                    );

                    this.addMarker(
                            webEngine,
                            flightEvents.getLast().getLatitude(),
                            flightEvents.getLast().getLongitude(),
                            null,
                            idlLongitudeOffset
                    );
                }

                final String jsonLatLongs = mapper.writeValueAsString(latLongs);
                final Double heading = mapOptions.showAircraftOnMap() ? flightEvents.getLast().getHeading() : null;
                webEngine.executeScript("drawActualRouteLine(" + jsonLatLongs  +"," + heading + ");");
            } catch (JsonProcessingException e) {
                logger.error("Could not parse latLong data to JSON", e);
            }
        }
    }

    public void drawLineGraph(VBox chartContainer, List<FlightEvent> events, FlightEventType eventType) {
        final CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Time");

        final NumberAxis yAxis = new NumberAxis();

        final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setStyle("-fx-background-color: transparent;");

        final XYChart.Series<String, Number> series = new XYChart.Series<>();

        if (events != null && !events.isEmpty()) {
            switch (eventType) {
                case GROUND_SPEED -> {
                    addDataPointToChart(events, series, FlightEvent::getGroundSpeed);

                    series.setName("Ground Speed");
                    yAxis.setLabel("Ground Speed (Kts)");
                    chart.setTitle("Speed over flight duration");
                }
                case DENSITY_ALTITUDE -> {
                    addDataPointToChart(events, series, FlightEvent::getPressureAltitude);

                    series.setName("Altitude");
                    yAxis.setLabel("Altitude (Ft)");
                    chart.setTitle("Altitude over flight duration");
                }
                default -> throw new IllegalStateException("Event type not supported");
            }

            xAxis.setTickLabelsVisible(false);
            xAxis.setTickMarkVisible(false);
            yAxis.setTickMarkVisible(false);
        }

        chart.getData().clear();
        chart.getData().add(series);

        chartContainer.getChildren().add(chart);
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

    public void drawFlightRouteMap(WebView webView, Pane mapPanel, List<FlightEvent> flightEvents, MapOptions mapOptions) {
        final WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");
                if (flightEvents != null && !flightEvents.isEmpty()) {
                    this.drawActualRoute(webEngine, flightEvents, mapOptions);
                }
            }
        });

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
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

    private void addDataPointToChart(List<FlightEvent> events, XYChart.Series<String, Number> series, Function<FlightEvent, Double> methodToCall) {
        events.stream()
                .filter(FlightEvent::isEnginesRunning)
                .forEach(event -> {
                    final OffsetDateTime odt =
                            OffsetDateTime.ofInstant(event.getCreatedAt(), ZoneId.of("Europe/London"));
                    final String time = odt.format(DateTimeFormatter.ofPattern("HH:mm"));

                    series.getData().add(new XYChart.Data<>(time, methodToCall.apply(event)));
                });
    }
}
