package com.pgp.casinoclient.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pgp.casinoclient.R;
import com.pgp.casinoclient.adapters.TransactionsTableAdapter;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.core.Transaction;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.PackageConverter;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.net.Request;
import com.pgp.casinoclient.net.RequestHeader;
import com.pgp.casinoclient.net.RequestHeaderValues;
import com.pgp.casinoclient.net.Transport;
import com.pgp.casinoclient.utils.event.eventArgs.TransactionsTableEventArgs;

import java.util.ArrayList;


public class TransactionHistoryFragment extends Fragment {

    private RecyclerView transactionsView;
    private SwipeRefreshLayout swipe;

    private View parentView;
    private Context context;
    private LayoutInflater inflater;
    private boolean isListing = false;

    private Player currentPlayer = null;

    private AppCompatActivity activity;

    public TransactionHistoryFragment(Player pl, AppCompatActivity activity) {
        currentPlayer = pl;
        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater = inflater;
        parentView = inflater.inflate(R.layout.fragment_transaction_history, container, false);
        context = parentView.getContext();
        transactionsView = parentView.findViewById(R.id.transactionsHistory);
        swipe = parentView.findViewById(R.id.swipe);
        swipe.setColorSchemeColors(context.getResources().getColor(R.color.theme_orange_2, null));
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isListing){
                    Request get = null;
                    get = new Request(RequestHeader.Sample(PackageType.TRANSACTIONS_HISTORY), null);
                    get.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, DataLoader.Singleton().CurrentPlayer.ID);
                    get.getHeader().Values.put(RequestHeaderValues.LAST_TRANSACTIONS_HISTORY_INDEX, DataLoader.Singleton().CurrentPlayer.Transactions.size() - 1);

                    Request callback = Transport.getTransport(activity).sendRequest(get);

                    if (callback != null){
                        ArrayList<Transaction> trans = (ArrayList<Transaction>)PackageConverter.tryToConvert(callback.getPackage(), PackageType.TRANSACTIONS_HISTORY, activity);
                        if (trans != null){
                            DataLoader.Singleton().CurrentPlayer.Transactions.addAll(trans);
                        }
                    }

                    refreshView(currentPlayer);
                    swipe.setRefreshing(false);
                }
            }
        });

        transactionsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) transactionsView.getLayoutManager();
                int pos = linearLayoutManager.findFirstVisibleItemPosition();
                if (dy >= 0 || pos == 0) {
                    isListing = false;
                } else {
                    isListing = true;
                }
            }
        });


        refreshView(currentPlayer);

        return parentView;
    }

    public void refreshView(Player pl){
        currentPlayer = pl;
        TransactionsTableAdapter adapter = new TransactionsTableAdapter(parentView.getContext(), pl);
        adapter.getOnClickEvent().AddListener((x) -> onClick((TransactionsTableEventArgs) x));
        transactionsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionsView.setAdapter(adapter);
    }

    private void onClick(@NonNull TransactionsTableEventArgs e){
        Transaction t = e.GetHolder().getTransaction();
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View view = inflater.inflate(R.layout.transaction_bottom_shit, null);
        dialog.setCancelable(true);
        dialog.setContentView(view);
        dialog.show();

        TextView titleName = view.findViewById(R.id.receiverTitle);
        TextView titleId = view.findViewById(R.id.titleId);
        TextView titleAmount = view.findViewById(R.id.amount);
        TextView senderName = view.findViewById(R.id.senderName);
        TextView senderId = view.findViewById(R.id.senderId);
        TextView receiverName = view.findViewById(R.id.receiverName);
        TextView receiverId = view.findViewById(R.id.receiverId);
        TextView senderAmount = view.findViewById(R.id.senderAmount);
        TextView receiverAmount = view.findViewById(R.id.receiverAmount);
        TextView type = view.findViewById(R.id.type);
        TextView desc = view.findViewById(R.id.desc);

        String amountString = "";
        String titleString = "";
        String receiverString = "";
        if (t.Receiver != null){
            if (t.Receiver.ID == currentPlayer.ID){
                amountString = "+ ";
                amountString+= Integer.toString(t.Amount);
                if (t.Sender != null){
                    titleString = t.Sender.Name;
                    receiverString = Long.toString( t.Sender.ID);
                }
                titleAmount.setTextColor(context.getResources().getColor(R.color.light_green, null));
            }
            receiverName.setText(t.Receiver.Name);
            receiverId.setText(Long.toString(t.Receiver.ID));
            receiverAmount.setText("Было получено: " + Integer.toString(t.Amount));
        }
        if (t.Sender != null){
            if(t.Sender.ID == currentPlayer.ID){
                amountString = "- ";
                amountString+= Integer.toString(t.SenderPaid);
                if (t.Receiver != null){
                    titleString = t.Receiver.Name;
                    receiverString = Integer.toString( t.Receiver.ID);
                }
            }
            senderName.setText(t.Sender.Name);
            senderId.setText(Long.toString(t.Sender.ID));
            senderAmount.setText("Было отправлено: " + Integer.toString(t.SenderPaid));
        }
        titleAmount.setText(amountString);
        titleId.setText(receiverString);
        titleName.setText(titleString);

/*        senderName.setText(t.Sender.Name);
        senderId.setText(Long.toString(t.Sender.ID));
        receiverName.setText(t.Receiver.Name);
        receiverId.setText(Long.toString(t.Receiver.ID));
        senderAmount.setText("Было отправлено: " + Integer.toString(t.SenderPaid));
        receiverAmount.setText("Было получено: " + Integer.toString(t.Amount));*/
        type.setText("Тип: " + t.Type.GetName());
        desc.setText(t.Description);
    }
}