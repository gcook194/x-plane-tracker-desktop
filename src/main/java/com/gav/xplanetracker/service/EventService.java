package com.gav.xplanetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dao.FlightEventDaoJDBC;
import com.gav.xplanetracker.dto.xplane.*;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // TODO move this to a constants file
    public static final String XPLANE_API_ADDRESS = "http://localhost:8086/api/v2/datarefs";

    public static final String DENSITY_ALT_DATAREF_NAME = "sim/flightmodel2/position/pressure_altitude";
    public static final String GROUND_SPEED_DATAREF_NAME = "sim/cockpit2/gauges/indicators/ground_speed_kt";
    public static final String LATITUDE_DATAREF_NAME = "sim/flightmodel/position/latitude";
    public static final String LONGITUDE_DATAREF_NAME = "sim/flightmodel/position/longitude";

    private static EventService INSTANCE;

    private final FlightEventDaoJDBC flightEventDao;
    private final OkHttpClient client;

    public EventService() {
        this.flightEventDao = FlightEventDaoJDBC.getInstance();
        this.client = new OkHttpClient();
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
        event.setCreatedAt(Instant.now());
        event.setFlightId(flight.getId());

        flightEventDao.create(event);
    }

    public XplaneDataRefListDTO getDataRefs() {
        final String datarefUri = XPLANE_API_ADDRESS +
                "?filter[name]=" + DENSITY_ALT_DATAREF_NAME +
                "&filter[name]=" + GROUND_SPEED_DATAREF_NAME +
                "&filter[name]=" + LATITUDE_DATAREF_NAME +
                "&filter[name]=" + LONGITUDE_DATAREF_NAME;

        final Request request = new Request.Builder()
                .url(datarefUri)
                .build();

        try {
            final String body = client.newCall(request)
                    .execute()
                    .body()
                    .string();
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, XplaneDataRefListDTO.class);
        } catch (IOException e) {
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

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(densityAltDataRefId))
                .build();

        try {
            final String body = client.newCall(request)
                    .execute()
                    .body()
                    .string();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body, XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(groundSpeedDataRefId))
                .build();

        try {
            final String body = client.newCall(request)
                    .execute()
                    .body()
                    .string();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body, XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(latitudeDataRefId))
                .build();

        try {
            final String body = client.newCall(request)
                    .execute()
                    .body()
                    .string();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body, XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(longitudeDataRefId))
                .build();

        try {
            final String body = client.newCall(request)
                    .execute()
                    .body()
                    .string();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body, XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
            logger.error("Error when fetching longitude: ", e);
            throw new RuntimeException(e);
        }
    }

    private String xPlaneDataRefReqUri(String dataRefId) {
        return XPLANE_API_ADDRESS + "/" + dataRefId + "/value";
    }
}
