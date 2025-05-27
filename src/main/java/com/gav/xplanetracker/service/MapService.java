package com.gav.xplanetracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.model.FlightEvent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
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

    public void addMarker(WebEngine webEngine, double latitude, double longitude, String label) {
        // drawing lines between waypoints breaks if longitude is negative
        longitude += 360;

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String script = String.format("addMarkerToMap(%f, %f, %s);", latitude, longitude, mapper.writeValueAsString(label));
            webEngine.executeScript(script);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawActualRoute(WebEngine webEngine, List<FlightEvent> flightEvents) {
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
                String jsonLatLongs = mapper.writeValueAsString(latLongs);
                webEngine.executeScript("drawActualRouteLine(" + jsonLatLongs  +"," + flightEvents.getLast().getHeading() + ");");
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
