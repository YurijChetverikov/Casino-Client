package com.pgp.casinoclient.net.packages;

import android.util.Log;

import androidx.annotation.NonNull;

import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;

public class IntegerPackage extends Package{

    private final String TAG = "Integer package";


    // Клиентский конструктор
    public IntegerPackage(int val){
        super((Object) null);
        setData(BinaryUtils.Int2Bytes(val));
    }

    public IntegerPackage(InputStream input) throws IOException {
        super(input);
    }


    @Override
    public void setData(byte[] data){
        super.setData(data);
        setNotReady();
        mData = ByteBuffer.wrap(data);
        setReady();
    }

    @Override
    public void setData(@NonNull Object data){
        if (data == null) return;
        if (data.getClass() == int.class){
            super.setData(data);
            setNotReady();
            int val = (int)data;
            setData(BinaryUtils.Int2Bytes(val));

        }else{
            Log.e(TAG, "Wrong package type provided: " + data.getClass().toString());
        }
    }

    @Override
    public Integer parseResult(){
        super.parseResult();
        setNotReady();
        if (mData != null){
            if (mData.array().length > 0){
                try{
                    return mData.getInt();
                }catch(Exception ex){
                    Log.e(TAG, ex.toString());
                }
            }
        }

        return null;
    }
}
