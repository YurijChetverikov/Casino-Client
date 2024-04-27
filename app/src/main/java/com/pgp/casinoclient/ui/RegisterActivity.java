package com.pgp.casinoclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class RegisterActivity extends AppCompatActivity {


    private EditText idView;
    private EditText passView;
    private Button loginButton;
    private Transport transport;

    private final String TAG = "Register Activity";


    private int id = -1;
    private int pass = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        idView = findViewById(R.id.id_edit);
        idView.setTransformationMethod(null);
        passView = findViewById(R.id.passView);
        passView.setTransformationMethod(null);
        loginButton = findViewById(R.id.submit_button);

        transport = Transport.getTransport(this);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idView.getText().toString().length() > 0){
                    id = Integer.parseInt(idView.getText().toString());
                    if (id >= 0){
                        idView.setError(null);
                        if (passView.getText().toString().length() > 0){
                            pass = Integer.parseInt(passView.getText().toString());

                            if (pass > 0){
                                passView.setError(null);

                                loginButton.setEnabled(false);
                                tryToGetData();
                                loginButton.setEnabled(true);
                            }else{
                                passView.setError(getString(R.string.error_invalid_prompt));
                            }
                        }else{
                            passView.setError(getString(R.string.error_pass_not_filled));
                        }
                    }else{
                        idView.setError(getString(R.string.error_invalid_prompt));
                    }
                }else{
                    idView.setError(getString(R.string.error_id_not_filled));
                }
            }
        });
    }


    public void tryToGetData(){
        Request getPlayer = new Request(RequestHeader.Sample(PackageType.PLAYER_FULL), null);

        getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, id);
        getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_PASSWORD, pass);

        Request callback = Transport.getTransport(this).sendRequest(getPlayer);

        if (callback == null) {serverConnectionError(); return;}

        if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.GOOD){
            Player pl = (Player) PackageConverter.tryToConvert(callback.getPackage(), PackageType.PLAYER_FULL, this);

            DataLoader.Singleton().CurrentPlayer = pl;

            Request getCasName = new Request(RequestHeader.Sample(PackageType.CASINO_NAME), null);

            callback = Transport.getTransport(this).sendRequest(getCasName);

            if (callback != null){
                if (callback.isSuccess()){
                    String name = (String) PackageConverter.tryToConvert(callback.getPackage(), PackageType.CASINO_NAME, this);
                    if (name != null){
                        DataLoader.Singleton().CasinoName = name;
                    }
                }
            }

            try {
                DataLoader.Singleton().WriteTableCache(this);
            }catch (IOException e) {
                Log.e(TAG, e.toString());
            }

            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }else{
            if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_ID){
                // Игрок с таким id не найден
                idView.setError("Игрок с таким ID не найден!");
            }else if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_PASSWORD){
                // Игрок с таким id имеет другой пароль!
                passView.setError("Пароль неверный!");
            }else{
                serverConnectionError();
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        }

    }

    private void serverConnectionError(){
        Toast.makeText(this, "Нет подключения к серверу!", Toast.LENGTH_SHORT).show();
    }
}
