package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initializeMap();
        setupInsets();
        setupButtons();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtons() {
        Button yeniButton = findViewById(R.id.yeni_button);
        yeniButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NewRoutaActivity.class)));

        Button rotalarButton = findViewById(R.id.rotalar_button);
        rotalarButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RoutasActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        setupMap();
    }

    private void setupMap() {
        myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        LatLng dekeli = new LatLng(36, 33);
        myMap.addMarker(new MarkerOptions().position(dekeli).title("Dekeli"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(dekeli));
    }
}
