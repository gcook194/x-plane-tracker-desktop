package com.gav.xplanetracker.dto;

public record ApplicationSettingsDTO(String simbriefUsername, String xplaneHost, boolean useNavigraphApi, boolean monitorScreenshots, String xPlaneScreenshotDirectory) {

}
