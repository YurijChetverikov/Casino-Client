package com.pgp.casinoclient.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pgp.casinoclient.core.Game;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.core.Transaction;
import com.pgp.casinoclient.core.TransactionType;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.utils.BinaryUtils;
import com.pgp.casinoclient.utils.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

public final class PackageConverter {

    private static final String TAG = "PackageConverter";



    // НЕ ИСПОЛЬЗОВАТЬ
    @Nullable
    public static Object tryToConvert(Request request, AppCompatActivity activity){
        if (request == null) {Log.e(TAG, "Request is NULL"); return null;}
        if (!request.isSuccess()) {Log.e(TAG, "Request is corrupted"); return null;}
        if (!request.getHeader().Values.containsKey(RequestHeaderValues.PACKAGE_TYPE)){Log.e(TAG, "Header is NULL"); return null;}
        if (request.getHeader().Values.get(RequestHeaderValues.PACKAGE_TYPE) == PackageType.INVALID) {Log.e(TAG, "Header is INVALID"); return null;}

        return tryToConvert(request.getPackage(), (PackageType)request.getHeader().Values.get(RequestHeaderValues.PACKAGE_TYPE), activity);

    }

    @Nullable
    public static Object tryToConvert(byte[] pack, PackageType type, AppCompatActivity activity){
        Object res = null;
        if (pack == null) { Log.e(TAG, "Package is NULL"); return null;}
        if (type == PackageType.INVALID) {Log.e(TAG, "Package type is INVALID"); return null;}
        if (pack.length == 0) {Log.e(TAG, "Package is 0 length"); return null;}
        ByteBuffer b = ByteBuffer.wrap(pack);

        switch (type){
            case PLAYER_FULL:
                res = proccedPlayer(b);
                break;
            case CASINO_LOGO:
                res = proccedBitmap(pack);
                break;
            case GAMES:
                res = proccedGames(b);
                break;
            case PLAYER:
                res = proccedString(b);
                break;
            case TRANSACTIONS_HISTORY:
                res = proccedTransactionsHistory(b, activity);
                break;
            case CASINO_NAME:
                res = proccedString(b);
                break;
        }

        return res;
    }


    @NonNull
    private static ArrayList<Transaction> proccedTransactionsHistory(@NonNull ByteBuffer b, AppCompatActivity activity){
        ArrayList<Transaction> trans = new ArrayList<Transaction>();


        int transCount = b.getInt();

        for(int i = 0; i < transCount; i++){
            Transaction t = new Transaction();
//                    int transID = b.getInt();
            int senderId = b.getInt();
            int receiverId = b.getInt();
            if (senderId != -1){
                t.Sender = DataLoader.Singleton().getPlayerById(senderId);
                if (t.Sender == null){
                    Request req = new Request(RequestHeader.Sample(PackageType.PLAYER), null);
                    req.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, senderId);

                    Request callback = Transport.getTransport(activity).sendRequest(req);
                    if (callback != null){
                        String name = (String) PackageConverter.tryToConvert(callback.getPackage(), PackageType.PLAYER, activity);
                        if (name != null){
                            Player pl = new Player(senderId, name);
                            DataLoader.Singleton().CachedPlayers.add(pl);
                            t.Sender = DataLoader.Singleton().getPlayerById(senderId);
                        }
                    }
                }
            }
            if (receiverId != -1){
                t.Receiver = DataLoader.Singleton().getPlayerById(receiverId);
                if (t.Receiver == null){
                    Request req = new Request(RequestHeader.Sample(PackageType.PLAYER), null);
                    req.getHeader().Values.put(RequestHeaderValues.PLAYER_ID, receiverId);

                    Request callback = Transport.getTransport(activity).sendRequest(req);
                    if (callback != null){
                        String name = (String) PackageConverter.tryToConvert(callback.getPackage(), PackageType.PLAYER, activity);
                        if (name != null){
                            Player pl = new Player(receiverId, name);
                            DataLoader.Singleton().CachedPlayers.add(pl);
                            t.Receiver = DataLoader.Singleton().getPlayerById(receiverId);
                        }
                    }
                }
            }
            t.Amount = b.getInt();
            t.SenderPaid = b.getInt();
            t.Type = TransactionType.Get((int)b.get());
            t.Description = BinaryUtils.ReadString(b);
            trans.add(t);
        }

        return trans;
    }


    @NonNull
    private static String proccedString(@NonNull ByteBuffer b){
        return BinaryUtils.ReadString(b);
    }

    @NonNull
    private static Game[] proccedGames(@NonNull ByteBuffer b){
        byte gamesCount = b.get();
        Game[] arr = new Game[gamesCount];
        for(byte i = 0; i < gamesCount; i++){
            Game g = new Game();
            g.ID = b.get();
            g.Name = BinaryUtils.ReadString(b);
            arr[i] = g;
        }

        return arr;
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

    @Nullable
    private static Bitmap proccedBitmap(@NonNull byte[] b) {
        try{
            final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inJustDecodeBounds = false;
            return decodeSampledBitmapFromResource(b, 150, 150);
        }catch(Exception ex){
            Logger.LogError(TAG, ex);
        }

        return null;
    }


    public static Bitmap decodeSampledBitmapFromResource(byte[] b,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(b, 0, b.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(b, 0, b.length, options);
    }

    public static int calculateInSampleSize(
            @NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
