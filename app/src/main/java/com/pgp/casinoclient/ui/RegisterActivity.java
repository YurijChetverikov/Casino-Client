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
import com.pgp.casinoclient.loaders.CacheReadingResult;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.Transport;
import com.pgp.casinoclient.net.packages.PlayerPackage;

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
        passView = findViewById(R.id.passView);
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

                                tryToGetData();

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
        PlayerPackage pak = new PlayerPackage(id, pass);
        RequestHeader h = RequestHeader.createHeader(PackageType.PLAYER_FULL);
        h.Values.put(RequestHeaderValues.PLAYER_ID, id);
        h.Values.put(RequestHeaderValues.PLAYER_PASSWORD, pass);
        Request r = new Request(h, null);

        Request res = transport.sendRequest(r);

        if (res != null){
            if (res.isSuccess()){
                if (res.getPackage() != null){
                    Player pl = (Player) res.getPackage().parseResult();

                    DataLoader.Singleton().CurrentPlayer = pl;
                    try {
                        DataLoader.Singleton().WriteTableCache(this);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }else{
                    serverConnectionError();
                }
            }else{
                serverConnectionError();
            }
        }else{
            serverConnectionError();
        }

    }

    private void serverConnectionError(){
        Toast.makeText(this, "Нет подключения к серверу!", Toast.LENGTH_SHORT).show();
    }
}
