package com.example.thehealthyseater;

import android.bluetooth.BluetoothSocket;

public class BluetoothSocketSingleton {
    private static BluetoothSocketSingleton instance;
    private BluetoothSocket bluetoothSocket;

    private BluetoothSocketSingleton() {}

    public static BluetoothSocketSingleton getInstance() {
        if (instance == null) {
            instance = new BluetoothSocketSingleton();
        }
        return instance;
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }
}