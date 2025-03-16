package com.example.thehealthyseater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiveActivity extends AppCompatActivity {

    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private TextView messageTextView;
    Switch switch1;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        messageTextView = findViewById(R.id.txt_starting);
        bluetoothSocket = BluetoothSocketSingleton.getInstance().getBluetoothSocket();
        handler = new Handler(Looper.getMainLooper());

        if (bluetoothSocket != null) {
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
                startListeningForMessages();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        switch1=findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendMessageToArduino("1");

                }else {
                    sendMessageToArduino("0");
                }
            }

        });
    }

    public void onBackPressed() {
        Toast.makeText(this, "Please Press Home Button to Exit", Toast.LENGTH_SHORT).show();
    }

    private void startListeningForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder messageBuffer = new StringBuilder();
                boolean collectingMessage = false;

                while (true) {
                    try {

                        /*
                         * From the arduino messages should send within < > delimiters
                         * example bluetooth.print("<bad posture>");
                         * bluetooth.print("<sleepy>");
                         * bluetooth.print("<get up>");
                         * bluetooth.print("<working;600000 , starting;12:00>");
                         * Like this
                         * */

                        // Read one byte at a time
                        int data = inputStream.read();
                        if (data == -1) continue;  // End of stream check

                        char character = (char) data;

                        // Start collecting message if '<' is detected
                        if (character == '<') {
                            collectingMessage = true;
                            messageBuffer.setLength(0);  // Clear buffer to start new message
                        }

                        // Collect characters if in collecting mode
                        if (collectingMessage) {
                            messageBuffer.append(character);
                        }

                        // If '>' is detected, end of message
                        if (character == '>' && collectingMessage) {
                            collectingMessage = false;
                            final String receivedMessage = messageBuffer.toString();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("Received Message", receivedMessage);

                                    // Strip off the < and > delimiters
                                    String cleanMessage = receivedMessage.substring(1, receivedMessage.length() - 1);

                                    // Process the message based on its content
                                    if (cleanMessage.contains("sleepy")) {
                                        showSleepNotification();
                                    } else if (cleanMessage.contains("badPosture")) {
                                        showBadPostureNotification();
                                    } else if (cleanMessage.contains("getUp")) {
                                        showOverWorkNotification();
                                    }
                                    else if (receivedMessage.contains("working;")) {
                                        Log.d("Received Message", receivedMessage);
                                        String data[] = receivedMessage.split(" , ");
                                        String workingTime = data[0].split(";")[1];
                                        String startingTime = data[1].split(";")[1];

                                        TextView txtStarting = findViewById(R.id.txt_starting);
                                        TextView txtWorking = findViewById(R.id.workingTime);
                                        TextView txtTimeUnit = findViewById(R.id.timeUnit);

                                        if (Integer.parseInt(workingTime) / 60000 < 60) {
                                            int time = Integer.parseInt(workingTime) / 60000;
                                            txtWorking.setText(String.valueOf(time));
                                            txtTimeUnit.setText("Minutes");
                                        } else {
                                            txtWorking.setText(String.valueOf(Integer.parseInt(workingTime) / (60 * 60000)));
                                            txtTimeUnit.setText("Hours");
                                        }
                                        startingTime = startingTime.substring(0, startingTime.length() - 1);
                                        txtStarting.setText(startingTime);
                                    }


                                }
                            });

                        }

                    }catch (IOException e) {
                            e.printStackTrace();
                            Intent intent = new Intent(ReceiveActivity.this, MainActivity.class);
                            intent.putExtra("isDisconnect", true);
                            startActivity(intent);
                            break;
                    }
                }
            }
        }).start();
    }

    private void sendMessageToArduino(String command) {

        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
                Log.d("BluetoothDebug", "Sent command: " + command);
                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("BluetoothDebug", "Not Sent");
            Toast.makeText(this, "No message or output stream", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSleepNotification() {
        String channelId = "SleepNotificationChannel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Sleep Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_sleep)
                .setContentTitle("Are you sleepy?")
                .setContentText("Please take a break!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, ReceiveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(1, builder.build());
    }

    private void showBadPostureNotification() {
        String channelId = "BadPostureNotificationChannel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "BadPosture Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_airline_seat_recline_extra_24)
                .setContentTitle("Bad Posture")
                .setContentText("Please be seated properly!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, ReceiveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(2, builder.build());
    }

    private void showOverWorkNotification() {
        String channelId = "OverWorkNotificationChannel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "OverWork Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_access_time_filled_24)
                .setContentTitle("Stand Up")
                .setContentText("Please stand up and strech!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        Intent intent = new Intent(this, ReceiveActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(3, builder.build());
    }



}