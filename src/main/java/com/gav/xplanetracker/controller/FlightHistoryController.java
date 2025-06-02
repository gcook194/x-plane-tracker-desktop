package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.enums.IntlDateLineOffset;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.service.FlightService;
import com.gav.xplanetracker.service.MapService;
import com.gav.xplanetracker.service.NavigraphService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FlightHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(FlightHistoryController.class);

    private final FlightService flightService;
    private final MapService mapService;
    private final NavigraphService navigraphService;

    final WebView webView;
    final WebEngine webEngine;

    final WebView simbriefWebView;
    final WebEngine simbriefWebEngine;

    public FlightHistoryController() {
        this.flightService = FlightService.getInstance();
        this.mapService = MapService.getInstance();
        this.navigraphService = NavigraphService.getInstance();

        this.webView = new WebView();
        this.webEngine = webView.getEngine();

        this.simbriefWebView = new WebView();
        this.simbriefWebEngine = simbriefWebView.getEngine();
    }

    @FXML
    private VBox leftPanel;

    @FXML
    private VBox flightInfoPanel;

    @FXML
    private HBox rootBox;

    @FXML
    private ListView<Flight> flightHistoryList;

    @FXML
    private VBox flightMapPanel;

    @FXML
    private VBox simbriefRoutePanel;

    @FXML
    private TabPane flightDetailsPane;

    @FXML
    private VBox flightSpeedPanel;

    @FXML
    private VBox flightAltitudePanel;

    @FXML
    public void initialize() {
        final ObservableList<Flight> flightObservableList =
                FXCollections.observableArrayList(flightService.getFlights());
        flightHistoryList.setItems(flightObservableList);

        flightHistoryList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Flight flight, boolean empty) {
                super.updateItem(flight, empty);
                if (empty || flight == null) {
                    setText(null);
                } else {
                   final String completedAt = Optional.ofNullable(flight.getCompletedAt())
                           .map(completed -> {
                               final LocalDateTime ldt = LocalDateTime.ofInstant(flight.getCompletedAt(), ZoneOffset.UTC);
                               return ldt.format(DateTimeFormatter.ofPattern("dd MMM yy"));
                           })
                           .orElse("N/A");

                    setText(String.format(
                            "%s\t\t%s - %s\t\t%s\t%s\t\t%s",
                            flight.getFlightNumberIcao(),
                            flight.getDepartureAirportIcao(),
                            flight.getArrivalAirportIcao(),
                            flight.getAircraftReg(),
                            flight.getAircraftTypeIcao(),
                            completedAt
                    ));
                }
            }
        });

        flightHistoryList.getSelectionModel().selectedItemProperty().addListener((obs, oldFlight, selectedFlight) -> {
            if (selectedFlight != null) {
                logger.info("Loading details for flight {}", selectedFlight.getId());

                loadFlightDetails(selectedFlight);

                flightDetailsPane.setVisible(true);
                flightDetailsPane.setManaged(true);
            }
        });

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        flightInfoPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));
    }

    private void loadFlightDetails(Flight flight) {
        // simbrief route
        if (flight.getNavigraphJson() != null) {
            final NavigraphFlightPlan navigraphFlightPlan = navigraphService.getFlightPlan(flight);
            loadNavigraphMap(navigraphFlightPlan);
        }

        // actual flight route
        loadMap(flight);

        // flight data
        loadFlightData(flight);
    }

    //TODO this and the method in FlightController are very similar - move to map service
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

        webView.prefWidthProperty().bind(flightInfoPanel.widthProperty());
        webView.prefHeightProperty().bind(flightInfoPanel.heightProperty());

        flightMapPanel.getChildren().clear();
        flightMapPanel.getChildren().add(webView);
    }

    private void loadNavigraphMap(NavigraphFlightPlan navigraphFlightPlan) {
        simbriefWebEngine.load(getClass().getResource("/web/map/map.html").toExternalForm());

        simbriefWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                simbriefWebEngine.executeScript("loadMap();");

                final IntlDateLineOffset idlLongitudeOffset = mapService.getIntlDateLineOffset(navigraphFlightPlan);

                mapService.addMarker(
                        simbriefWebEngine,
                        navigraphFlightPlan.getDeparture().getLatitude(),
                        navigraphFlightPlan.getDeparture().getLongitude(),
                        navigraphFlightPlan.getDeparture().getName(),
                        idlLongitudeOffset
                );

                navigraphFlightPlan.getWaypoints().forEach(waypoint -> {
                    mapService.addMarker(
                            simbriefWebEngine,
                            waypoint.getLatitude(),
                            waypoint.getLongitude(),
                            waypoint.getName(),
                            idlLongitudeOffset
                    );

                    logger.debug("[{}, {}]", waypoint.getLatitude(), waypoint.getLongitude());
                });

                mapService.addMarker(
                        simbriefWebEngine,
                        navigraphFlightPlan.getArrival().getLatitude(),
                        navigraphFlightPlan.getArrival().getLongitude(),
                        navigraphFlightPlan.getArrival().getName(),
                        idlLongitudeOffset
                );

                simbriefWebEngine.executeScript("drawBasicRouteLine();");
                simbriefWebEngine.executeScript("fitToAllMarkers();");
            }
        });

        simbriefWebView.prefWidthProperty().bind(flightInfoPanel.widthProperty());
        simbriefWebView.prefHeightProperty().bind(flightInfoPanel.heightProperty());

        simbriefRoutePanel.getChildren().clear();
        simbriefRoutePanel.getChildren().add(simbriefWebView);
    }

    private void loadFlightData(Flight flight) {
        flightAltitudePanel.getChildren().clear();
        flightSpeedPanel.getChildren().clear();

        final List<FlightEvent> events = flightService.getFlightEvents(flight);
        mapService.drawLineGraph(flightAltitudePanel, events, FlightEventType.DENSITY_ALTITUDE);
        mapService.drawLineGraph(flightSpeedPanel, events, FlightEventType.GROUND_SPEED);
    }
}
