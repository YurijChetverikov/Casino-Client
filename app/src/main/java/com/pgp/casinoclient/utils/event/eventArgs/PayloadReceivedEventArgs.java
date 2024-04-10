package com.pgp.casinoclient.utils.event.eventArgs;

import com.google.android.gms.nearby.connection.Payload;

public class PayloadReceivedEventArgs extends EventArgs{

    private Payload mPayload;

    public PayloadReceivedEventArgs(Payload payload){
        mPayload = payload;
    }

    public Payload getPayload(){
        return mPayload;
    }


}
