package com.lilith.android.wificontrolapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class WifiConnectUnit {

    private final Activity context;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    public WifiConnectUnit(Activity context) {
        this.context = context;

        this.connectivityManager = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        this.wifiManager = (WifiManager)
                context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public boolean setWifiEnableProcess(boolean value, int requestCode) {
        if (value) {
            if (!wifiManager.isWifiEnabled()) {
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q)
                    wifiManager.setWifiEnabled(true);
                else {
                    Intent intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                    context.startActivityForResult(intent, requestCode);
                }
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q)
                wifiManager.setWifiEnabled(false);
            else {
                Intent intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                context.startActivityForResult(intent, requestCode);
            }
        }

        return true;
    }

    public boolean connectProcess(String ssid, String passphrase, OnCompleteListence listence) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            boolean hasPermission =
                    ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (!hasPermission)
                return false;

            // Deleted Wi-fi Configuration
            for (WifiConfiguration existsWifiConfig : this.wifiManager.getConfiguredNetworks()) {
                boolean isExists = existsWifiConfig.SSID.equals("\"" + ssid + "\"");
                if (isExists)
                    wifiManager.removeNetwork(existsWifiConfig.networkId);
            }

            // Created Wi-Fi Configuration
            WifiConfiguration config = new WifiConfiguration();
            config.SSID = "\"" + ssid + "\"";
            config.preSharedKey = "\"" + passphrase + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

            int networkId = wifiManager.addNetwork(config);
            boolean enableNetworkResult = wifiManager.enableNetwork(networkId, false);

            listence.onComplete(enableNetworkResult);
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            WifiNetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(passphrase)
                    .build();

            NetworkRequest request =
                    new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                            .setNetworkSpecifier(specifier)
                            .build();

            ConnectivityManager.NetworkCallback connectionCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                    listence.onComplete(true);
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    listence.onComplete(false);
                }
            };

            connectivityManager.requestNetwork(request, connectionCallback);
            return true;
        } else {
            return false;
        }
    }

    public boolean getPermissionsProcess(int requestCode) {
        boolean hasAccessFineLocationPermission =
                ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!hasAccessFineLocationPermission)
            ActivityCompat.requestPermissions(this.context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);

        return hasAccessFineLocationPermission;
    }

    public interface OnCompleteListence {
        void onComplete(boolean result);
    }
}
