package com.gav.xplanetracker.controller;

import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.enums.FlightEventType;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.gav.xplanetracker.model.MapOptions;
import com.gav.xplanetracker.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    private final ChartService chartService;
    private final ScreenshotService screenshotService;

    final WebView webView;

    final WebView simbriefWebView;

    public FlightHistoryController() {
        this.flightService = FlightService.getInstance();
        this.mapService = MapService.getInstance();
        this.navigraphService = NavigraphService.getInstance();
        this.chartService = ChartService.getInstance();
        this.screenshotService = ScreenshotService.getInstance();

        this.webView = new WebView();
        this.simbriefWebView = new WebView();
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
    private Label aircraftType;

    @FXML
    private Label aircraftReg;

    @FXML
    private VBox flightDetailsBox;

    @FXML
    private Label flightRoute;

    @FXML
    private Label flightCodeLabel;

    @FXML
    private Label routeLabel;

    @FXML
    private Label fullRouteLabel;

    @FXML
    private Label flightDuration;

    @FXML
    private Label offBlockTime;

    @FXML
    private Label arrivalTime;

    @FXML
    private Label flightDistance;

    @FXML
    private VBox flightInfoBox;

    @FXML
    private ImageView flightImage;

    @FXML
    private StackPane flightImagePane;

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
                    setGraphic(null);
                } else {
                    final VBox cellLayout = flightDetailsListItem(flight);
                    setGraphic(cellLayout);
                }
            }
        });


        flightHistoryList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldFlight, selectedFlight) -> {
            if (selectedFlight != null) {
                logger.info("Loading details for flight {}", selectedFlight.getId());

                loadFlightDetails(selectedFlight);

                flightDetailsPane.setVisible(true);
                flightDetailsPane.setManaged(true);
            }
        });

        leftPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(1.0 / 3));
        flightInfoPanel.prefWidthProperty().bind(rootBox.widthProperty().multiply(2.0 / 3));

        webView.prefWidthProperty().bind(flightInfoPanel.widthProperty());
        webView.prefHeightProperty().bind(flightInfoPanel.heightProperty());

        simbriefWebView.prefWidthProperty().bind(flightInfoPanel.widthProperty());
        simbriefWebView.prefHeightProperty().bind(flightInfoPanel.heightProperty());

        flightImage.fitWidthProperty().bind(flightInfoPanel.widthProperty().multiply(0.5));
    }

    private static VBox flightDetailsListItem(Flight flight) {
        final String completedAt = Optional.ofNullable(flight.getCompletedAt())
                .map(completed -> {
                    final LocalDateTime ldt = LocalDateTime.ofInstant(completed, ZoneOffset.UTC);
                    return ldt.format(DateTimeFormatter.ofPattern("dd MMM yy"));
                })
                .orElse("N/A");

        final Label flightNumberLabel = new Label(flight.getFlightNumberIcao());
        flightNumberLabel.getStyleClass().add("flight-number");

        final Label completedAtLabel = new Label(completedAt);
        completedAtLabel.getStyleClass().add("completed-date");

        final HBox topRow = new HBox(flightNumberLabel, new Region(), completedAtLabel);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);
        topRow.setAlignment(Pos.CENTER_LEFT);

        final Label routeLabel = new Label(flight.getDepartureAirportIcao() + " → " + flight.getArrivalAirportIcao());
        routeLabel.getStyleClass().add("flight-route");

        final Label aircraftLabel = new Label(flight.getAircraftReg() + " (" + flight.getAircraftTypeIcao() + ")");
        aircraftLabel.getStyleClass().add("aircraft-info");

        final VBox cellLayout = new VBox(topRow, routeLabel, aircraftLabel);
        cellLayout.setSpacing(4);
        cellLayout.getStyleClass().add("flight-cell");

        return cellLayout;
    }

    private void loadFlightDetails(Flight flight) {
        final List<FlightEvent> flightEvents = flightService.getFlightEvents(flight);

        // Flight screenshot
        loadHeroImage(flight);

        // Flight information
        loadFlightInfo(flight);

        // simbrief route
        if (flight.getNavigraphJson() != null) {
            final NavigraphFlightPlan navigraphFlightPlan = navigraphService.getFlightPlan(flight);
            mapService.drawSimbriefMap(simbriefWebView, simbriefRoutePanel, navigraphFlightPlan);
        }

        // actual flight route
        final MapOptions actualRouteMapOptions = new MapOptions()
                .setShowAircraftOnMap(false)
                .setShowDepartureArrival(true);

        mapService.drawFlightRouteMap(webView, flightMapPanel, flightEvents, actualRouteMapOptions);

        // flight data
        loadFlightData(flightEvents);
    }

    private void loadFlightData(List<FlightEvent> flightEvents) {
        flightAltitudePanel.getChildren().clear();
        flightSpeedPanel.getChildren().clear();

        chartService.drawLineGraph(flightAltitudePanel, flightEvents, FlightEventType.DENSITY_ALTITUDE);
        chartService.drawLineGraph(flightSpeedPanel, flightEvents, FlightEventType.GROUND_SPEED);
    }

    private void loadFlightInfo(Flight flight) {
        aircraftType.setText(flight.getAircraftTypeIcao());
        aircraftReg.setText(flight.getAircraftReg());
        flightCodeLabel.setText(flight.getFlightNumberIcao());
        routeLabel.setText(
                String.format("%s -> %s", flight.getDepartureAirportIcao(), flight.getArrivalAirportIcao())
        );

        final String blockTime = flightService.getBlockTime(flight);
        flightDuration.setText(blockTime);

        final String offBlockTimeStr = flightService.getOffBlockTime(flight);
        offBlockTime.setText(offBlockTimeStr);

        final String arrivalTimeStr = flightService.getArrivalTime(flight);
        arrivalTime.setText(arrivalTimeStr);

        if (flight.getNavigraphJson() != null) {
            final NavigraphFlightPlan flightPlan = navigraphService.getFlightPlan(flight);
            final int distance = navigraphService.getPlannedFlightDistance(flightPlan);
            flightRoute.setText(flightPlan.getRoute());
            fullRouteLabel.setText(
                    String.format(
                            "%s to %s",
                            flightPlan.getDeparture().getName(),
                            flightPlan.getArrival().getName()
                    )
            );
            flightDistance.setText(Integer.toString(distance));
        }

        flightDetailsBox.setVisible(true);
        flightDetailsBox.setManaged(true);

        flightInfoBox.setVisible(true);
        flightInfoBox.setManaged(true);
    }

    private void loadHeroImage(Flight flight) {
        final List<Image> images = screenshotService.getScreenshots(flight);

        flightImagePane.setVisible(false);
        flightImagePane.setManaged(false);
        flightImage.setImage(null);

        if (!images.isEmpty()) {
            flightImage.setImage(images.getFirst());

            flightImagePane.setVisible(true);
            flightImagePane.setManaged(true);
        }
    }
}
