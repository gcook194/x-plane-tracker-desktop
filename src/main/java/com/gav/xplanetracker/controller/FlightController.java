package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.enums.IntlDateLineOffset;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.service.*;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    private final FlightService flightService;
    private final NavigraphService navigraphService;
    private final XPlaneService xPlaneService;
    private final SettingsService settingsService;
    private final MapService mapService;

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
        this.mapService = MapService.getInstance();

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
    private Button cancelFlight;

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

        flightService.getActiveFlight().ifPresentOrElse(
                this::activeFlightView,
                this::noActiveFlightView
        );
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
        cancelFlight.setVisible(false);
        cancelFlight.setVisible(false);
        flightDetailsBox.setVisible(false);
        flightDetailsBox.setManaged(false);
        flightInfoBox.setVisible(false);
        flightInfoBox.setManaged(false);
        startFlight.setVisible(true);
        startFlight.setManaged(true);

        noActiveFlightView();
    }

    @FXML
    protected void onCancelFlightClick() {
        flightService.cancelActiveFlight();

        stopFlight.setVisible(false);
        stopFlight.setManaged(false);
        cancelFlight.setVisible(false);
        cancelFlight.setVisible(false);
        flightDetailsBox.setVisible(false);
        flightDetailsBox.setManaged(false);
        flightInfoBox.setVisible(false);
        flightInfoBox.setManaged(false);
        startFlight.setVisible(true);
        startFlight.setManaged(true);

        noActiveFlightView();
    }

    @FXML
    private void onActiveFlightProgressTabSelected() {
        logger.debug("refreshing active flight progress tab");
        flightService.getActiveFlight()
                .ifPresent(this::loadMap);
    }

    @FXML
    private void onActiveFlightDataTabSelected() {
        logger.debug("refreshing active flight data graphs");
        flightService.getActiveFlight()
                .ifPresent(this::loadFlightData);
    }

    private void loadNavigraphMap(Flight flight) {
        navigraphWebEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        navigraphWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                navigraphWebEngine.executeScript("loadMap();");

                if (flight != null) {
                    final IntlDateLineOffset idlLongitudeOffset = mapService.getIntlDateLineOffset(navigraphFlightPlan);

                    mapService.addMarker(
                            navigraphWebEngine,
                            navigraphFlightPlan.getDeparture().getLatitude(),
                            navigraphFlightPlan.getDeparture().getLongitude(),
                            navigraphFlightPlan.getDeparture().getName(),
                            idlLongitudeOffset
                    );

                    navigraphFlightPlan.getWaypoints().forEach(waypoint -> {
                        mapService.addMarker(
                                navigraphWebEngine,
                                waypoint.getLatitude(),
                                waypoint.getLongitude(),
                                waypoint.getName(),
                                idlLongitudeOffset
                        );

                        logger.debug("[{}, {}]", waypoint.getLatitude(), waypoint.getLongitude());
                    });

                    mapService.addMarker(
                            navigraphWebEngine,
                            navigraphFlightPlan.getArrival().getLatitude(),
                            navigraphFlightPlan.getArrival().getLongitude(),
                            navigraphFlightPlan.getArrival().getName(),
                            idlLongitudeOffset
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

    private void loadMap(Flight flight) {
        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("loadMap();");
                if (flight != null) {
                    final List<FlightEvent> flightEvents = flightService.getFlightEvents(flight);
                    mapService.drawActualRoute(webEngine, flightEvents);
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

    private void loadFlightData(Flight flight) {
        activeFlightAltitudePanel.getChildren().clear();
        activeFlightSpeedPanel.getChildren().clear();

        if (flight != null) {
            final List<FlightEvent> events = flightService.getFlightEvents(flight);

            mapService.drawLineGraph(activeFlightAltitudePanel, events, FlightEventType.DENSITY_ALTITUDE);
            mapService.drawLineGraph(activeFlightSpeedPanel, events, FlightEventType.GROUND_SPEED);
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

        cancelFlight.setVisible(true);
        cancelFlight.setVisible(true);

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
        loadMap(flight);
        loadFlightData(flight);
    }

    private void noActiveFlightView() {
        loadMap(null);
        loadNavigraphMap(null);
        loadFlightData(null);
    }
}