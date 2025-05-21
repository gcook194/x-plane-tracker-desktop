package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.NavigraphService;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class FlightController {

    private final FlightService flightService;
    private final NavigraphService navigraphService;

    private final WebView webView;
    private final WebEngine webEngine;

    private NavigraphFlightPlan navigraphFlightPlan;

    public FlightController() {
        this.flightService = FlightService.getInstance();
        this.navigraphService = NavigraphService.getInstance();
        this.webView = new WebView();
        this.webEngine = webView.getEngine();
    }

    @FXML
    private Label departureAirport;

    @FXML
    private Label arrivalAirport;

    @FXML
    private Label aircraftType;

    @FXML
    private Label aircraftReg;

    @FXML
    private Label flightNumber;

    @FXML
    private GridPane flightDetailsBox;

    @FXML
    private VBox loadingBox;

    @FXML
    private ProgressIndicator progressIndicator;

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
    public void initialize() {

        // load the flight plan from Navigraph
        navigraphFlightPlan = navigraphService.getFlightPlan();

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        mapPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));

        webEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        // TODO make this a method
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                addMarker(
                        webEngine,
                        navigraphFlightPlan.getDeparture().getLatitude(),
                        navigraphFlightPlan.getDeparture().getLongitude(),
                        navigraphFlightPlan.getDeparture().getName()
                );

                addMarker(
                        webEngine,
                        navigraphFlightPlan.getArrival().getLatitude(),
                        navigraphFlightPlan.getArrival().getLongitude(),
                        navigraphFlightPlan.getArrival().getName()
                );

                webEngine.executeScript("drawBasicRouteLine();");
                webEngine.executeScript("fitToAllMarkers();");
            }
        });

        webView.prefWidthProperty().bind(mapPanel.widthProperty());
        webView.prefHeightProperty().bind(mapPanel.heightProperty());

        mapPanel.getChildren().clear();
        mapPanel.getChildren().add(webView);
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

                startFlight.setVisible(false);
                startFlight.setManaged(false);

                stopFlight.setVisible(true);
                stopFlight.setManaged(true);

                final Flight flight = getValue();
                flightNumber.setText(flight.getFlightNumberIcao());
                departureAirport.setText(flight.getDepartureAirportIcao());
                arrivalAirport.setText(flight.getArrivalAirportIcao());
                aircraftType.setText(flight.getAircraftTypeIcao());
                aircraftReg.setText(flight.getAircraftReg());
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
        stopFlight.setVisible(false);
        stopFlight.setManaged(false);
        flightDetailsBox.setVisible(false);
        flightDetailsBox.setManaged(false);
        startFlight.setVisible(true);
        startFlight.setManaged(true);
    }

    private void addMarker(WebEngine webEngine, double latitude, double longitude, String label) {
        // fix markets not repeating
        if (longitude < 0) {
            longitude += 360;
        }

        String script = String.format("addMarkerToMap(%f, %f, '%s');", latitude, longitude, label);
        webEngine.executeScript(script);
    }
}