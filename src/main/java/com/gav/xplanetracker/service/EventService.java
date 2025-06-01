package com.gav.xplanetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dao.FlightEventDaoJDBC;
import com.gav.xplanetracker.dto.xplane.*;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // TODO move this to a constants file
    public static final String XPLANE_API_ADDRESS = "http://localhost:8086/api/v2/datarefs";

    public static final String DENSITY_ALT_DATAREF_NAME = "sim/flightmodel2/position/pressure_altitude";
    public static final String GROUND_SPEED_DATAREF_NAME = "sim/cockpit2/gauges/indicators/ground_speed_kt";
    public static final String LATITUDE_DATAREF_NAME = "sim/flightmodel/position/latitude";
    public static final String LONGITUDE_DATAREF_NAME = "sim/flightmodel/position/longitude";
    public static final String HEADING_DATAREF_NAME = "sim/flightmodel/position/mag_psi";
    public static final String ENGINES_RUNNING_DATAREF_NAME = "sim/flightmodel/engine/ENGN_running";

    private static EventService INSTANCE;

    private final FlightEventDaoJDBC flightEventDao;
    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public EventService() {
        this.flightEventDao = FlightEventDaoJDBC.getInstance();
        this.client = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public static EventService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EventService();
        }
        return INSTANCE;
    }

    public void create(final Flight flight) {
        logger.info("Creating flight event for flight {}", flight.getId());

        // The IDs we need to query on are generated every time the simulator starts
        final XplaneDataRefListDTO dataRefs = getDataRefs();

        final FlightEvent event = new FlightEvent();
        event.setPressureAltitude(getPressureAltitude(dataRefs));
        event.setGroundSpeed(getGroundSpeed(dataRefs));
        event.setLatitude(getLatitude(dataRefs));
        event.setLongitude(getLongitude(dataRefs));
        event.setHeading(getHeading(dataRefs));
        event.setEnginesRunning(getEngineStatus(dataRefs));
        event.setCreatedAt(Instant.now());
        event.setFlightId(flight.getId());

        flightEventDao.create(event);
    }

    public String getAircraftRegistration() {
        logger.info("Fetching aircraft registration from simulator");
        final XplaneDataRefListDTO dataRefs = getDataRefs();

        // TODO work out how to get this properly from the sim
        return null;
    }

    public XplaneDataRefListDTO getDataRefs() {
        final String datarefUri = XPLANE_API_ADDRESS +
                "?filter[name]=" + DENSITY_ALT_DATAREF_NAME +
                "&filter[name]=" + GROUND_SPEED_DATAREF_NAME +
                "&filter[name]=" + LATITUDE_DATAREF_NAME +
                "&filter[name]=" + LONGITUDE_DATAREF_NAME +
                "&filter[name]=" + HEADING_DATAREF_NAME +
                "&filter[name]=" + ENGINES_RUNNING_DATAREF_NAME;

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(datarefUri))
                .GET()
                .build();

        try {
            final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), XplaneDataRefListDTO.class);
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching dataRefs: ", e);
            throw new RuntimeException(e);
        }
    }

    public double getPressureAltitude(XplaneDataRefListDTO dataRefs) {
        final String densityAltDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(DENSITY_ALT_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(densityAltDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching pressure altitude: ", e);
            throw new RuntimeException(e);
        }
    }

    public double getGroundSpeed(XplaneDataRefListDTO dataRefs) {
        final String groundSpeedDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(GROUND_SPEED_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(groundSpeedDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching ground speed: ", e);
            throw new RuntimeException(e);
        }
    }

    public double getLatitude(XplaneDataRefListDTO dataRefs) {
        final String latitudeDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(LATITUDE_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(latitudeDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching latitude: ", e);
            throw new RuntimeException(e);
        }
    }

    public double getLongitude(XplaneDataRefListDTO dataRefs) {
        final String longitudeDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(LONGITUDE_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(longitudeDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching longitude: ", e);
            throw new RuntimeException(e);
        }
    }

    public double getHeading(XplaneDataRefListDTO dataRefs) {
        final String longitudeDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(HEADING_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(longitudeDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No heading returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching tail number: ", e);
            throw new RuntimeException(e);
        }
    }

    public boolean getEngineStatus(XplaneDataRefListDTO dataRefs) {
        final String enginesRunningDataRefId = dataRefs.getData().stream()
                .filter(dataRef -> dataRef.getName().equals(ENGINES_RUNNING_DATAREF_NAME))
                .findAny()
                .map(XplaneDataRefDTO::getId)
                .orElseThrow(IllegalArgumentException::new);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xPlaneDataRefReqUri(enginesRunningDataRefId)))
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            final XplaneApiListResponse response = objectMapper.readValue(httpResponse.body(), XplaneApiListResponse.class);

            if (response != null) {
                return Arrays.stream(response.data())
                        .anyMatch(number -> number == 1);
            }

            throw new RuntimeException("No heading returned from simulator API");
        } catch (IOException | InterruptedException e) {
            logger.error("Error when fetching tail number: ", e);
            throw new RuntimeException(e);
        }
    }

    private String xPlaneDataRefReqUri(String dataRefId) {
        return XPLANE_API_ADDRESS + "/" + dataRefId + "/value";
    }
}
