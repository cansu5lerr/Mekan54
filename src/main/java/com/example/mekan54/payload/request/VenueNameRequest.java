package com.example.mekan54.payload.request;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VenueNameRequest {
    private String venueName;

    public String getVenueName() {
        return venueName;
    }
   @JsonCreator
    public VenueNameRequest(@JsonProperty("venueName") String venueName) {
        this.venueName = venueName;
    }
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}
