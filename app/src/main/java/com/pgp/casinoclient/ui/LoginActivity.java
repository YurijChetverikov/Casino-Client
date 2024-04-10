package com.pgp.casinoclient.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.loaders.CacheReadingResult;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.NsdHelper;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.RequestType;
import com.pgp.casinoclient.net.Transport;
import com.pgp.casinoclient.net.TransportLayer;
import com.pgp.casinoclient.net.packages.PlayerPackage;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

public class LoginActivity extends AppCompatActivity {


    private TextView welcomeView;
    private EditText passView;
    private Button loginButton;

    private final String TAG = "Login Activity";

    private int pass = 0;

    private AppCompatActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        passView = findViewById(R.id.passView);
        loginButton = findViewById(R.id.submit_button);
        welcomeView = findViewById(R.id.welcomeTextView);

        activity = this;

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String helloString = "";
        if (currentHour > 4 && currentHour <= 10){
            helloString = "Доброе утро, ";
        }else if(currentHour > 10 && currentHour <= 16){
            helloString = "Добрый день, ";
        }else if(currentHour > 16 && currentHour <= 22){
            helloString = "Добрый вечер, ";
        }else if(currentHour > 22 && currentHour <= 4){
            helloString = "Доброй ночи, ";
        }

        helloString+= DataLoader.Singleton().CurrentPlayer.Name;

        welcomeView.setText(helloString);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passView.getText().toString().length() == 4){
                    pass = Integer.parseInt(passView.getText().toString());

                    if (pass > 0){
                        passView.setError(null);
                        Request r = new Request(RequestHeader.createHeader(PackageType.PASSWORD), null);
                        r.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, DataLoader.Singleton().CurrentPlayer.ID);
                        r.getHeader().Values.put(RequestHeaderValues.PLAYER_PASSWORD, pass);
                        Request callback = Transport.getTransport(activity).sendRequest(r);

                        if (callback.isSuccess()){
                            if ((boolean)callback.getPackage().parseResult() == true){
                                if (DataLoader.Singleton().CurrentPlayer.Password != pass){
                                    // Делаем запрос на сервер, чтобы получить всего игрока, так как пароли не совпадают

                                    Request r2 = new Request(RequestHeader.createHeader(PackageType.PLAYER_FULL), null);
                                    r2.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, DataLoader.Singleton().CurrentPlayer.ID);
                                    r2.getHeader().Values.put(RequestHeaderValues.PLAYER_PASSWORD, pass);
                                    Request callback2 = Transport.getTransport(activity).sendRequest(r2);

                                    if (callback2.isSuccess()){
                                        DataLoader.Singleton().CurrentPlayer = (Player) callback2.getPackage().parseResult();
                                        try {
                                            DataLoader.Singleton().WriteTableCache(activity);
                                        } catch (IOException e) {
                                            Log.e(TAG, e.toString());
                                        }
                                    }else{
                                        passView.setError(getString(R.string.error_incorrect_prompt));
                                    }
                                }
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }else{
                                passView.setError(getString(R.string.error_incorrect_prompt));
                            }
                        }else{
                            serverConnectionError();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }else{
                        passView.setError(getString(R.string.error_invalid_prompt));
                    }
                }else{
                    passView.setError(getString(R.string.error_pass_not_filled));
                }
            }
        });
    }

    private void serverConnectionError(){
        Toast.makeText(this, "Нет подключения к серверу!", Toast.LENGTH_SHORT).show();
    }
}
