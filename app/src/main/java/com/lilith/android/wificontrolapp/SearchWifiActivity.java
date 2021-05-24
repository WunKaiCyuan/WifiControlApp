package com.lilith.android.wificontrolapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchWifiActivity extends AppCompatActivity {

    private final String TAG = "SearchWifiActivity";
    private final int WIFI_ENABLED = 1;
    private final int GET_PERMISSION = 2;

    private RecyclerView rvSearchWifi;
    private LinearLayoutManager rvSearchWifiLayoutManager;
    private SearchWifiAdapter rvSearchWifiAdapter;

    private Button btnSearchWifi;

    private WifiManager wifiManager;
    private boolean isPostSearchWifi = false;
    private Handler handler = new Handler();

    private Runnable searchWifi = () -> {
        new Thread(() -> {
            wifiManager.startScan();
            List<ScanResult> results = wifiManager.getScanResults();

            runOnUiThread(() -> {
                rvSearchWifiAdapter.itemDataList.clear();
                for (ScanResult item : results) {
                    SearchWifiItem data = new SearchWifiItem();
                    data.wifiName = item.SSID;
                    data.address = item.BSSID;
                    data.encryptionType = item.capabilities;
                    data.frequency = item.frequency + "";
                    data.strength = item.level + "";

                    data.connectionRunnable = () -> {
                        Intent intent = new Intent();
                        intent.putExtra("ssid", item.SSID);
                        setResult(RESULT_OK, intent);
                        finish();
                    };
                    rvSearchWifiAdapter.itemDataList.add(data);
                }
                rvSearchWifiAdapter.notifyDataSetChanged();
            });

        }).start();

        handler.postDelayed(this.searchWifi, 3000);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_wifi);

        getPermission();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        btnSearchWifi = findViewById(R.id.btnSearchWifi);
        rvSearchWifi = findViewById(R.id.rvSearchWifi);

        btnSearchWifi.setOnClickListener(v -> {

            if (!isPostSearchWifi) {
                if (setWifiEnabled()) {
                    handler.post(searchWifi);
                    isPostSearchWifi = true;
                    btnSearchWifi.setText("暫停搜尋");
                }
            } else {
                handler.removeCallbacks(searchWifi);
                isPostSearchWifi = false;
                btnSearchWifi.setText("搜尋WIFI");
            }
        });

        rvSearchWifiLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvSearchWifi.setLayoutManager(rvSearchWifiLayoutManager);

        rvSearchWifiAdapter = new SearchWifiAdapter();
        rvSearchWifi.setAdapter(rvSearchWifiAdapter);
    }

    private boolean setWifiEnabled() {
        if (!wifiManager.isWifiEnabled()) {
            boolean result = wifiManager.setWifiEnabled(true);
            if (!result) {
                // android api 29 level "wifiManager.setWifiEnabled" deprecation
                Intent intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                startActivityForResult(intent, WIFI_ENABLED);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(searchWifi);
        isPostSearchWifi = false;
        btnSearchWifi.setText("搜尋WIFI");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_PERMISSION:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "請接受授權", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case WIFI_ENABLED:
                if (wifiManager.isWifiEnabled()) {
                    handler.post(searchWifi);
                    isPostSearchWifi = true;
                    btnSearchWifi.setText("暫停搜尋");
                } else {
                    Toast.makeText(this, "請開啟WIFI", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case GET_PERMISSION:
                for (int index = 0; index < grantResults.length; index++) {
                    int grantResult = grantResults[index];
                    String permission = permissions[index];
                    boolean result = grantResult == PackageManager.PERMISSION_GRANTED;

                    if(!result){
                        Toast.makeText(this, "請接受授權", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                }
                break;
        }
    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GET_PERMISSION);
        }
    }

    class SearchWifiAdapter extends RecyclerView.Adapter<SearchWifiItemViewHolder> {

        public ArrayList<SearchWifiItem> itemDataList = new ArrayList<>();

        @NonNull
        @Override
        public SearchWifiItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_search_wifi_info, parent, false);
            return new SearchWifiItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchWifiItemViewHolder holder, int position) {
            SearchWifiItem itemData = itemDataList.get(position);
            holder.tvWifiName.setText(itemData.wifiName);
            holder.tvAddress.setText(itemData.address);
            holder.tvEncryptionType.setText(itemData.encryptionType);
            holder.tvFrequency.setText(itemData.frequency);
            holder.tvStrength.setText(itemData.strength);
            holder.btnConnection.setOnClickListener(v -> itemData.connectionRunnable.run());
        }

        @Override
        public int getItemCount() {
            return itemDataList.size();
        }
    }

    class SearchWifiItemViewHolder extends RecyclerView.ViewHolder {

        private TextView tvWifiName;
        private TextView tvAddress;
        private TextView tvEncryptionType;
        private TextView tvFrequency;
        private TextView tvStrength;
        private Button btnConnection;

        public SearchWifiItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWifiName = itemView.findViewById(R.id.tvWifiName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvEncryptionType = itemView.findViewById(R.id.tvEncryptionType);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvStrength = itemView.findViewById(R.id.tvStrength);
            btnConnection = itemView.findViewById(R.id.btnConnection);
        }
    }

    class SearchWifiItem {
        private String wifiName;
        private String address;
        private String encryptionType;
        private String frequency;
        private String strength;
        private Runnable connectionRunnable;
    }
}