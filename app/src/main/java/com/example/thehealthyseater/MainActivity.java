package com.example.thehealthyseater;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        Button connectButton = findViewById(R.id.connect_button);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                connectToBluetooth();
                connectButton.isEnabled();
            }
        });
    }

    private void connectToBluetooth() {

        TextView textView = findViewById(R.id.status);
        String deviceAddress = "00:20:02:20:07:D4";
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

        try {
            textView.setText("Connecting to Bluetooth...");
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            BluetoothSocketSingleton.getInstance().setBluetoothSocket(bluetoothSocket);
            bluetoothSocket.connect();
            Toast.makeText(this, "Connected to Bluetooth", Toast.LENGTH_SHORT).show();

            // Start ScreenTwoActivity
            Intent intent = new Intent(MainActivity.this, SendActivity.class);
            startActivity(intent);
            textView.setText("Connected to Bluetooth");


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to connect to Bluetooth", Toast.LENGTH_SHORT).show();
            textView.setText("");

        }
    }

    // Pass BluetoothSocket to other activities
    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }
}
