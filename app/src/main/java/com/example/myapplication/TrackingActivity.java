package com.example.myapplication;

import static com.example.myapplication.Helpers.AutoPilotHelper.calculateAzimuth;
import static com.example.myapplication.Helpers.AutoPilotHelper.checkorder;
import static com.example.myapplication.Helpers.AutoPilotHelper.getCurrentLocation;
import static com.example.myapplication.Helpers.BluetoothHelper.connectToBluetoothDevice;
import static com.example.myapplication.Helpers.BluetoothHelper.getServo;
import static com.example.myapplication.Helpers.BluetoothHelper.sendServo;
import static com.example.myapplication.Helpers.MapDbHelper.loadRouteData;
import static com.example.myapplication.Helpers.MapHelper.startLocationUpdates;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Helpers.BluetoothHelper;
import com.example.myapplication.Helpers.RoutaClass;
import com.example.myapplication.Helpers.RoutaDbHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "TakipActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int PERMISSION_REQUEST_CODE = 2;
    private RoutaClass routaObject;
    private GoogleMap myMap;
    private TextView routeNameTextView;
    private TextView routeDetailsTextView;
    private RoutaDbHelper dbHelper;
    private Polyline polyline;
    private List<Marker> markerList = new ArrayList<>();
    private int routeId;
    private LatLng currentlocation;
    private int index = 1;
    private LatLng target;
    private double targetazimuth;
    private double currentazimuth;
    private double servo;
    private List<Double> azimuthangle;
    private double difangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startTracking());

        routeNameTextView = findViewById(R.id.textView3);
        dbHelper = new RoutaDbHelper(this);

        Intent intent = getIntent();
        routeId = intent.getIntExtra("EXTRA_INT", 0);

        BluetoothHelper.checkPermissions(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        polyline = loadRouteData(TrackingActivity.this, myMap, dbHelper, routeNameTextView, routeDetailsTextView, markerList, polyline, routeId);
        startLocationUpdates(this, myMap);
    }

    private void startTracking() {
        connectToBluetoothDevice(this);
        currentazimuth = BluetoothHelper.getCurrentAzimuth();
        currentlocation = getCurrentLocation(this);
        index = checkorder(this, markerList, index);
        target = markerList.get(index).getPosition();
        targetazimuth = calculateAzimuth(currentlocation, target);
        servo = getServo();
        azimuthangle.remove(0);
        azimuthangle.add(currentazimuth - targetazimuth);
        difangle = Math.abs(azimuthangle.get(1) - azimuthangle.get(0));
        if (Math.abs(azimuthangle.get(1)) > 80) {
            if (azimuthangle.get(1) > 80){sendServo(80);}
            if (azimuthangle.get(1) < -80){sendServo(-80);}
        }
        else if (Math.abs(azimuthangle.get(1)) > 40) {
            sendServo((int) Math.floor((4/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
        else if (Math.abs(azimuthangle.get(1)) > 20) {
            sendServo((int) Math.floor((2/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
        else if (Math.abs(azimuthangle.get(1)) > 10) {
            sendServo((int) Math.floor((1/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
        else if (Math.abs(azimuthangle.get(1)) > 5) {
            sendServo((int) Math.floor((0.5/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
        else if (Math.abs(azimuthangle.get(1)) > 2) {
            sendServo((int) Math.floor((0.1/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
        else if (Math.abs(azimuthangle.get(1)) < 0.5) {
            sendServo((int) Math.floor((0.01/difangle)*servo));
            if (azimuthangle.get(0)*azimuthangle.get(1) < 0){sendServo((int) (-servo));}
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothHelper.disconnectBluetoothDevice();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            } else {
                Toast.makeText(this, "Permission denied. Cannot proceed with Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startTracking();
            } else {
                Toast.makeText(this, "Bluetooth activation cancelled by user", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
