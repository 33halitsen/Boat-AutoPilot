package com.example.myapplication.Helpers;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class RoutaClass {

    private String route_name;
    private List<MarkerOptions> markers;

    public RoutaClass(String routeName, List<MarkerOptions> markers) {
        this.route_name = routeName;
        this.markers = markers;
    }
    public RoutaClass() {
    }
    // Getter metotları
    public String getRouteName() {
        return route_name;
    }

    public List<MarkerOptions> getMarkers() {
        return markers;
    }
    public void setRouteName(String routeName) {
        this.route_name = routeName;
    }
    public void setMarkers(List<MarkerOptions> markers) {
        this.markers = markers;
    }
    public void cleanup() {
        // Perform any necessary cleanup operations here
        // For example, clearing the markers list
        if (markers != null) {
            markers.clear();
        }
        route_name = null;
    }
}
