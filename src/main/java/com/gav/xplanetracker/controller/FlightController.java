package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.ApplicationSettingsDTO;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.model.MapOptions;
import com.gav.xplanetracker.service.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private final ChartService chartService;

    final WebView webView;
    final WebView navigraphWebView;

    final MapOptions actualRouteMapOptions;

    public FlightController() {
        this.flightService = FlightService.getInstance();
        this.navigraphService = NavigraphService.getInstance();
        this.xPlaneService = XPlaneService.getInstance();
        this.settingsService = SettingsService.getInstance();
        this.mapService = MapService.getInstance();
        this.chartService = ChartService.getInstance();

        this.webView = new WebView();
        this.navigraphWebView = new WebView();

        this.actualRouteMapOptions = new MapOptions()
                .setShowAircraftOnMap(true)
                .setShowDepartureArrival(false);
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

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        mapPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));

        webView.prefWidthProperty().bind(mapPanel.widthProperty());
        webView.prefHeightProperty().bind(mapPanel.heightProperty());

        navigraphWebView.prefWidthProperty().bind(mapPanel.widthProperty());
        navigraphWebView.prefHeightProperty().bind(mapPanel.heightProperty());

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
                final NavigraphFlightPlan navigraphFlightPlan = navigraphService.getFlightPlan();
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

    // TODO cancel and complete methods are very similar
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
                .ifPresent(flight -> {
                    final List<FlightEvent> events = flightService.getFlightEvents(flight);
                    mapService.drawFlightRouteMap(webView, activeFlightMapPanel, events, actualRouteMapOptions);
                });
    }

    @FXML
    private void onActiveFlightDataTabSelected() {
        logger.debug("refreshing active flight data graphs");
        flightService.getActiveFlight()
                .ifPresent(this::loadFlightData);
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

            chartService.drawLineGraph(activeFlightAltitudePanel, events, FlightEventType.DENSITY_ALTITUDE);
            chartService.drawLineGraph(activeFlightSpeedPanel, events, FlightEventType.GROUND_SPEED);
        }
    }

    private void activeFlightView(Flight flight) {
        final NavigraphFlightPlan navigraphFlightPlan = navigraphService.getFlightPlan(flight);

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

        mapService.drawSimbriefMap(navigraphWebView, navigraphMapPanel, navigraphFlightPlan);

        final List<FlightEvent> events = flightService.getFlightEvents(flight);
        mapService.drawFlightRouteMap(webView, activeFlightMapPanel, events, actualRouteMapOptions);
        loadFlightData(flight);
    }

    private void noActiveFlightView() {
        mapService.drawBasicMap(webView, activeFlightMapPanel);
        mapService.drawBasicMap(navigraphWebView, navigraphMapPanel);
        loadFlightData(null);
    }
}