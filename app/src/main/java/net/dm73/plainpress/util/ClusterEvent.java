package net.dm73.plainpress.util;


import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import net.dm73.plainpress.model.Event;

public class ClusterEvent implements ClusterItem{

    private final LatLng mPosition;
    private BitmapDescriptor bitmapDescriptorFactory;
    private Event event;
    private String key;

    public ClusterEvent(LatLng mPosition, BitmapDescriptor bitmapDescriptorFactory, Event event, String key) {
        this.mPosition = mPosition;
        this.bitmapDescriptorFactory = bitmapDescriptorFactory;
        this.event = event;
        this.key = key;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public BitmapDescriptor getIcon(){
        return bitmapDescriptorFactory;
    }

    public Event getEvent(){
        return event;
    }

    public String getKey(){
        return key;
    }
}

