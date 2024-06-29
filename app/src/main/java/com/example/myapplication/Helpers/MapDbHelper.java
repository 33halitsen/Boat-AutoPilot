package com.example.myapplication.Helpers;

import static com.example.myapplication.Helpers.MapHelper.clearRouteInfo;
import static com.example.myapplication.Helpers.MapHelper.displayRouteMarkers;
import static com.example.myapplication.Helpers.MapHelper.updateRouteInfo;
import static com.example.myapplication.Helpers.MapHelper.zoomToRoute;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class MapDbHelper {
    public static RoutaClass routaObject;
    public static void saveRoute(Context context, TextInputLayout routeNameLayout, TextInputEditText routeNameEditText, List<Marker> markerList) {
        List<MarkerOptions> markerOptionsList = new ArrayList<>();
        String routeName = routeNameEditText.getText().toString().trim();

        if (routeName.isEmpty()) {
            routeNameLayout.setError("Route name is required!");
            return;
        }

        routeNameLayout.setError(null);

        for (Marker marker : markerList) {
            if (marker.getPosition() != null) {
                markerOptionsList.add(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .draggable(false));
            } else {
                Toast.makeText(context, "Invalid marker position!", Toast.LENGTH_SHORT).show();
            }
        }

        if (!markerOptionsList.isEmpty()) {
            RoutaDbHelper dbHelper = new RoutaDbHelper(context);
            dbHelper.addRoute(markerOptionsList, routeName);
        } else {
            Toast.makeText(context, "Please add valid markers!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void saveRoute(Context context, TextInputEditText routeNameEditText, List<Marker> markerList, int routaid) {
        List<MarkerOptions> markerOptionsList = new ArrayList<>();
        String routeName = routeNameEditText.getText().toString().trim();

        for (Marker marker : markerList) {
            if (marker.getPosition() != null) {
                markerOptionsList.add(new MarkerOptions()
                        .position(marker.getPosition())
                        .title(marker.getTitle())
                        .draggable(false));
            } else {
                Toast.makeText(context, "Invalid marker position!", Toast.LENGTH_SHORT).show();
            }
        }

        if (!markerOptionsList.isEmpty()) {
            RoutaDbHelper dbHelper = new RoutaDbHelper(context);
            dbHelper.updateRoute(markerOptionsList, routeName, routaid);
        } else {
            Toast.makeText(context, "Please add valid markers!", Toast.LENGTH_SHORT).show();
        }
    }
    public static Polyline loadRouteData(Context context, GoogleMap myMap, RoutaDbHelper dbHelper, TextView routeNameTextView, TextView routeDetailsTextView, List<Marker> markerList, Polyline polyline, int routeId) {
        routaObject = dbHelper.getRouteById(routeId);
        if (routaObject != null) {
            List<MarkerOptions> markerOptionsList = routaObject.getMarkers();
            if (markerOptionsList != null && !markerOptionsList.isEmpty()) {
                polyline = displayRouteMarkers(myMap, markerOptionsList, polyline, markerList, false);
                if (routeDetailsTextView != null){updateRouteInfo(routaObject, routeNameTextView, routeDetailsTextView, routeId);}
                else{updateRouteInfo(routeNameTextView, routeId);}
                if (markerList != null) {
                    zoomToRoute(myMap, markerList);
                }
            } else {
                Toast.makeText(context, "No markers found for the route", Toast.LENGTH_SHORT).show();
                if (routeDetailsTextView != null){clearRouteInfo(routeNameTextView, routeDetailsTextView);}
                else{clearRouteInfo(routeNameTextView);}
            }
        } else {
            Toast.makeText(context, "Route not found", Toast.LENGTH_SHORT).show();
            if (routeDetailsTextView != null){clearRouteInfo(routeNameTextView, routeDetailsTextView);}
            else{clearRouteInfo(routeNameTextView);}
        }
        if (routaObject != null) {
            routaObject.cleanup();
        }
        return polyline;
    }

    public static Polyline loadRouteData(Context context, GoogleMap myMap, RoutaDbHelper dbHelper, TextInputEditText routeNameEditText, List<Marker> markerList, Polyline polyline, int routeId) {
        routaObject = dbHelper.getRouteById(routeId);
        if (routaObject != null) {
            List<MarkerOptions> markerOptionsList = routaObject.getMarkers();
            if (markerOptionsList != null && !markerOptionsList.isEmpty()) {
                polyline = displayRouteMarkers(myMap, markerOptionsList, polyline, markerList, true);
                routeNameEditText.setText(routaObject.getRouteName());
                zoomToRoute(myMap, markerList);
            } else {
                Toast.makeText(context, "No markers found for the route", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Route not found", Toast.LENGTH_SHORT).show();
        }
        routaObject.cleanup();
        return polyline;
    }
}
