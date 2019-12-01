package net.dm73.plainpress.model;


import com.google.android.gms.maps.model.Marker;

public class EventMarker {

    private Marker marker;
    private String imageUrl;
    private String titleEvent;
    private String locationEvent;
    private String descEvent;

    public EventMarker(Marker marker, String imageUrl, String titleEvent, String locationEvent, String descEvent) {
        this.marker = marker;
        this.imageUrl = imageUrl;
        this.titleEvent = titleEvent;
        this.locationEvent = locationEvent;
        this.descEvent = descEvent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitleEvent() {
        return titleEvent;
    }

    public String getLocationEvent() {
        return locationEvent;
    }

    public String getDescEvent() {
        return descEvent;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitleEvent(String titleEvent) {
        this.titleEvent = titleEvent;
    }

    public void setLocationEvent(String locationEvent) {
        this.locationEvent = locationEvent;
    }

    public void setDescEvent(String descEvent) {
        this.descEvent = descEvent;
    }
}
