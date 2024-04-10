package com.pgp.casinoclient.net;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pgp.casinoclient.net.packages.BooleanPackage;
import com.pgp.casinoclient.net.packages.Package;
import com.pgp.casinoclient.net.packages.PlayerPackage;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Request {

    private final String TAG = "Request class";
    private RequestHeader mHeader = null;
    private Package mPackage = null;
    private boolean mSuccess = false;


    // Этот конструктор предназначен только для входящих данных/запросов
    public Request(InputStream input, int available){
        int readedBytes = 2; // Кол-во прочтённых байт заголовка; чтобы их потом вычесть из available
        if (available > 0){
            try{
                mHeader = RequestHeader.parse(input, available);
                if (mHeader != null){
                    if (mHeader.Values.containsKey(RequestHeaderValues.REQUEST_TYPE) &&
                            mHeader.Values.containsKey(RequestHeaderValues.PACKAGE_TYPE)){

                        PackageType packType = (PackageType) mHeader.Values.get(RequestHeaderValues.PACKAGE_TYPE);
                        RequestType reqType = (RequestType) mHeader.Values.get(RequestHeaderValues.REQUEST_TYPE);

                        switch (packType){
                            case PLAYER_FULL:
                                if (reqType == RequestType.RESPONSE){
                                    mPackage = new PlayerPackage(input);
                                    mSuccess = true;

                                }else{
                                    Log.e(TAG, "Invalid request type");
                                }
                                break;
                            case PASSWORD:
                                if (reqType == RequestType.RESPONSE){
                                    mPackage = new BooleanPackage(input);
                                    mSuccess = true;

                                }else{
                                    Log.e(TAG, "Invalid request type");
                                }
                                break;
                        }
                    }
                }else{
                    Log.e(TAG, "Invalid header");
                }
            }catch(IOException ex){
                Log.e(TAG, ex.toString());
            }
        }
    }


    // Этот конструктор предназначен только для исходящих данных/запросов
    public Request(RequestHeader header, Package data){
        if (header == null) {Log.e(TAG, "Header in null"); return;}
        if (!header.Values.containsKey(RequestHeaderValues.REQUEST_TYPE)) {Log.e(TAG, "Header does not contains REQUEST_TYPE"); return;}
        if (!header.Values.containsKey(RequestHeaderValues.PACKAGE_TYPE)) {Log.e(TAG, "Header does not contains PACKAGE_TYPE"); return;}
        if (header.Values.get(RequestHeaderValues.REQUEST_TYPE) == RequestType.INVALID) {Log.e(TAG, "REQUEST_TYPE is INVALID"); return;}
        if (header.Values.get(RequestHeaderValues.PACKAGE_TYPE) == PackageType.INVALID) {Log.e(TAG, "PACKAGE_TYPE is INVALID"); return;}
        if (data == null) {Log.e(TAG, "Package is null"); }

        mHeader = header;
        mPackage = data;

        mSuccess = true;
    }


    public Package getPackage(){
        return mPackage;
    }

    public boolean isSuccess(){
        return mSuccess;
    }

    public RequestHeader getHeader(){
        return mHeader;
    }

    public byte[] writeInArray() throws IOException {
        if (!mHeader.Values.containsKey(RequestHeaderValues.REQUEST_TYPE)) {Log.e(TAG, "Header does not contains REQUEST_TYPE"); return null;}
        if (!mHeader.Values.containsKey(RequestHeaderValues.PACKAGE_TYPE)) {Log.e(TAG, "Header does not contains PACKAGE_TYPE"); return null;}
        if (mHeader.Values.get(RequestHeaderValues.REQUEST_TYPE) == RequestType.INVALID) {Log.e(TAG, "REQUEST_TYPE is INVALID"); return null;}
        if (mHeader.Values.get(RequestHeaderValues.PACKAGE_TYPE) == PackageType.INVALID) {Log.e(TAG, "PACKAGE_TYPE is INVALID"); return null;}

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(mHeader.write());

        if (mPackage != null){
            int l = mPackage.getData().array().length;
            out.write(mPackage.getData().array(), 0, l);
        }
        out.close();


        return out.toByteArray();
    }
}
