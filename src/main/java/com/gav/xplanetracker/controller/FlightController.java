package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.service.FlightService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class FlightController {

    private final FlightService flightService;

    public FlightController() {
        this.flightService = new FlightService();
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
    protected void onStartFlightClick() {
        loadingBox.setVisible(true);
        loadingBox.setManaged(true);

        final Task<Flight> loadFlightDataTask = new Task<>() {
            @Override
            protected Flight call() throws Exception {
                return flightService.startFlight();
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
}