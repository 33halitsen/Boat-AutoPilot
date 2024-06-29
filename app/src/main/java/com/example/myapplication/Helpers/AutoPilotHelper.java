package com.example.myapplication.Helpers;

import static java.lang.Math.toRadians;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class AutoPilotHelper {
    private static LocationManager locationManager;
    public static int calculateAzimuth(LatLng startPoint, LatLng endPoint) {
        double dLon = toRadians(endPoint.longitude - startPoint.longitude);

        double latcRad = toRadians(startPoint.latitude);
        double lattRad = toRadians(endPoint.latitude);

        double x = Math.cos(lattRad) * Math.sin(dLon);
        double y = Math.cos(latcRad) * Math.sin(lattRad) -
                Math.sin(latcRad) * Math.cos(lattRad) * Math.cos(dLon);

        double azimuth = Math.atan2(x, y);
        azimuth = Math.toDegrees(azimuth);
        azimuth = (azimuth + 360) % 360;

        return (int) azimuth;
    }
    public static double calculateDistance(LatLng point1, LatLng point2) {
        // LatLng noktalarının koordinatları alınıyor
        double lat1 = point1.latitude;
        double lon1 = point1.longitude;
        double lat2 = point2.latitude;
        double lon2 = point2.longitude;

        // Radyan cinsinden koordinatlar
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formülü için
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Yeryüzü için ortalama yarıçapı (km cinsinden)
        double earthRadius = 6371;

        // Uzaklık hesaplanıyor
        double distance = earthRadius * c;

        return distance * 1000; // metre cinsinden mesafe
    }
    public static LatLng getCurrentLocation(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return null;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            Toast.makeText(context, "Konum alınamadı", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    // Anlık konum ile bir Marker arasındaki mesafeyi hesaplayan fonksiyon
    public static double calculateDistanceToMarker(Context context, Marker marker) {
        LatLng currentLocation = getCurrentLocation(context);
        if (currentLocation != null) {
            LatLng markerPosition = marker.getPosition();
            return calculateDistance(currentLocation, markerPosition);
        } else {
            // Eğer konum alınamazsa -1 veya başka bir hata durumu döndürebilirsiniz.
            return -1;
        }
    }
    public static int checkorder(Context context, List<Marker> markerlist, int index){
       if (calculateDistanceToMarker(context, markerlist.get(index)) < 2){
           if (index + 1 > markerlist.size()){return 0;}
           else {return index + 1;}
       }
       else {return index;}
    }
}
