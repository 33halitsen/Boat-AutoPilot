package com.example.myapplication;

import static com.example.myapplication.Helpers.MapHelper.initializeMapFragment;
import static com.example.myapplication.Helpers.MapDbHelper.loadRouteData;
import static com.example.myapplication.Helpers.MapDbHelper.saveRoute;
import static com.example.myapplication.Helpers.MapHelper.updatePolyline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Helpers.RoutaClass;
import com.example.myapplication.Helpers.RoutaDbHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap myMap;
    private RoutaDbHelper dbHelper;
    private TextInputEditText routeNameEditText;
    private RoutaClass routaObject;
    private List<Marker> markerList;
    private Polyline polyline;
    private int routeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);

        initialize();
        initializeMapFragment(EditActivity.this, getSupportFragmentManager());
        Button saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(v -> {
            saveRoute(EditActivity.this, routeNameEditText, markerList, routeId);
            Intent intent = new Intent(EditActivity.this, RoutasActivity.class);
            intent.putExtra("EXTRA_INT", routeId);
            startActivity(intent);
        });
    }
    private void initialize() {
        dbHelper = new RoutaDbHelper(EditActivity.this);
        routaObject = new RoutaClass();
        markerList = new ArrayList<>();
        routeId = getIntent().getIntExtra("EXTRA_INT", 1000);
        routeNameEditText = findViewById(R.id.rota_ismi_2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        myMap = map;
        myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        myMap.setOnMapClickListener(this);
        myMap.setOnMarkerDragListener(this);
        myMap.setOnMarkerClickListener(this);
        polyline = loadRouteData(EditActivity.this, myMap, dbHelper, routeNameEditText, markerList, polyline, routeId);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Marker marker = myMap.addMarker(new MarkerOptions().position(latLng).title("Location").draggable(true));
        if (marker != null) {
            markerList.add(marker);
            updatePolyline(polyline, markerList);
            Toast.makeText(EditActivity.this, "Location added: ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        // Optional: actions on marker drag start
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        updatePolyline(polyline, markerList);
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        updatePolyline(polyline, markerList);
        Toast.makeText(EditActivity.this, "Location updated: ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        marker.remove();
        markerList.remove(marker);
        updatePolyline(polyline, markerList);
        Toast.makeText(EditActivity.this, "Location removed: ", Toast.LENGTH_SHORT).show();
        return true;
    }
}
