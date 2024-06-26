package com.pgp.casinoclient.ui;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.databinding.ActivityMainBinding;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.NsdHelper;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestType;
import com.pgp.casinoclient.net.Transport;
import com.pgp.casinoclient.net.TransportLayer;
import com.pgp.casinoclient.net.packages.Package;
import com.pgp.casinoclient.net.packages.PlayerPackage;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Class<? extends Fragment> currentFragmentId;
    private static MainActivity singleton = null;
    private ActivityMainBinding binding;



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
                OpenFragment(new HomeFragment());
            }else if (item.getItemId() == R.id.transactions){
                OpenFragment(new TransactionFragment());
            }//else if (item.getItemId() == R.id.transactionsHistory) {
//                OpenFragment(new TransactionHistoryFragment(DataLoader.Singleton().GetCasinoPlayer()));
//            }else if (item.getItemId() == R.id.settings){
//                OpenFragment(new SettingsFragment());
//            }else if (item.getItemId() == R.id.playersList){
//                OpenFragment(new PlayersListFragment());
//            }

            return true;
        });


        OpenFragment(new HomeFragment());

        transport = Transport.getTransport(this);
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