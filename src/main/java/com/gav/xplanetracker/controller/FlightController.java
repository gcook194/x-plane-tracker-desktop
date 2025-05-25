package com.gav.xplanetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.NavigraphService;
import com.gav.xplanetracker.service.SettingsService;
import com.gav.xplanetracker.service.XPlaneService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    private final FlightService flightService;
    private final NavigraphService navigraphService;
    private final XPlaneService xPlaneService;
    private final SettingsService settingsService;

    private NavigraphFlightPlan navigraphFlightPlan;
    final WebView webView;
    final WebEngine webEngine;

    public FlightController() {
        this.flightService = FlightService.getInstance();
        this.navigraphService = NavigraphService.getInstance();
        this.xPlaneService = XPlaneService.getInstance();
        this.settingsService = SettingsService.getInstance();

        this.webView = new WebView();
        this.webEngine = webView.getEngine();
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
    private VBox mapPanel;

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
    private StackPane mainContent;

    @FXML
    private Button settingsButton;

    @FXML
    private Button dashboardButton;

    @FXML
    public void initialize() {
        //TODO this doesn't work correctly - fix
        settingsButton.setOnAction(event -> loadView("/com/gav/xplanetracker/settings-view.fxml"));
        dashboardButton.setOnAction(event -> loadView("/com/gav/xplanetracker/start-flight-view.fxml"));

        handleSimulatorState();
        navigraphFlightPlan = navigraphService.getFlightPlan();

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        mapPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));
        loadMap(false, null);
    }

    @FXML
    protected void onStartFlightClick() {
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);

        final Task<Flight> loadFlightDataTask = new Task<>() {
            @Override
            protected Flight call() {
                return flightService.getOrCreateCurrentFlight(navigraphFlightPlan);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
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

                final Flight flight = getValue();
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


                loadMap(true, flight);
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
        //flightService.completeActiveFlight();

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

        String script = String.format("addMarkerToMap(%f, %f, '%s');", latitude, longitude, label);
        webEngine.executeScript(script);
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

        if (!latLongs.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String jsonLatLongs = mapper.writeValueAsString(latLongs);
                webEngine.executeScript("drawActualRouteLine(" + jsonLatLongs  +");");
            } catch (JsonProcessingException e) {
                logger.error("Could not parse latLong data to JSON", e);
            }
        }
    }

    private void loadMap(boolean drawFlightDetails, Flight flight) {
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");
                if (drawFlightDetails) {
//                    addMarker(
//                            webEngine,
//                            navigraphFlightPlan.getDeparture().getLatitude(),
//                            navigraphFlightPlan.getDeparture().getLongitude(),
//                            navigraphFlightPlan.getDeparture().getName()
//                    );
//
//                    navigraphFlightPlan.getWaypoints().forEach(waypoint -> {
//                        addMarker(
//                                webEngine,
//                                waypoint.getLatitude(),
//                                waypoint.getLongitude(),
//                                waypoint.getName()
//                        );
//                    });
//
//                    addMarker(
//                            webEngine,
//                            navigraphFlightPlan.getArrival().getLatitude(),
//                            navigraphFlightPlan.getArrival().getLongitude(),
//                            navigraphFlightPlan.getArrival().getName()
//                    );

                    // webEngine.executeScript("drawBasicRouteLine();");
                    // webEngine.executeScript("fitToAllMarkers();");

                    drawActualRoute(flight);
                }
            }
        });

        webView.prefWidthProperty().bind(mapPanel.widthProperty());
        webView.prefHeightProperty().bind(mapPanel.heightProperty());

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
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

    private void loadView(final String view) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
            final Parent settingsRoot = loader.load();

            mainContent.getChildren().setAll(settingsRoot); // Replace current content
        } catch (IOException e) {
            logger.error("Error when switching views: ", e);
        }
    }
}