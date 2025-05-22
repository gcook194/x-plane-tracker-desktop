package com.gav.xplanetracker.dto.xplane;

import java.util.ArrayList;
import java.util.List;

public class XplaneDataRefListDTO {

    private List<XplaneDataRefDTO> data;

    public XplaneDataRefListDTO() {
        this.data = new ArrayList<>();
    }

    public List<XplaneDataRefDTO> getData() {
        return data;
    }

    public void setData(List<XplaneDataRefDTO> data) {
        this.data = data;
    }
}
