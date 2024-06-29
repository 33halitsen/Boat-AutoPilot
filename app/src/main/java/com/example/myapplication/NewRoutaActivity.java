package com.example.myapplication;

import static com.example.myapplication.Helpers.MapHelper.initializeMapFragment;
import static com.example.myapplication.Helpers.MapDbHelper.saveRoute;
import static com.example.myapplication.Helpers.MapHelper.updatePolyline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class NewRoutaActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;
    private List<Marker> markerList = new ArrayList<>();
    private Polyline polyline;

    private TextInputLayout routeNameLayout;
    private TextInputEditText routeNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newrouta);

        initializeMapFragment(NewRoutaActivity.this, getSupportFragmentManager());
        initializeUIElements();

        Button saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(v -> {
            saveRoute(NewRoutaActivity.this, routeNameLayout, routeNameEditText, markerList);
            Intent intent = new Intent(NewRoutaActivity.this, MainActivity.class);
            startActivity(intent);        });
    }

    private void initializeUIElements() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        routeNameLayout = findViewById(R.id.rota_ismi_1);
        routeNameEditText = findViewById(R.id.rota_ismi_2);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerDragListener(this);
        googleMap.setOnMarkerClickListener(this);

        polyline = googleMap.addPolyline(new PolylineOptions().clickable(false));
        LatLng initialPosition = new LatLng(36.11, 33.11);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 15.0f));
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Location").draggable(true));
        if (marker != null) {
            markerList.add(marker);
            updatePolyline(polyline, markerList);
            Toast.makeText(NewRoutaActivity.this, "Location added: ", Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        // Optional: Handle marker drag start event
    }
    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        updatePolyline(polyline, markerList);    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        updatePolyline(polyline, markerList);        int index = markerList.indexOf(marker);
        if (index != -1) {
            markerList.set(index, marker);
            updatePolyline(polyline, markerList);        }
        Toast.makeText(NewRoutaActivity.this, "Location updated: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        marker.remove();
        markerList.remove(marker);
        updatePolyline(polyline, markerList);
        Toast.makeText(NewRoutaActivity.this, "Location removed: ", Toast.LENGTH_SHORT).show();
        return true;
    }
}
