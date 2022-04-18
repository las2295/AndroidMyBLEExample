package com.epoch.myble;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import javax.crypto.Mac;

public class MainActivity extends AppCompatActivity {
    private static final int SELECT_DEVICE_REQUEST_CODE = 0;
    ActivityResultLauncher resultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartIntentSenderForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result != null && result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    ScanResult scanResult = data.getParcelableExtra(
                                            CompanionDeviceManager.EXTRA_DEVICE
                                    );
                                    BluetoothDevice deviceToPair = scanResult.getDevice();

                                    if (deviceToPair != null) {
                                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            requestPermissions(new String[] {Manifest.permission.BLUETOOTH_CONNECT}, PackageManager.PERMISSION_GRANTED);
                                        }
                                        deviceToPair.createBond();
                                        // Continue interacting with paired device;
                                        Log.d("BLE", "Connected");
                                    }
                                }
                            } else {
                                Log.d("BLE", "Pairing Error");
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // https://developer.android.com/guide/topics/connectivity/companion-device-pairing
        requestPermissions(new String[] {Manifest.permission.BLUETOOTH_CONNECT}, PackageManager.PERMISSION_GRANTED);

        BluetoothLeDeviceFilter deviceFilter =
            new BluetoothLeDeviceFilter.Builder()
                .build();

        AssociationRequest pairingRequest = new AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            .build();

        CompanionDeviceManager deviceManager =
            (CompanionDeviceManager) getSystemService(
                    Context.COMPANION_DEVICE_SERVICE
            );

        deviceManager.associate(pairingRequest,
            new CompanionDeviceManager.Callback() {
                @Override
                public void onDeviceFound(IntentSender chooseLauncher) {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(chooseLauncher).build();

                    resultLauncher.launch(intentSenderRequest);
                }

            @Override
            public void onFailure(CharSequence charSequence) {
                Log.d("BLE", charSequence.toString());
            }
        }, null);
    }
}