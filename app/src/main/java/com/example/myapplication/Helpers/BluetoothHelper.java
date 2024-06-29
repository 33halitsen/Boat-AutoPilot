package com.example.myapplication.Helpers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    private static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int PERMISSION_REQUEST_CODE = 2;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;
    private static OutputStream outputStream;
    private static InputStream inputStream;

    public static void checkPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                }, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public static void connectToBluetoothDevice(Activity activity) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            Toast.makeText(activity, "Already connected to HC-06 device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, PERMISSION_REQUEST_CODE);
            return;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("halit MacBook Air")) {
                bluetoothDevice = device;
                break;
            }
        }

        if (bluetoothDevice != null) {
            new Thread(() -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                            return;
                        }
                    }
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();
                    outputStream = bluetoothSocket.getOutputStream();
                    inputStream = bluetoothSocket.getInputStream();
                    while (true) {
                        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Bluetooth successfully connected", Toast.LENGTH_LONG).show());
                } catch (IOException e) {
                    Log.e(TAG, "Error connecting to Bluetooth device", e);
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Turn on bluetooth and restart the app", Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            Toast.makeText(activity, "HC-06 device not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void disconnectBluetoothDevice() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                bluetoothSocket = null;
                outputStream = null;
                inputStream = null;
                bluetoothDevice = null;
            } catch (IOException e) {
                Log.e(TAG, "Error closing Bluetooth socket", e);
            }
        }
    }

    public static int sendCommandAndReceiveResponse(String command) {
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            Log.e(TAG, "Bluetooth is not connected");
            return Integer.MIN_VALUE; // Geçersiz bir sayı anlamına gelir
        }
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            String response = new String(buffer, 0, bytesRead);

            // Yanıtı int değere dönüştürme
            try {
                return Integer.parseInt(response);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Response is not a valid integer", e);
                return Integer.MIN_VALUE;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending command or receiving response", e);
            return Integer.MIN_VALUE;
        }
    }
    // 3 eksenli pusula verilerini almak için fonksiyon
    public static int getCurrentAzimuth() {
        String command = "GET_CURRENT_AZIMUTH";
        return sendCommandAndReceiveResponse(command);
    }
    public static int getServo() {
        String command = "GET_SERVO";
        return sendCommandAndReceiveResponse(command);
    }
    public static boolean sendServo(int servo) {
        if (bluetoothSocket == null || !bluetoothSocket.isConnected()) {
            Log.e(TAG, "Bluetooth is not connected");
            return false; // Bağlantı yoksa false döner
        }
        try {
            String commandString = Integer.toString(servo);
            outputStream.write(commandString.getBytes());
            outputStream.flush();
            return true; // Başarılı olursa true döner
        } catch (IOException e) {
            Log.e(TAG, "Error sending int command", e);
            return false; // Hata olursa false döner
        }
    }
}
