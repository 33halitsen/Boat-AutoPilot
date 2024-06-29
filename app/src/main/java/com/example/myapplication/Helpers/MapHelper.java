package com.example.myapplication.Helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapHelper {

    private static Marker userMarker;
    private static LocationManager locationManager;
    private static LocationListener locationListener;

    public static void initializeMapFragment(Context context, FragmentManager fragmentManager) {
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync((OnMapReadyCallback) context);
        }
    }

    public static Polyline displayRouteMarkers(GoogleMap myMap, List<MarkerOptions> markerOptionsList, Polyline polyline, List<Marker> markerList, boolean draggable) {
        clearMarkers(markerList, polyline);
        polyline = myMap.addPolyline(new PolylineOptions());
        for (MarkerOptions markerOptions : markerOptionsList) {
            markerOptions.draggable(draggable);
            Marker marker = myMap.addMarker(markerOptions);
            markerList.add(marker);
        }
        return updatePolyline(polyline, markerList);
    }

    public static void updateRouteInfo(RoutaClass routaObject, TextView routeNameTextView, TextView routeDetailsTextView, int routeId) {
        int displayedRouteId = routeId + 1;
        routeNameTextView.setText("Route " + displayedRouteId);
        routeDetailsTextView.setText(routaObject.getRouteName());
    }

    public static void updateRouteInfo(TextView routeNameTextView, int routeId) {
        int displayedRouteId = routeId + 1;
        routeNameTextView.setText("Route " + displayedRouteId);
    }

    public static void clearRouteInfo(TextView routeNameTextView, TextView routeDetailsTextView) {
        routeNameTextView.setText("");
        routeDetailsTextView.setText("");
    }

    public static void clearRouteInfo(TextView routeNameTextView) {
        routeNameTextView.setText("");
    }

    public static void clearMarkers(List<Marker> markerList, Polyline polyline) {
        for (Marker marker : markerList) {
            marker.remove();
        }
        markerList.clear();
        if (polyline != null) {
            polyline.remove();
        }
    }

    public static Polyline updatePolyline(Polyline polyline, List<Marker> markerList) {
        List<LatLng> points = new ArrayList<>();
        for (Marker marker : markerList) {
            points.add(marker.getPosition());
        }
        polyline.setPoints(points);
        return polyline;
    }

    public static void zoomToRoute(GoogleMap myMap, List<Marker> markerList) {
        double averageLat = 0, averageLng = 0;
        for (Marker marker : markerList) {
            averageLat += marker.getPosition().latitude;
            averageLng += marker.getPosition().longitude;
        }
        averageLat /= markerList.size();
        averageLng /= markerList.size();

        LatLng position = new LatLng(averageLat, averageLng);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14.0f));
    }

    // Yeni eklenen fonksiyon: Konumu haritada işaretleyen ve eski işaretlemeyi kaldıran fonksiyon
    public static void updateMarkerOnMap(GoogleMap myMap, LatLng newLocation, LatLng oldLocation) {
        if (oldLocation != null && userMarker != null) {
            userMarker.remove(); // Eski işaretlemeyi kaldır
        }
        MarkerOptions markerOptions = new MarkerOptions().position(newLocation).title("Güncel Konum");
        userMarker = myMap.addMarker(markerOptions);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 14.0f));
    }
    public static void startLocationUpdates(final Context context, final GoogleMap myMap) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
                updateMarkerOnMap(myMap, newLocation, userMarker != null ? userMarker.getPosition() : null);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener); // 5 saniyede bir veya 10 metre değişiklikte güncelle
    }

    // Konum güncellemelerini durdurmak için fonksiyon
    public static void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}
