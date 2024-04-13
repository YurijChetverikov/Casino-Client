package com.pgp.casinoclient.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pgp.casinoclient.utils.BinaryUtils;
import com.pgp.casinoclient.utils.PositionInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String TAG = "Request class";
    private RequestHeader mHeader = null;
    private byte[] mPackage = null;
    private boolean mSuccess = false;


    // Этот конструктор предназначен только для входящих данных/запросов
    public Request(@NonNull InputStream input) throws IOException{
        if (input.available() > 0){
            try{
                int available = input.available();
                PositionInputStream stream = new PositionInputStream(input);
                mHeader = RequestHeader.parse(stream);
                if (mHeader == null) {Log.e(TAG, "Invalid header"); return;}
                if (!mHeader.check(false)) {Log.e(TAG, "Invalid header"); return;}

                mPackage = new byte[available - stream.getPosition()];
                stream.read(mPackage, 0, mPackage.length);


                mSuccess = true;



            }catch(IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }


    // Этот конструктор предназначен только для исходящих данных/запросов
    public Request(@NonNull RequestHeader header, byte[] data){
        if (header == null) {Log.e(TAG, "Header in null"); return;}
        if (!header.check(true)) {Log.e(TAG, "Header is INVALID"); return;}
//        if (data == null) {Log.e(TAG, "Package is null"); /*return;*/}

        mHeader = header;
        mPackage = data;

        mSuccess = true;

    }


    public byte[] getPackage(){
        return mPackage;
    }

    public RequestHeader getHeader(){
        return mHeader;
    }

    public boolean isSuccess(){
        return mSuccess;
    }

    public byte[] writeInArray() throws IOException {
        if (!mHeader.check(true)) {Log.e(TAG, "Header is INVALID"); return null;}

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(mHeader.write());

        if (mPackage != null){
            out.write(mPackage, 0, mPackage.length);
        }
        out.close();


        return out.toByteArray();
    }
}
