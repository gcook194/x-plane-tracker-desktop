package com.gav.xplanetracker.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gav.xplanetracker.dao.SettingsDaoJDBC;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.gav.xplanetracker.dto.navigraph.Waypoint;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NavigraphService {

    public static final String SIMBRIEF_URI = "https://www.simbrief.com/api/xml.fetcher.php";
    private static NavigraphService INSTANCE;

    private final SettingsDaoJDBC settingsDao;
    private final OkHttpClient client;
    private final XmlMapper xmlMapper;

    public NavigraphService() {
        this.settingsDao = SettingsDaoJDBC.getInstance();
        this.client = new OkHttpClient();
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

        System.out.println("General - airline - " + airline);
        System.out.println("General - flight number - " + flightNumber);

        flightPlan.setFlightNumber(flightNumber);
        flightPlan.setIcaoAirline(airline);
    }

    private void setOriginDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode originNode = navigraphResponse.path("origin");
        final String icaoCode = originNode.path("icao_code").asText();
        final String name = originNode.path("name").textValue();
        final double latitude = originNode.path("pos_lat").asDouble();
        final double longitude = originNode.path("pos_long").asDouble();

        System.out.println("Origin - departure airport - " + icaoCode);

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

        System.out.println("Destination - arrival airport - " + icaoCode);

        flightPlan.getArrival().setIcaoCode(icaoCode);
        flightPlan.getArrival().setName(name);
        flightPlan.getArrival().setLatitude(latitude);
        flightPlan.getArrival().setLongitude(longitude);
    }

    private void setAircraftDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode aircraftNode = navigraphResponse.path("aircraft");
        final String aircraftType = aircraftNode.path("icaocode").asText();
        final String aircraftReg = aircraftNode.path("reg").asText();

        System.out.println("Aircraft - type - " + aircraftType);
        System.out.println("Aircraft - registration - " + aircraftReg);

        flightPlan.setAircraftType(aircraftType);
        flightPlan.setAircraftRegistration(aircraftReg);
    }

    private void setWaypointDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode waypointsNode = navigraphResponse.path("navlog");
        final List<Waypoint> waypoints = new ArrayList<>();

        System.out.println("Waypoints");

        waypointsNode.path("fix").elements().forEachRemaining(fix -> {
            final String name = fix.path("name").asText();
            final String stageOfFlight = fix.path("stage").asText();
            final String viaAirway = fix.path("via_airway").asText();
            final double latitude = fix.path("pos_lat").asDouble();
            final double longitude = fix.path("pos_long").asDouble();

            System.out.printf("\t %s, lat: %f, long: %f%n", name, latitude, longitude);

            final Waypoint waypoint = new Waypoint();
            waypoint.setName(name);
            waypoint.setStageOfFlight(stageOfFlight);
            waypoint.setViaAirway(viaAirway);
            waypoint.setLatitude(latitude);
            waypoint.setLongitude(longitude);

            waypoints.add(waypoint);
        });

        flightPlan.addWaypoints(waypoints);
    }

    public NavigraphFlightPlan getFlightPlan() {
        System.out.println("Started loading Navigraph flight plan");
        try {
            final boolean useNavigraphApi = settingsDao.useNavigraphConnection();
            final JsonNode root = getRootNode(useNavigraphApi);
            final NavigraphFlightPlan flightPlan = new NavigraphFlightPlan();

            setGeneralDetails(root, flightPlan);
            setOriginDetails(root, flightPlan);
            setDestinationDetails(root, flightPlan);
            setAircraftDetails(root, flightPlan);
            setWaypointDetails(root, flightPlan);

            System.out.println("Finished loading Navigraph flight plan");

            return flightPlan;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode getRootNode(final boolean useNavigraphApi) throws IOException {
        if (useNavigraphApi) {
            final String simbriefUser = settingsDao.getSimbriefUsername(); //Gavin194
            final Request request = new Request.Builder()
                    .url(SIMBRIEF_URI + "?username=" + simbriefUser)
                    .build();
            final Response response = client.newCall(request).execute();

            return xmlMapper.readTree(response.body().string());
        } else {
            try (InputStream inputStream =
                         NavigraphService.class.getClassLoader().getResourceAsStream("test-data/simbrief.xml")) {
                return xmlMapper.readTree(inputStream);
            }
        }
    }
}
