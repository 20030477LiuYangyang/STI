package com.example.testbeaconapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import Utils.NotificationUtil;
import Utils.PermissionUtil;
import beacon.TimedBeaconSimulator;

public class MainActivity extends AppCompatActivity {

    // Beacon detection
    protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;

    // Notifications
    private static final String CHANNEL_1 = "channel1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextView
        final TextView beaconPacketIDTextView = findViewById(R.id.displayID);

        // Button is invisible by default
        final Button btnQuiz = findViewById(R.id.btnQuiz);
        btnQuiz.setVisibility(View.INVISIBLE);

        // Button action
        btnQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QuizActivity.class));
            }
        });

        // This method obtains location permission from user. To use this method, pass in the name
        // of the activity you want it to work on as a parameter (i.e. 'yourClassName.this' without
        // the quotation marks, where yourClassName is the name of your class).
        PermissionUtil.obtainLocationPermission(MainActivity.this);

        // This method obtains Bluetooth Scan permission from user. To use this method, pass in the
        // name of the activity you want it to work on as a parameter (i.e. 'yourClassName.this'
        // without the quotation marks, where yourClassName is the name of your class).
        PermissionUtil.obtainBluetoothScanPermission(MainActivity.this);

        // Create a notification channel (Android Oreo and above). To use the
        // createNotificationChannel method, pass the following parameters into the
        // createNotificationChannel method (in order):
        //
        // context : The environment you are calling the method from (in this case,
        //           MainActivity.this).
        // channelNo : The channel number (1, 2, 3, etc...)
        // channelCode : The 'ID' of the channel. Can be any value (e.g., channel1).
        // channelName : Name of the channel (e.g. "Alerts")
        // channelDescription : Description of the channel (e.g. "Disaster alerts")
        // importance : Importance of the notification (e.g. NotificationManager.IMPORTANCE_LOW)
        NotificationUtil.createNotificationChannel(MainActivity.this,
                1,
                CHANNEL_1,
                "Beacon Notifications",
                "Beacon detection notifications",
                NotificationManager.IMPORTANCE_HIGH);

        // NOTE: ID1 is the UUID of the advertisement packet.
        //
        // The startBeaconDetection method scans for beacons and, if the right advertisement packet
        // ID (ID2 in this case) is found, alters the visibility of the Button or the value of the
        // TextView passed into the method.
        //
        // The parameters to pass into the startBeaconDetection method are (in order):
        //
        // buttonToMakeVisible : The Button you want to make visible. Can be null.
        // buttonText : The text you want to appear on the Button. Can be null.
        // textViewToEdit : The TextView you want to edit. Can be null.
        // textViewText : The text that appears in the TextView. If null, the text view will show
        //                the value of the captured advertisement packet's ID2 field by default.
        // validBeaconID: The ID2 value that is 'valid' and to check for. The highest value you can
        //                input is 65535 (leave it at 4386 for testing purposes).
        startBeaconDetection(btnQuiz, "Go Now", beaconPacketIDTextView, "You've discovered a new location!",4386);

        // Initialises test beacons (for devices without BLE like the Android Emulator). Meant for
        // testing only. Before compiling to a real device, comment out the following two lines of
        // code:
        BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    // !!!
    // If you are developing the app's UI, you can ignore the rest of the following code.
    // !!!
    private void startBeaconDetection(Button buttonToMakeVisible, String buttonText, TextView textViewToEdit, String textViewText, int validBeaconID) {
        beaconManager = BeaconManager.getInstanceForApplication(MainActivity.this);
        // beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        // beaconManager.setDebug(true);

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw a beacon for the first time!");

                try {
                    beaconManager.startRangingBeacons(region);
                    
                    // The sendNotification method is a method that sends a notification. To use
                    // this method, pass in the following parameters (in order):
                    //
                    // context : The environment you are calling the method from (in this case,
                    //          MainActivity.this).
                    // channelCode : The 'ID' of the channel. Can be any value (e.g., channel1).
                    // contentTitle : The title of the notification (e.g., TraceForever).
                    // contentText : The notification message (e.g., 1 pax dining).
                    // icon : The number of the icon you want to use (e.g.,
                    //        R.drawable.ic_launcher_background)
                    // priority : The priority of the notification (e.g.,
                    //            NotificationCompact.PRIORITY_LOW)
                    NotificationUtil.sendNotification(MainActivity.this,
                            CHANNEL_1,
                            "Test Beacon App",
                            "I found a beacon!",
                            R.drawable.ic_one,
                            NotificationCompat.PRIORITY_HIGH);
                }
                catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage());
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see a beacon :(");

                try {
                    beaconManager.stopRangingBeacons(region);
                }
                catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage());
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state);
            }
        });

        // Clear different remembered regions from last ran instance of app
        for (Region region : beaconManager.getMonitoredRegions()) {
            beaconManager.stopMonitoring(region);
        }

        // Make Button visible and set TextView and Button text.
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                for (Beacon beacon : beacons) {
                    String beaconPacketID2 = String.valueOf(beacon.getId2());

                    if (Integer.valueOf(beaconPacketID2) == validBeaconID) {
                        buttonToMakeVisible.setVisibility(View.VISIBLE);

                        if (textViewToEdit != null) {
                            textViewToEdit.setText(textViewText);
                        }

                        if (buttonToMakeVisible != null) {
                            // Button becomes visible when correct beacon is detected.
                            if (buttonText != null) {
                                buttonToMakeVisible.setText(buttonText);
                            }
                        }
                    }

                    break;
                }
            }
        });

        beaconManager.startMonitoring(new Region("myMonitoringUniqueId", null, null, null));
    }
}