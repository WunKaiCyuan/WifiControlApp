package com.lilith.android.wificontrolapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.ConnectionService;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class ConnectionWifiActivity extends AppCompatActivity {

    private final String TAG = "ConnectionWifiActivity";

    private final int REQUEST_CODE_GET_PERMISSIONS = 1;
    private final int REQUEST_CODE_SEARCH_WIFI = 2;
    private final int REQUEST_CODE_SET_WIFI_ENABLE = 3;

    private EditText etSSID;
    private EditText etPassphrase;
    private Button btnWifiList;
    private Button btnConnection;

    private WifiConnectUnit wifiConnectUnit;
    private WifiConnectUnit.OnCompleteListence onCompleteListence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_wifi);

        wifiConnectUnit = new WifiConnectUnit(this);
        onCompleteListence = (WifiConnectUnit.OnCompleteListence) result -> {
            if (result)
                Toast.makeText(ConnectionWifiActivity.this, etSSID.getText().toString() + " 連線成功", Toast.LENGTH_SHORT).show();
        };

        etSSID = findViewById(R.id.etSSID);
        etPassphrase = findViewById(R.id.etPassphrase);
        btnWifiList = findViewById(R.id.btnWifiList);
        btnConnection = findViewById(R.id.btnConnection);

        btnWifiList.setOnClickListener(v -> searchWifi());
        btnConnection.setOnClickListener(v -> connectionWifi());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SET_WIFI_ENABLE:
                if (wifiConnectUnit.isWifiEnabled())
                    wifiConnectUnit.connectProcess(
                            etSSID.getText().toString(), etPassphrase.getText().toString(), onCompleteListence);
                else
                    Toast.makeText(this, "請開啟WIFI", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_CODE_SEARCH_WIFI:
                if (resultCode == RESULT_OK) {
                    String ssid = data.getStringExtra("ssid");
                    etSSID.setText(ssid);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_GET_PERMISSIONS:
                boolean permissionsResult = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        permissionsResult = false;
                        break;
                    }
                }

                if (!permissionsResult) {
                    Toast.makeText(this, "請接受授權", Toast.LENGTH_SHORT).show();
                    break;
                }

                wifiConnectUnit.connectProcess(
                        etSSID.getText().toString(), etPassphrase.getText().toString(), onCompleteListence);
                break;
        }
    }

    private void searchWifi() {
        Intent intent = new Intent(this, SearchWifiActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SEARCH_WIFI);
    }

    private void connectionWifi() {

        if (!wifiConnectUnit.getPermissionsProcess(REQUEST_CODE_GET_PERMISSIONS))
            return;

        if (!wifiConnectUnit.isWifiEnabled()) {
            wifiConnectUnit.setWifiEnableProcess(true, REQUEST_CODE_SET_WIFI_ENABLE);
            return;
        }

        wifiConnectUnit.connectProcess(
                etSSID.getText().toString(), etPassphrase.getText().toString(), onCompleteListence);

    }
}