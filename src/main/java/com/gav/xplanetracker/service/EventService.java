package com.gav.xplanetracker.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dao.FlightEventDaoJDBC;
import com.gav.xplanetracker.dto.xplane.*;
import com.gav.xplanetracker.model.Flight;
import com.gav.xplanetracker.model.FlightEvent;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.time.Instant;

public class EventService {

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
        System.out.println("Creating flight event for flight " + flight.getId());

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
            final ResponseBody body = client.newCall(request)
                    .execute()
                    .body();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneDataRefListDTO dto = mapper.readValue(body.string(), XplaneDataRefListDTO.class);
            body.close();

            return dto;
        } catch (IOException e) {
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
            final ResponseBody body = client.newCall(request)
                    .execute()
                    .body();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);
            body.close();

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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
            final ResponseBody body = client.newCall(request)
                    .execute()
                    .body();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);
            body.close();

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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
            final ResponseBody body = client.newCall(request)
                    .execute()
                    .body();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);
            body.close();

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
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
            final ResponseBody body = client.newCall(request)
                    .execute()
                    .body();
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);
            body.close();

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String xPlaneDataRefReqUri(String dataRefId) {
        return XPLANE_API_ADDRESS + "/" + dataRefId + "/value";
    }
}
