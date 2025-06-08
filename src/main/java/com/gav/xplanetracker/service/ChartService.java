package com.gav.xplanetracker.service;

import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.model.FlightEvent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class ChartService {

    private static ChartService INSTANCE;

    public static ChartService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChartService();
        }

        return INSTANCE;
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
                case FUEL_QUANTITY -> {
                    addDataPointToChart(events, series, FlightEvent::getFuelQuantity);

                    series.setName("Fuel Quantity (KG)");
                    yAxis.setLabel("Fuel Quantity (KG)");
                    chart.setTitle("Fuel Usage over flight duration");
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
