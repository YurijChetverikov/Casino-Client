package com.pgp.casinoclient.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.pgp.casinoclient.utils.Logger;

import java.io.IOException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private Context context;


    private TextView playerName;
    private TextView playerId;
    private TextView playerJoinDate;
    private TextView playerBalance;
    private TextView casinoName;
    private ImageView casinoImage;


    private SwipeRefreshLayout swipe;
    private RecyclerView transactionHistoryView;

    private AppCompatActivity activity;

    private static String TAG = "Home Fragment";


    public HomeFragment(AppCompatActivity parentActivity) {
        activity = parentActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View parentView = inflater.inflate(R.layout.fragment_home, container, false);
        context = parentView.getContext();
        //playerName = parentView.findViewById(R.id.player_name);
        playerId = parentView.findViewById(R.id.player_id);
        playerBalance = parentView.findViewById(R.id.player_balance);
        casinoImage = parentView.findViewById(R.id.casinoLogoView);
        casinoName = parentView.findViewById(R.id.casino_name);
        swipe = parentView.findViewById(R.id.swipe);
        swipe.setColorSchemeColors(context.getResources().getColor(R.color.theme_orange_2, null));
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Request getLogo = new Request(RequestHeader.Sample(PackageType.CASINO_LOGO), null);

                Request callback = Transport.getTransport(activity).sendRequest(getLogo);

                if (callback != null){
                    if (callback.isSuccess()){
                        if (callback.getPackage().length > 100){
                            DataLoader.Singleton().setCasinoBitmap(callback.getPackage());
                            try {
                                DataLoader.Singleton().WriteCasinoImageCache(activity.getApplicationContext());
                            } catch (IOException e) {
                                Logger.LogError(TAG, e);
                            }
                        }
                    }
                }

                Request getPlayer = new Request(RequestHeader.Sample(PackageType.PLAYER_FULL), null);

                getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, DataLoader.Singleton().CurrentPlayer.ID);
                getPlayer.getHeader().Values.put(RequestHeaderValues.PLAYER_PASSWORD, DataLoader.Singleton().CurrentPlayer.Password);

                callback = Transport.getTransport(activity).sendRequest(getPlayer);

                if (callback == null) {Toast.makeText(getContext(), getString(R.string.error_server_connection), Toast.LENGTH_SHORT); return;}

                if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.GOOD){
                    Player pl = (Player) PackageConverter.tryToConvert(callback.getPackage(), PackageType.PLAYER_FULL, activity);

                    DataLoader.Singleton().CurrentPlayer = pl;
                    try {
                        DataLoader.Singleton().WriteTableCache(activity);
                    }catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }

                }else{
                    if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_ID){
                        // Игрок с таким id не найден
                    }else if (callback.getHeader().Values.get(RequestHeaderValues.ERROR_CODE) == RequestErrorCode.DATA_NOT_FOUND$PLAYER_WITH_PASSWORD){
                        // Игрок с таким id имеет другой пароль!
                        Toast.makeText(activity, "Пароль не верный!", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getContext(), getString(R.string.error_server_connection), Toast.LENGTH_SHORT);
                    }
                }




                updateFragment();
                swipe.setRefreshing(false);
                //TODO: сделать обновление по wifi direct
            }
        });

        updateFragment();

        return parentView;
    }


    private void updateFragment(){
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String helloString = "";
        if (currentHour > 4 && currentHour <= 10){
            helloString = "Доброе утро,\n";
        }else if(currentHour > 10 && currentHour <= 16){
            helloString = "Добрый день,\n";
        }else if(currentHour > 16 && currentHour <= 22){
            helloString = "Добрый вечер,\n";
        }else if(currentHour > 22 && currentHour <= 4){
            helloString = "Доброй ночи,\n";
        }
        //playerName.setText(helloString + DataLoader.Singleton().CurrentPlayer.getName());
        playerId.setText("Ваш ID: " + Long.toString(DataLoader.Singleton().CurrentPlayer.ID));
        playerBalance.setText(Integer.toString(DataLoader.Singleton().CurrentPlayer.Balance) + " ♣");

        if (DataLoader.Singleton().getCasinoBitmap() != null){
            casinoImage.setImageBitmap(DataLoader.Singleton().getCasinoBitmap());
        }

        casinoName.setText(DataLoader.Singleton().CasinoName);

        //TransactionsTableAdapter adapter = new TransactionsTableAdapter(context, DataLoader.Singleton().getPlayer().getTransactions());
        //transactionHistoryView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //transactionHistoryView.setAdapter(adapter);

    }
}