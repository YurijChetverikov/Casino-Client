package com.pgp.casinoclient.net.packages;

import android.util.Log;

import androidx.annotation.NonNull;

import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.net.PackageType;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;

public class PlayerPackage extends Package{

    private final String TAG = "Player package";

    public PlayerPackage(Player pl) {
        super(pl);
    }

    public PlayerPackage(InputStream input) throws IOException {
        super(input);
    }

    // Клиентский конструктор
    public PlayerPackage(int playerId, int playerPass){
        super((Object) null);

        ByteBuffer b = ByteBuffer.allocate(8);
        b.putInt(playerId);
        b.putInt(playerPass);

        setData(b.array());
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
        if (data.getClass() == Player.class){
            super.setData(data);
            setNotReady();
            Player pl = (Player)data;

            try{
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(BinaryUtils.Int2Bytes(pl.ID));
                out.write(BinaryUtils.Int2Bytes(pl.Password));
                out.write(BinaryUtils.WriteString(pl.Name));
                out.write(BinaryUtils.Int2Bytes(pl.Balance));
                out.write(0); // Флаги
                out.write(pl.TransactionsLeft);
                out.write(BinaryUtils.Long2Bytes(pl.RegistrationDate.getTime()));

                setData(out.toByteArray());
                out.close();

                setReady();
            }catch(IOException ex){
                Log.e(TAG, ex.toString());
            }



        }else{
            Log.e(TAG, "Wrong package type provided: " + data.getClass().toString());
        }
    }

    @Override
    public Player parseResult(){
        super.parseResult();
        setNotReady();
        if (mData != null){
            if (mData.array().length > 0){
                try{
                    Player pl = new Player(mData.getInt(), mData.getInt(), BinaryUtils.ReadString(mData), mData.getInt());
                    byte flags = mData.get();
                    pl.TransactionsLeft = mData.get();
                    pl.RegistrationDate = new Date(mData.getLong());
                    return pl;
                }catch(Exception ex){

                }
            }
        }

        return null;
    }

}
