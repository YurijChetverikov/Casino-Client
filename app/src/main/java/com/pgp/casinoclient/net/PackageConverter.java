package com.pgp.casinoclient.net;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.nio.ByteBuffer;
import java.util.Date;

public final class PackageConverter {

    private static final String TAG = "PackageConverter";

    @Nullable
    public static Object tryToConvert(Request request){
        if (request == null) {Log.e(TAG, "Request is NULL"); return null;}
        if (!request.isSuccess()) {Log.e(TAG, "Request is corrupted"); return null;}

        return tryToConvert(request.getPackage(), (PackageType)request.getHeader().Values.get(RequestHeaderValues.PACKAGE_TYPE));

    }

    @Nullable
    public static Object tryToConvert(byte[] pack, PackageType type){
        Object res = null;
        if (pack == null) { Log.e(TAG, "Package is NULL"); return null;}
        if (type == PackageType.INVALID) {Log.e(TAG, "Package type is INVALID"); return null;}
        ByteBuffer b = ByteBuffer.wrap(pack);

        switch (type){
            case PLAYER_FULL:
                res = proccedPlayer(b);
                break;
        }

        return res;
    }


    @Nullable
    private static Player proccedPlayer(@NonNull ByteBuffer b) {
        try{
            Player pl = new Player(b.getInt(), b.getInt(), BinaryUtils.ReadString(b), b.getInt());
            byte flags = b.get();
            pl.TransactionsLeft = b.get();
            pl.RegistrationDate = new Date(b.getLong());

            return pl;
        }catch(Exception ex){
            Log.e(TAG, "Exception while parsing player: " + ex.toString());

        }

        return null;
    }

}
