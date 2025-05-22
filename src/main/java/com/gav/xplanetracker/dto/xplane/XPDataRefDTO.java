package com.gav.xplanetracker.dto.xplane;

import com.fasterxml.jackson.annotation.JsonProperty;

public record XPDataRefDTO(String id, @JsonProperty("is_writeable") boolean isWriteable, String name, @JsonProperty("value_type") String valueType) {
}
