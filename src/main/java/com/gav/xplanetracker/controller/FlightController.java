package com.gav.xplanetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.service.*;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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

public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    private final FlightService flightService;
    private final NavigraphService navigraphService;
    private final XPlaneService xPlaneService;
    private final SettingsService settingsService;
    private final EventService eventService;

    private NavigraphFlightPlan navigraphFlightPlan;
    final WebView webView;
    final WebEngine webEngine;

    final WebView navigraphWebView;
    final WebEngine navigraphWebEngine;

    public FlightController() {
        this.flightService = FlightService.getInstance();
        this.navigraphService = NavigraphService.getInstance();
        this.xPlaneService = XPlaneService.getInstance();
        this.settingsService = SettingsService.getInstance();
        this.eventService = EventService.getInstance();

        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        this.navigraphWebView = new WebView();
        this.navigraphWebEngine = navigraphWebView.getEngine();
    }

    @FXML
    private Label aircraftType;

    @FXML
    private Label aircraftReg;

    @FXML
    private VBox flightDetailsBox;

    @FXML
    private VBox loadingBox;

    @FXML
    private Button startFlight;

    @FXML
    private Button stopFlight;

    @FXML
    private VBox leftPanel;

    @FXML
    private HBox rootBox;

    @FXML
    private Label errorMessage;

    @FXML
    private HBox errorBanner;

    @FXML
    private HBox successBanner;

    @FXML
    private Label successMessage;

    @FXML
    private Label flightRoute;

    @FXML
    private Label flightCodeLabel;

    @FXML
    private Label routeLabel;

    @FXML
    private Label fullRouteLabel;

    @FXML
    private VBox flightInfoBox;

    @FXML
    private VBox mapPanel;

    @FXML
    private VBox navigraphMapPanel;

    @FXML
    private VBox activeFlightMapPanel;

    @FXML
    private VBox activeFlightAltitudePanel;

    @FXML
    private VBox activeFlightSpeedPanel;

    @FXML
    public void initialize() {
        handleSimulatorState();
        navigraphFlightPlan = navigraphService.getFlightPlan();

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        mapPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));

        flightService.getCurrentFlight().ifPresentOrElse(
                this::activeFlightView,
                this::noActiveFlightPlanView
        );
    }

    @FXML
    protected void onStartFlightClick() {
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);

        final Task<Flight> loadFlightDataTask = new Task<>() {
            @Override
            protected Flight call() {
                final String aircraftReg = eventService.getAircraftRegistration();
                return flightService.getOrCreateCurrentFlight(navigraphFlightPlan, aircraftReg);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                activeFlightView(getValue());
            }

            @Override
            protected void failed() {
                super.failed();
                loadingBox.setVisible(false);
                loadingBox.setManaged(false);
            }
        };

        new Thread(loadFlightDataTask).start();
    }

    @FXML
    protected void onStopFlightClick() {
        flightService.completeActiveFlight();

        stopFlight.setVisible(false);
        stopFlight.setManaged(false);
        flightDetailsBox.setVisible(false);
        flightDetailsBox.setManaged(false);
        flightInfoBox.setVisible(false);
        flightInfoBox.setManaged(false);
        startFlight.setVisible(true);
        startFlight.setManaged(true);

        loadMap(false, null);
    }

    // TODO move to service layer
    private void addMarker(WebEngine webEngine, double latitude, double longitude, String label) {
        // fixes issue where negative longitudes always display to the east on the map
        if (longitude < 0) {
            longitude += 360;
        }

        final ObjectMapper mapper = new ObjectMapper();
        try {
            label = mapper.writeValueAsString(label);
            final String script = String.format("addMarkerToMap(%f, %f, %s);", latitude, longitude, label);
            webEngine.executeScript(script);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO move to service layer
    private void drawActualRoute(Flight flight) {
        final List<FlightEvent> events = flightService.getFlightEvents(flight);
        final List<double[]> latLongs = events.stream()
                .map(event -> {
                    final double[] latLong = new double[2];
                    latLong[0] = event.getLatitude();
                    latLong[1] = event.getLongitude();

                    return latLong;
                })
                .toList();

        final double heading = events.getLast().getHeading();

        if (!latLongs.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonLatLongs = mapper.writeValueAsString(latLongs);
                webEngine.executeScript("drawActualRouteLine(" + jsonLatLongs  +"," + heading + ");");
            } catch (JsonProcessingException e) {
                logger.error("Could not parse latLong data to JSON", e);
            }
        }
    }

    private void loadNavigraphMap(Flight flight) {
        navigraphWebEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        navigraphWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                navigraphWebEngine.executeScript("loadMap();");

                if (flight != null) {
                    addMarker(
                            navigraphWebEngine,
                            navigraphFlightPlan.getDeparture().getLatitude(),
                            navigraphFlightPlan.getDeparture().getLongitude(),
                            navigraphFlightPlan.getDeparture().getName()
                    );

                    navigraphFlightPlan.getWaypoints().forEach(waypoint -> {
                        addMarker(
                                navigraphWebEngine,
                                waypoint.getLatitude(),
                                waypoint.getLongitude(),
                                waypoint.getName()
                        );
                    });

                    addMarker(
                            navigraphWebEngine,
                            navigraphFlightPlan.getArrival().getLatitude(),
                            navigraphFlightPlan.getArrival().getLongitude(),
                            navigraphFlightPlan.getArrival().getName()
                    );

                    navigraphWebEngine.executeScript("drawBasicRouteLine();");
                    navigraphWebEngine.executeScript("fitToAllMarkers();");
                }
            }
        });

        navigraphWebView.prefWidthProperty().bind(mapPanel.widthProperty());
        navigraphWebView.prefHeightProperty().bind(mapPanel.heightProperty());

        navigraphMapPanel.getChildren().clear();
        navigraphMapPanel.getChildren().add(navigraphWebView);
    }

    private void loadMap(boolean drawFlightDetails, Flight flight) {
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");
                if (drawFlightDetails) {
                    drawActualRoute(flight);
                }
            }
        });

        webView.prefWidthProperty().bind(mapPanel.widthProperty());
        webView.prefHeightProperty().bind(mapPanel.heightProperty());

        activeFlightMapPanel.getChildren().clear();
        activeFlightMapPanel.getChildren().add(webView);
    }

    //TODO this could probably be done as a single HBox and label but I am too stupid
    public void handleSimulatorState() {
        final ApplicationSettingsDTO settings = settingsService.getSettings();
        final boolean isSimulatorRunning = xPlaneService.isSimulatorRunning(settings.xplaneHost());

        if (!isSimulatorRunning) {
            errorMessage.setText("X-Plane is not connected! Flight events won't be tracked.");
            errorBanner.setManaged(true);
            errorBanner.setVisible(true);
        } else {
            successMessage.setText("X-Plane is connected!");
            successBanner.setManaged(true);
            successBanner.setVisible(true);
        }
    }

    private void drawLineGraph(VBox chartContainer, List<FlightEvent> events, FlightEventType eventType) {
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

        chartContainer.prefWidthProperty().bind(mapPanel.prefWidthProperty());
        chartContainer.prefHeightProperty().bind(mapPanel.prefHeightProperty());
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

    private void loadFlightData(Flight flight) {
        if (flight != null) {
            final List<FlightEvent> events = flightService.getFlightEvents(flight);

            drawLineGraph(activeFlightAltitudePanel, events, FlightEventType.DENSITY_ALTITUDE);
            drawLineGraph(activeFlightSpeedPanel, events, FlightEventType.GROUND_SPEED);
        }
    }

    private void activeFlightView(Flight flight) {
        loadingBox.setVisible(false);
        loadingBox.setManaged(false);

        flightDetailsBox.setVisible(true);
        flightDetailsBox.setManaged(true);

        flightInfoBox.setVisible(true);
        flightInfoBox.setManaged(true);

        startFlight.setVisible(false);
        startFlight.setManaged(false);

        stopFlight.setVisible(true);
        stopFlight.setManaged(true);

        aircraftType.setText(flight.getAircraftTypeIcao());
        aircraftReg.setText(flight.getAircraftReg());
        flightRoute.setText(navigraphFlightPlan.getRoute());
        flightCodeLabel.setText(flight.getFlightNumberIcao());
        routeLabel.setText(
                String.format("%s -> %s", flight.getDepartureAirportIcao(), flight.getArrivalAirportIcao())
        );
        fullRouteLabel.setText(
                String.format(
                        "%s to %s",
                        navigraphFlightPlan.getDeparture().getName(),
                        navigraphFlightPlan.getArrival().getName()
                )
        );


        loadNavigraphMap(flight);
        loadMap(true, flight);
        loadFlightData(flight);
    }

    private void noActiveFlightPlanView() {
        loadMap(false, null);
        loadNavigraphMap(null);
        loadFlightData(null);
    }
}