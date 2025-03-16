package com.example.thehealthyseater;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SendActivity extends AppCompatActivity {

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private static final String TAG = "BluetoothDebug";
    private int neckDistance;
    private int minPressure;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);


        Button okButton = findViewById(R.id.ok);

        bluetoothSocket = BluetoothSocketSingleton.getInstance().getBluetoothSocket();

        if (bluetoothSocket != null) {
            try {
                outputStream = bluetoothSocket.getOutputStream();
                inputStream=bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSensorData();
                Intent intent = new Intent(SendActivity.this, ReceiveActivity.class);
                startActivity(intent);
            }
        });
    }

    private void requestSensorData() {
        if (outputStream != null) {
            try {
                // Request data from Arduino
                outputStream.write("GET_DATA\n".getBytes());
                outputStream.flush();
                Log.d(TAG, "Requested sensor data");

                // Read response from Arduino
                //readSensorData();

            } catch (IOException e) {
                Log.e(TAG, "Failed to request data", e);
            }
        }
    }

//    private void readSensorData() {
//        try {
//            byte[] buffer = new byte[1024];
//            int bytes = inputStream.read(buffer);
//            String data = new String(buffer, 0, bytes);
//
//
//            // Parse the data as "neckDistance:pressure"
//            String[] values = data.split(":");
//            if (values.length == 2) {
//                neckDistance = Integer.parseInt(values[0].trim());
//                minPressure = Integer.parseInt(values[1].trim());
//
//                Log.d(TAG, "Neck Distance: " + neckDistance);
//                Log.d(TAG, "Minimum Pressure: " + minPressure);
//
//                // Send the parsed data for EEPROM storage
//                sendDataToEEPROM();
//            }
//
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to read data", e);
//        }
//    }
//
//    private void sendDataToEEPROM() {
//        // Send the parsed sensor data to be saved in EEPROM
//        String eepromData = neckDistance + ":" + minPressure;
//
//        try {
//            outputStream.write(eepromData.getBytes());
//            outputStream.flush();
//            Log.d(TAG, "Sent data to EEPROM: " + eepromData);
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to send data to EEPROM", e);
//        }
//    }

}
