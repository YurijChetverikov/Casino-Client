package com.pgp.casinoclient.net.packages;

import androidx.annotation.NonNull;

import com.pgp.casinoclient.net.PackageType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Package {

    protected ByteBuffer mData;
    private boolean mReady = false; // Ставится в true, когда мы всё загрузили или всё распарсили


    public Package(@NonNull InputStream rawData) throws IOException {
        int available = rawData.available();
        byte[] buff = new byte[available];
        rawData.read(buff, 0, available);
        mData = ByteBuffer.wrap(buff);
    }

    public Package(Object o){
        setData(o);
    }



    public void setData(byte[] data){
        mData = ByteBuffer.wrap(data);
    }

    public void setData(@NonNull Object data){

    }

    public Object parseResult(){
        Object result = null;


        return result;
    }

    public ByteBuffer getData(){
        return mData;
    }



    protected void setReady(){
        mReady = true;
    }
    protected void setNotReady(){
        mReady = false;
    }
    public boolean ready(){
        return mReady;
    }
}
