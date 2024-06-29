package com.example.myapplication;

import static com.example.myapplication.Helpers.MapHelper.initializeMapFragment;
import static com.example.myapplication.Helpers.MapDbHelper.loadRouteData;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Helpers.RoutaClass;
import com.example.myapplication.Helpers.RoutaDbHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class RoutasActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private Polyline polyline;
    private int routeId;
    private List<Marker> markerList = new ArrayList<>();
    private RoutaClass routaObject;
    private TextView routeNameTextView;
    private TextView routeDetailsTextView;
    private RoutaDbHelper dbHelper;
    private int maxRouteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routas);

        initUiComponents();
        initializeMapFragment(RoutasActivity.this, getSupportFragmentManager());
        setupWindowInsetsListener();
        setupButtonListeners();

        dbHelper = new RoutaDbHelper(RoutasActivity.this);
        maxRouteId = dbHelper.getLastItemId();
        routeId = getIntent().getIntExtra("EXTRA_INT", maxRouteId);
    }

    private void initUiComponents() {
        routeNameTextView = findViewById(R.id.routeNameTextView);
        routeDetailsTextView = findViewById(R.id.routeDetailsTextView);
    }

    private void setupWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtonListeners() {
        findViewById(R.id.previous_route_button).setOnClickListener(v -> handlePreviousRoute());
        findViewById(R.id.next_route_button).setOnClickListener(v -> handleNextRoute());
        findViewById(R.id.edit_button).setOnClickListener(v -> handleEditRoute());
        findViewById(R.id.remove_button).setOnClickListener(v -> handleRemoveRoute());
        findViewById(R.id.takip).setOnClickListener(v -> {
                Intent intent = new Intent(RoutasActivity.this, TrackingActivity.class);
                intent.putExtra("EXTRA_INT", routeId);
                startActivity(intent);
            });
    }

    private void handlePreviousRoute() {
        if (routeId > 0) {
            routeId--;
        } else {
            routeId = maxRouteId;
        }
        polyline =loadRouteData(RoutasActivity.this, myMap, dbHelper, routeNameTextView, routeDetailsTextView, markerList, polyline, routeId);
    }

    private void handleNextRoute() {
        if (routeId < maxRouteId) {
            routeId++;
        } else {
            routeId = 0;
        }
        polyline = loadRouteData(RoutasActivity.this, myMap, dbHelper, routeNameTextView, routeDetailsTextView, markerList, polyline, routeId);
    }

    private void handleEditRoute() {
        Intent intent = new Intent(RoutasActivity.this, EditActivity.class);
        intent.putExtra("EXTRA_INT", routeId);
        startActivity(intent);
    }

    private void handleRemoveRoute() {
        dbHelper.deleteRoute(routeId);
        maxRouteId--;

        if (maxRouteId < 0) {
            Toast.makeText(this, "All Routes were deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(RoutasActivity.this, MainActivity.class);
            intent.putExtra("EXTRA_INT", routeId);
            startActivity(intent);
        } else {
            handleNextRoute();
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        polyline = loadRouteData(RoutasActivity.this, myMap, dbHelper, routeNameTextView, routeDetailsTextView, markerList, polyline, routeId);
    }
}
