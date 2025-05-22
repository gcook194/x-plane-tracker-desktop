package com.gav.xplanetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dao.FlightEventDaoJDBC;
import com.gav.xplanetracker.dto.xplane.XPDataRefDTO;
import com.gav.xplanetracker.dto.xplane.XpDataRefListDTO;
import com.gav.xplanetracker.dto.xplane.XplaneApiResponse;
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
        final XpDataRefListDTO dataRefs = getDataRefs();

        final FlightEvent event = new FlightEvent();
        event.setPressureAltitude(getPressureAltitude(dataRefs));
        event.setGroundSpeed(getGroundSpeed(dataRefs));
        event.setLatitude(getLatitude(dataRefs));
        event.setLongitude(getLongitude(dataRefs));
        event.setCreatedAt(Instant.now());

        flightEventDao.create(event);
    }

    public XpDataRefListDTO getDataRefs() {
        final String datarefUri = XPLANE_API_ADDRESS +
                "?filter[name]=" + DENSITY_ALT_DATAREF_NAME +
                "&filter[name]=" + GROUND_SPEED_DATAREF_NAME +
                "&filter[name]=" + LATITUDE_DATAREF_NAME +
                "&filter[name]=" + LONGITUDE_DATAREF_NAME;

        final Request request = new Request.Builder()
                .url(datarefUri)
                .build();

        try (ResponseBody body = client.newCall(request).execute().body()) {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body.string(), XpDataRefListDTO.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getPressureAltitude(XpDataRefListDTO dataRefs) {
        final String densityAltDataRefId = dataRefs.data().stream()
                .filter(dataRef -> dataRef.name().equals(DENSITY_ALT_DATAREF_NAME))
                .findAny()
                .map(XPDataRefDTO::id)
                .orElseThrow(IllegalArgumentException::new);

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(densityAltDataRefId))
                .build();

        try (
                ResponseBody body = client.newCall(request)
                .execute()
                .body()
        ) {
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getGroundSpeed(XpDataRefListDTO dataRefs) {
        final String groundSpeedDataRefId = dataRefs.data().stream()
                .filter(dataRef -> dataRef.name().equals(GROUND_SPEED_DATAREF_NAME))
                .findAny()
                .map(XPDataRefDTO::id)
                .orElseThrow(IllegalArgumentException::new);

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(groundSpeedDataRefId))
                .build();

        try (
                ResponseBody body = client.newCall(request)
                        .execute()
                        .body()
        ) {
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getLatitude(XpDataRefListDTO dataRefs) {
        final String latitudeDataRefId = dataRefs.data().stream()
                .filter(dataRef -> dataRef.name().equals(LATITUDE_DATAREF_NAME))
                .findAny()
                .map(XPDataRefDTO::id)
                .orElseThrow(IllegalArgumentException::new);

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(latitudeDataRefId))
                .build();

        try (
                ResponseBody body = client.newCall(request)
                        .execute()
                        .body()
        ) {
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);

            if (response != null) {
                return Double.parseDouble(response.data());
            }

            throw new RuntimeException("No density altitude returned from simulator API");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double getLongitude(XpDataRefListDTO dataRefs) {
        final String longitudeDataRefId = dataRefs.data().stream()
                .filter(dataRef -> dataRef.name().equals(LONGITUDE_DATAREF_NAME))
                .findAny()
                .map(XPDataRefDTO::id)
                .orElseThrow(IllegalArgumentException::new);

        final Request request = new Request.Builder()
                .url(xPlaneDataRefReqUri(longitudeDataRefId))
                .build();

        try (
                ResponseBody body = client.newCall(request)
                        .execute()
                        .body()
        ) {
            final ObjectMapper mapper = new ObjectMapper();
            final XplaneApiResponse response = mapper.readValue(body.string(), XplaneApiResponse.class);

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
