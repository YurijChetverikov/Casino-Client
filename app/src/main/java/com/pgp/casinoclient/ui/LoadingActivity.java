package com.pgp.casinoclient.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.loaders.CacheReadingResult;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.PackageConverter;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.Transport;

import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    private final String TAG = "Loading Activity";
    private final String SERVER_SSID = "HONOR 10i";
    private final String SERVER_PASSWORD = "sx9ZLFkC";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }


    // Возвратит 0, если wifi выключен , 1 - если ни к чему не подключен, 2 - если подключен к казину, 3 - если к какому-то левому wifi
    private byte checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return 1; // Not connected to an access point
            }

            if (wifiInfo.getSSID().equals(SERVER_SSID)) {
                return 2;
            }

            return 3; // Connected to an access point
        } else {
            return 0; // Wi-Fi adapter is OFF
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Если не подключены к точке доступа-серверу -> пытаемся подключиться, если не выходит - пишем пользователю

        //byte a = checkWifiOnAndConnected();


        //  switch (a){
        //      case 0:
        // Wifi вырублен

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SERVER_SSID + "\"";   // Please note the quotes. String should contain ssid in quotes
        conf.preSharedKey = "\"" + SERVER_PASSWORD + "\"";
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.addNetwork(conf);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + SERVER_SSID + "\"")) {
                wifiManager.disconnect();
                wifiManager.enableNetwork(i.networkId, true);
                wifiManager.reconnect();

                break;
            }
        }


        // Читаем кэш, если он существует
        try {
            CacheReadingResult res = DataLoader.Singleton().ReadCache(this);
            if (res == CacheReadingResult.READED_SUCCESSFULLY){
                // Кэш успешно прочитали, можем загружать активити входа
                startActivity(new Intent(this, LoginActivity.class));
            }else if (res == CacheReadingResult.READED_WITH_ERRORS){
                // Вот такой строчки вообще быть не должно при работе приложения, иначе все данные сотрутся!!!
                // Тогда открывает активити логина, где просим пароль и ID
                startActivity(new Intent(this, RegisterActivity.class));
                Toast.makeText(this, "Кэш повреждён!", Toast.LENGTH_SHORT).show();
            }else{
                // Кэша нет => открывает активити логина, где просим пароль и ID
                startActivity(new Intent(this, RegisterActivity.class));
                //startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        } catch (Exception e) {
            DataLoader.Singleton().Players.clear();

            Log.e(TAG, "пизда\n" + e.toString());
        }
    }
}
