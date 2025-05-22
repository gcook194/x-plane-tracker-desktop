package com.gav.xplanetracker.dto.xplane;

import com.fasterxml.jackson.annotation.JsonProperty;

public class XplaneDataRefDTO {

    private String id;
    private boolean isWritable;
    private String name;
    private String valueType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isWritable() {
        return isWritable;
    }

    @JsonProperty("is_writable")
    public void setWritable(boolean writable) {
        isWritable = writable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueType() {
        return valueType;
    }

    @JsonProperty("value_type")
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}
