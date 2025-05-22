package com.gav.xplanetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gav.xplanetracker.dto.xplane.XpDataRefListDTO;
import com.gav.xplanetracker.model.Flight;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

public class EventService {

    // TODO move this to a constants file
    public static final String XPLANE_API_ADDRESS = "http://localhost:8086/api/v2/datarefs";

    public static final String DENSITY_ALT_DATAREF_NAME = "sim/flightmodel2/position/pressure_altitude";
    public static final String GROUND_SPEED_DATAREF_NAME = "sim/cockpit2/gauges/indicators/ground_speed_kt";
    public static final String LATITUDE_DATAREF_NAME = "sim/flightmodel/position/latitude";
    public static final String LONGITUDE_DATAREF_NAME = "sim/flightmodel/position/longitude";

    private static EventService INSTANCE;

    private final OkHttpClient client;

    public EventService() {
        client = new OkHttpClient();
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

//    public void create(final Flight flight) {
//        System.out.println("Creating flight event for flight " + flight.getId());
//
//        // The IDs we need to query on are generated every time the simulator starts
//        final XpDataRefListDTO dataRefs = getDataRefs();
//
//        final FlightEvent event = FlightEvent.builder()
//                .pressureAltitude(getPressureAltitude(dataRefs))
//                .groundSpeed(getGroundSpeed(dataRefs))
//                .latitude(getLatitude(dataRefs))
//                .longitude(getLongitude(dataRefs))
//                .build();
//
//        log.info(new ObjectMapper()
//                .configure(SerializationFeature.INDENT_OUTPUT, true)
//                .writeValueAsString(event)
//        );
//
//        flight.addEvent(event);
//        flightRepository.save(flight);
//    }
//
//    public XpDataRefListDTO getDataRefs() {
//        final String datarefUri = XPLANE_API_ADDRESS +
//                "?filter[name]=" + DENSITY_ALT_DATAREF_NAME +
//                "&filter[name]=" + GROUND_SPEED_DATAREF_NAME +
//                "&filter[name]=" + LATITUDE_DATAREF_NAME +
//                "&filter[name]=" + LONGITUDE_DATAREF_NAME;
//
//        return restClient.get()
//                .uri(datarefUri)
//                .header("Content-Type", "application/json")
//                .retrieve()
//                .body(XpDataRefListDTO.class);
//    }
//
//    public double getPressureAltitude(XpDataRefListDTO dataRefs) {
//        final String densityAltDataRefId = dataRefs.data().stream()
//                .filter(dataRef -> dataRef.name().equals(DENSITY_ALT_DATAREF_NAME))
//                .findAny()
//                .map(XPDataRefDTO::id)
//                .orElseThrow(IllegalArgumentException::new);
//
//        final XplaneApiResponse response = restClient.get()
//                .uri(xPlaneDataRefReqUri(densityAltDataRefId))
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, (req, res) -> {
//                    throw new XplaneApiResponseError("Could not retrieve density altitude");
//                })
//                .body(XplaneApiResponse.class);
//
//        if (response != null) {
//            return Double.parseDouble(response.data());
//        }
//
//        throw new RuntimeException("No density altitude returned from simulator API");
//    }
//
//    public double getGroundSpeed(XpDataRefListDTO dataRefs) {
//        final String groundSpeedDataRefId = dataRefs.data().stream()
//                .filter(dataRef -> dataRef.name().equals(GROUND_SPEED_DATAREF_NAME))
//                .findAny()
//                .map(XPDataRefDTO::id)
//                .orElseThrow(IllegalArgumentException::new);
//
//        final XplaneApiResponse response = restClient.get()
//                .uri(xPlaneDataRefReqUri(groundSpeedDataRefId))
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, (req, res) -> {
//                    throw new XplaneApiResponseError("Could not retrieve ground speed");
//                })
//                .body(XplaneApiResponse.class);
//
//        if (response != null) {
//            return Double.parseDouble(response.data());
//        }
//
//        throw new RuntimeException("No ground speed returned from simulator API");
//    }
//
//    public double getLatitude(XpDataRefListDTO dataRefs) {
//        final String latitudeDataRefId = dataRefs.data().stream()
//                .filter(dataRef -> dataRef.name().equals(LATITUDE_DATAREF_NAME))
//                .findAny()
//                .map(XPDataRefDTO::id)
//                .orElseThrow(IllegalArgumentException::new);
//
//        final XplaneApiResponse response = restClient.get()
//                .uri(xPlaneDataRefReqUri(latitudeDataRefId))
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, (req, res) -> {
//                    throw new XplaneApiResponseError("Could not retrieve latitude");
//                })
//                .body(XplaneApiResponse.class);
//
//        if (response != null) {
//            return Double.parseDouble(response.data());
//        }
//
//        throw new RuntimeException("No latitude returned from simulator API");
//    }
//
//    public double getLongitude(XpDataRefListDTO dataRefs) {
//        final String longitudeDataRefId = dataRefs.data().stream()
//                .filter(dataRef -> dataRef.name().equals(LONGITUDE_DATAREF_NAME))
//                .findAny()
//                .map(XPDataRefDTO::id)
//                .orElseThrow(IllegalArgumentException::new);
//
//        final XplaneApiResponse response = restClient.get()
//                .uri(xPlaneDataRefReqUri(longitudeDataRefId))
//                .retrieve()
//                .onStatus(HttpStatusCode::isError, (req, res) -> {
//                    throw new XplaneApiResponseError("Could not retrieve longitude");
//                })
//                .body(XplaneApiResponse.class);
//
//        if (response != null) {
//            return Double.parseDouble(response.data());
//        }
//
//        throw new RuntimeException("No longitude returned from simulator API");
//    }

    private String xPlaneDataRefReqUri(String dataRefId) {
        return XPLANE_API_ADDRESS + "/" + dataRefId + "/value";
    }
}
