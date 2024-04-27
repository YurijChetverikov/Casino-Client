package com.pgp.casinoclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.core.Transaction;
import com.pgp.casinoclient.utils.event.Event;
import com.pgp.casinoclient.utils.event.eventArgs.TransactionsTableEventArgs;

public class TransactionsTableAdapter extends RecyclerView.Adapter<TransactionsTableAdapter.ViewHolder>{


    private final LayoutInflater inflater;
    private Player player;
    private Context context;

    private Event onClickEvent = new Event();;

    public TransactionsTableAdapter(Context context, Player player) {
        this.player = player;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.trans_history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction t = player.Transactions.get(position);
        //holder.descView.setText(t.getDesc());
        //holder.nameView.setText(t.buildString(DataLoader.Singleton().getPlayer()));
       // holder.typeView.setText(t.getType().GetName());

        String amountString = "";
        String titleString = t.Type.GetName();
        String receiverString = "";
        if (t.Receiver != null && t.Sender != null){
            if(t.Sender.ID == player.ID){
                amountString = "- ";
                amountString+= Integer.toString(t.SenderPaid);
                titleString+= " игроку " + t.Receiver.Name;
                receiverString = Integer.toString( t.Receiver.ID);
            }else if (t.Receiver.ID == player.ID){
                amountString = "+ ";
                amountString+= Integer.toString(t.Amount);
                titleString+= " от " + t.Sender.Name;
                receiverString = Integer.toString( t.Sender.ID);
                holder.amountView.setTextColor(context.getResources().getColor(R.color.light_green, null));
            }
        }
        amountString+= " ♣";
        holder.setTransaction(t);
        holder.amountView.setText(amountString);
        holder.senderView.setText(receiverString);
        holder.titleView.setText(titleString);

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickEvent.Fire(new TransactionsTableEventArgs(holder));
            }
        });
    }

    @Override
    public int getItemCount() {
        return player.Transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView, senderView, amountView;
        final LinearLayout itemLayout;
        private Transaction t;
        ViewHolder(View view){
            super(view);
            itemLayout = view.findViewById(R.id.item);
            titleView = view.findViewById(R.id.transTitle);
            senderView = view.findViewById(R.id.transUndertitle);
            amountView = view.findViewById(R.id.transAmount);
        }

        public void setTransaction(Transaction t){
            this.t = t;
        }
        public Transaction getTransaction(){
            return t;
        }
    }

    public Event getOnClickEvent(){
        return onClickEvent;
    }

}