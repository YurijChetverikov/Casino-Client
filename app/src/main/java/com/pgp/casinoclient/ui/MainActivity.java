package com.pgp.casinoclient.ui;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Game;
import com.pgp.casinoclient.databinding.ActivityMainBinding;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.PackageConverter;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.Transport;
import com.pgp.casinoclient.utils.Logger;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Class<? extends Fragment> currentFragmentId;
    private static MainActivity singleton = null;
    private ActivityMainBinding binding;

    private final String TAG = "Main Activity";

    private Transport transport;


    private boolean connected = false;
    Thread myThread;


    public static MainActivity Singleton(){
        return singleton;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        singleton = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.home){
                OpenFragment(new HomeFragment(this));
            }else if (item.getItemId() == R.id.transactions){
                OpenFragment(new TransactionFragment(this));
            }else if (item.getItemId() == R.id.transactionsHistory) {
                OpenFragment(new TransactionHistoryFragment(DataLoader.Singleton().CurrentPlayer, this));
            }//else if (item.getItemId() == R.id.settings){
//                OpenFragment(new SettingsFragment());
//            }else if (item.getItemId() == R.id.playersList){
//                OpenFragment(new PlayersListFragment());
//            }

            return true;
        });

        if (DataLoader.Singleton().getCasinoBitmap() == null){
            Request getLogo = new Request(RequestHeader.Sample(PackageType.CASINO_LOGO), null);

            Request callback = Transport.getTransport(this).sendRequest(getLogo);

            if (callback != null){
                if (callback.isSuccess()){
                    if (callback.getPackage().length > 100){
                        DataLoader.Singleton().setCasinoBitmap(callback.getPackage());
                        try {
                            DataLoader.Singleton().WriteCasinoImageCache(this);
                        } catch (IOException e) {
                            Logger.LogError(TAG, e);
                        }
                    }
                }
            }
        }

        Request getGames = new Request(RequestHeader.Sample(PackageType.GAMES), null);

        Request callback = Transport.getTransport(this).sendRequest(getGames);

        if (callback != null){
            if (callback.isSuccess()){
                if (callback.getPackage().length > 1){
                    Game[] arr = (Game[])PackageConverter.tryToConvert(callback.getPackage(), PackageType.GAMES, this);
                    if (arr.length > 0){
                        DataLoader.Singleton().Games.clear();
                        for (Game g : arr){
                            DataLoader.Singleton().Games.add(g);
                        }
                    }
                }
            }
        }


        OpenFragment(new HomeFragment(this));
    }



    public void OpenFragment(@NonNull Fragment f){
        if (currentFragmentId != f.getClass()){
            currentFragmentId = f.getClass();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(TRANSIT_FRAGMENT_FADE);
            transaction.replace(R.id.content_frame, f);
            transaction.commit();
        }
    }




    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (myThread != null){
            myThread.interrupt();
        }
    }

}