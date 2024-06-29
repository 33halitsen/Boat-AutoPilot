package com.example.myapplication.Helpers;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
public class JsonHelper {
    public static String convertToJSON(List<MarkerOptions> markers) {
        JSONArray jsonArray = new JSONArray();
        try {
            for (MarkerOptions marker : markers) {
                JSONObject markerObject = new JSONObject();
                markerObject.put("latitude", marker.getPosition().latitude);
                markerObject.put("longitude", marker.getPosition().longitude);
                markerObject.put("title", marker.getTitle());
                markerObject.put("snippet", marker.getSnippet());
                jsonArray.put(markerObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    public static List<MarkerOptions> convertToMarkerOptions(String jsonString) {
        List<MarkerOptions> markers = new ArrayList<>();

        if (jsonString == null || jsonString.isEmpty()) {
            System.out.println("JSON string is null or empty");
            return markers;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject markerObject = jsonArray.getJSONObject(i);
                if (markerObject.has("latitude") && markerObject.has("longitude")) {
                    double latitude = markerObject.getDouble("latitude");
                    double longitude = markerObject.getDouble("longitude");
                    String title = markerObject.optString("title", "");
                    String snippet = markerObject.optString("snippet", "");

                    LatLng position = new LatLng(latitude, longitude);
                    MarkerOptions marker = new MarkerOptions()
                            .position(position)
                            .title(title)
                            .snippet(snippet);
                    markers.add(marker);
                } else {
                    System.out.println("Marker object does not have required fields: latitude and longitude");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return markers;
    }
}
