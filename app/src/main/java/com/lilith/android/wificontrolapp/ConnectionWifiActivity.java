package com.lilith.android.wificontrolapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConnectionWifiActivity extends AppCompatActivity {

    private final String TAG = "ConnectionWifiActivity";
    private final int SEARCH_WIFI = 1;

    private EditText etSSID;
    private EditText etPassphrase;
    private Button btnWifiList;
    private Button btnConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_wifi);

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
            case SEARCH_WIFI:
                if (resultCode == RESULT_OK) {
                    String ssid = data.getStringExtra("ssid");
                    etSSID.setText(ssid);
                }
                break;
        }
    }

    private void searchWifi() {
        Intent intent = new Intent(this, SearchWifiActivity.class);
        startActivityForResult(intent, SEARCH_WIFI);
    }

    /*
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectionWifi() {
        WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                .setSsid(etSSID.getText().toString())
                .setWpa2Passphrase(etPassphrase.getText().toString())
                .build();

        NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                        .setNetworkSpecifier(specifier)
                        .build();

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback connectionCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                Toast.makeText(ConnectionWifiActivity.this, "連線成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Toast.makeText(ConnectionWifiActivity.this, "連線失敗", Toast.LENGTH_SHORT).show();
            }
        };
        connectivityManager.requestNetwork(request, connectionCallback);
    }

     */

    private void connectionWifi() {
        NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_FOREGROUND)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)
                        .setNetworkSpecifier(etSSID.getText().toString())
                        .build();

        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        ConnectivityManager.NetworkCallback connectionCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                connectivityManager.bindProcessToNetwork(network);
                Toast.makeText(ConnectionWifiActivity.this, "連線成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Toast.makeText(ConnectionWifiActivity.this, "連線失敗", Toast.LENGTH_SHORT).show();
            }
        };
        connectivityManager.requestNetwork(request, connectionCallback);
    }
}