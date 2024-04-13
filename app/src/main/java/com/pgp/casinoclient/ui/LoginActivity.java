package com.pgp.casinoclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.PackageConverter;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestErrorCode;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.Transport;

import java.io.IOException;
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

                        Request getPlayer = new Request(RequestHeader.Sample(PackageType.PLAYER_FULL), null);

                        getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, DataLoader.Singleton().CurrentPlayer.ID);
                        getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_PASSWORD, pass);

                        Request callback = Transport.getTransport(activity).sendRequest(getPlayer);

                        if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.GOOD){
                            Player pl = (Player) PackageConverter.tryToConvert(callback);

                            DataLoader.Singleton().CurrentPlayer = pl;
                            try {
                                DataLoader.Singleton().WriteTableCache(activity);
                            }catch (IOException e) {
                                Log.e(TAG, e.toString());
                            }

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));

                            Log.i(TAG, "DONE.");
                        }else{
                            if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_ID){
                                // Игрок с таким id не найден
                            }else if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_PASSWORD){
                                // Игрок с таким id имеет другой пароль!
                                Toast.makeText(activity, "Пароль не верный!", Toast.LENGTH_SHORT).show();
                            }else{
                                serverConnectionError();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
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
