package com.gav.xplanetracker.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gav.xplanetracker.dto.navigraph.NavigraphFlightPlan;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class NavigraphService {

    private final OkHttpClient client;
    private final XmlMapper xmlMapper;

    public NavigraphService() {
        this.client = new OkHttpClient();
        this.xmlMapper = new XmlMapper();
        configureXmlMapper();
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
        final String departureAirport = originNode.path("icao_code").asText();

        System.out.println("Origin - departure airport - " + departureAirport);

        flightPlan.setDepartureAirport(departureAirport);
    }

    private void setDestinationDetails(final JsonNode navigraphResponse, final NavigraphFlightPlan flightPlan) {
        final JsonNode destinationNode = navigraphResponse.path("destination");
        final String arrivalAirport = destinationNode.path("icao_code").asText();

        System.out.println("Destination - arrival airport - " + arrivalAirport);

        flightPlan.setArrivalAirport(arrivalAirport);
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

    public NavigraphFlightPlan getFlightPlan() {
        System.out.println("Started loading Navigraph flight plan");

        //TODO externalise this
        final Request request = new Request.Builder()
                .url("https://www.simbrief.com/api/xml.fetcher.php?username=Gavin194")
                .build();

        try {
            final Response response = client.newCall(request).execute();
            final JsonNode root = xmlMapper.readTree(response.body().string());
            final NavigraphFlightPlan flightPlan = new NavigraphFlightPlan();

            setGeneralDetails(root, flightPlan);
            setOriginDetails(root, flightPlan);
            setDestinationDetails(root, flightPlan);
            setAircraftDetails(root, flightPlan);

            System.out.println("Finished loading Navigraph flight plan");

            return flightPlan;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
