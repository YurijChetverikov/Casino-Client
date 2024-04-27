package com.pgp.casinoclient.utils.event.eventArgs;


import com.pgp.casinoclient.adapters.TransactionsTableAdapter;

public class TransactionsTableEventArgs extends EventArgs {

    private TransactionsTableAdapter.ViewHolder holder;

    public TransactionsTableEventArgs(TransactionsTableAdapter.ViewHolder holder){
        this.holder = holder;
    }

    public TransactionsTableAdapter.ViewHolder GetHolder(){
        return holder;
    }


}
