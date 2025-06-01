package com.gav.xplanetracker.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gav.xplanetracker.dao.SettingsDaoJDBC;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.dto.navigraph.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class NavigraphService {

    private static final Logger logger = LoggerFactory.getLogger(NavigraphService.class);

    public static final String SIMBRIEF_URI = "https://www.simbrief.com/api/xml.fetcher.php";
    private static NavigraphService INSTANCE;

    private final SettingsDaoJDBC settingsDao;
    private final HttpClient client;
    private final XmlMapper xmlMapper;

    public NavigraphService() {
        this.settingsDao = SettingsDaoJDBC.getInstance();
        this.client = HttpClient.newHttpClient();
        this.xmlMapper = new XmlMapper();
        configureXmlMapper();
    }

    public static NavigraphService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NavigraphService();
        }
        return INSTANCE;
    }

    private void configureXmlMapper() {
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void setGeneralDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode generalNode = navigraphResponse.path("general");
        final String airline = generalNode.path("icao_airline").asText();
        final String flightNumber = generalNode.path("flight_number").asText();
        final String route = generalNode.path("route").asText();

        logger.info("General - airline - {}",  airline);
        logger.info("General - flight number - {}", flightNumber);
        logger.info("General - route - {}", route);

        flightPlan.setFlightNumber(flightNumber);
        flightPlan.setIcaoAirline(airline);
        flightPlan.setRoute(route);
    }

    private void setOriginDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode originNode = navigraphResponse.path("origin");
        final String icaoCode = originNode.path("icao_code").asText();
        final String name = originNode.path("name").textValue();
        final double latitude = originNode.path("pos_lat").asDouble();
        final double longitude = originNode.path("pos_long").asDouble();

        logger.info("Origin - arrival airport - {}", icaoCode);
        logger.info("Origin - name - {}", name);
        logger.info("Origin - latitude - {}", latitude);
        logger.info("Origin - longitude - {}", longitude);

        flightPlan.getDeparture().setIcaoCode(icaoCode);
        flightPlan.getDeparture().setName(name);
        flightPlan.getDeparture().setLatitude(latitude);
        flightPlan.getDeparture().setLongitude(longitude);
    }

    private void setDestinationDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode destinationNode = navigraphResponse.path("destination");
        final String icaoCode = destinationNode.path("icao_code").asText();
        final String name = destinationNode.path("name").textValue();
        final double latitude = destinationNode.path("pos_lat").asDouble();
        final double longitude = destinationNode.path("pos_long").asDouble();

        logger.info("Destination - arrival airport - {}", icaoCode);
        logger.info("Destination - name - {}", name);
        logger.info("Destination - latitude - {}", latitude);
        logger.info("Destination - longitude - {}", longitude);

        flightPlan.getArrival().setIcaoCode(icaoCode);
        flightPlan.getArrival().setName(name);
        flightPlan.getArrival().setLatitude(latitude);
        flightPlan.getArrival().setLongitude(longitude);
    }

    private void setAircraftDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode aircraftNode = navigraphResponse.path("aircraft");
        final String aircraftType = aircraftNode.path("icaocode").asText();
        final String aircraftReg = aircraftNode.path("reg").asText();

        logger.info("Aircraft - type - {}", aircraftType);
        logger.info("Aircraft - registration - {}", aircraftReg);

        flightPlan.setAircraftType(aircraftType);
        flightPlan.setAircraftRegistration(aircraftReg);
    }

    private void setWaypointDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode waypointsNode = navigraphResponse.path("navlog");
        final List<Waypoint> waypoints = new ArrayList<>();

        logger.info("Waypoints");

        waypointsNode.path("fix").elements().forEachRemaining(fix -> {
            final String name = fix.path("name").asText();
            final String stageOfFlight = fix.path("stage").asText();
            final String viaAirway = fix.path("via_airway").asText();
            final double latitude = fix.path("pos_lat").asDouble();
            final double longitude = fix.path("pos_long").asDouble();
            final int distance = fix.path("distance").asInt();

            logger.info("\t {}, lat: {}, long: {}", name, latitude, longitude);

            final Waypoint waypoint = new Waypoint();
            waypoint.setName(name);
            waypoint.setStageOfFlight(stageOfFlight);
            waypoint.setViaAirway(viaAirway);
            waypoint.setLatitude(latitude);
            waypoint.setLongitude(longitude);
            waypoint.setDistance(distance);

            waypoints.add(waypoint);
        });

        flightPlan.addWaypoints(waypoints);
    }

    public NavigraphFlightPlan getFlightPlan() {
        logger.info("Started loading Navigraph flight plan");
        try {
            final boolean useNavigraphApi = settingsDao.useNavigraphConnection();
            final JsonNode root = getRootNode(useNavigraphApi);
            final NavigraphFlightPlan flightPlan = new NavigraphFlightPlan();

            setGeneralDetails(root, flightPlan);
            setOriginDetails(root, flightPlan);
            setDestinationDetails(root, flightPlan);
            setAircraftDetails(root, flightPlan);
            setWaypointDetails(root, flightPlan);

            logger.info("Finished loading Navigraph flight plan");

            return flightPlan;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPlannedFlightDistance(NavigraphFlightPlan flightPlan) {
        return flightPlan.getWaypoints().stream()
                .map(Waypoint::getDistance)
                .reduce(0, Integer::sum);
    }

    private JsonNode getRootNode(final boolean useNavigraphApi) throws IOException, InterruptedException {
        if (useNavigraphApi) {
            final String simbriefUser = settingsDao.getSimbriefUsername(); //Gavin194

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SIMBRIEF_URI + "?username=" + simbriefUser))
                    .GET()
                    .build();

            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return xmlMapper.readTree(response.body());
        } else {
            try (InputStream inputStream =
                         NavigraphService.class.getClassLoader().getResourceAsStream("test-data/simbrief.xml")) {
                return xmlMapper.readTree(inputStream);
            }
        }
    }
}
