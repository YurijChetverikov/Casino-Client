package com.pgp.casinoclient.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.loaders.DataLoader;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class HomeFragment extends Fragment {

    private Context context;


    private TextView playerName;
    private TextView playerId;
    private TextView playerJoinDate;
    private TextView playerBalance;
    private TextView blackjackCountView;

    private SwipeRefreshLayout swipe;


    private RecyclerView transactionHistoryView;
    public HomeFragment() {
        // Required empty public constructor
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
        swipe = parentView.findViewById(R.id.swipe);
        swipe.setColorSchemeColors(context.getResources().getColor(R.color.theme_orange_2, null));
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
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
        //playerName.setText(helloString + DataLoader.Singleton().getPlayer().getName());
        playerId.setText("Ваш ID: " + Long.toString(DataLoader.Singleton().CurrentPlayer.ID));
        playerBalance.setText(Integer.toString(DataLoader.Singleton().CurrentPlayer.Balance) + " ♣");


        //TransactionsTableAdapter adapter = new TransactionsTableAdapter(context, DataLoader.Singleton().getPlayer().getTransactions());
        //transactionHistoryView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //transactionHistoryView.setAdapter(adapter);

    }
}