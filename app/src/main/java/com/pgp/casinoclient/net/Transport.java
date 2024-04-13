package com.pgp.casinoclient.net;

import static android.content.Context.WIFI_SERVICE;

import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pgp.casinoclient.net.Request;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Transport {

    private final String TAG = "Transport";
    private final int mPort = 2109;
    private OutputStream mOut;
    private InputStream mIn;
    private byte[] mAddress;
    private Socket mSocket;



    @NonNull
    private static byte[] getHotspotIPAddress(@NonNull AppCompatActivity activity) {

        try{
            final WifiManager manager = (WifiManager) activity.getSystemService(WIFI_SERVICE);

            int ipAddress = Integer.reverseBytes(manager.getDhcpInfo().gateway);
            byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
//            String ipAddressString;
//            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
            return ipByteArray;
        }catch(Exception ex){
            Log.e("WIFI_IP", "Unable to get host address.");
            return null;
        }



        //byte[] b = BinaryUtils.Int2Bytes(manager.getDhcpInfo().gateway);


    }


    private Transport(byte[] address){
        mAddress = address;
    }

    @Nullable
    public static Transport getTransport(AppCompatActivity activity){
        byte[] address = getHotspotIPAddress(activity);
        if (address != null){
            return new Transport(address);
        }
        return null;
    }


    public void destroy(){
        try{
            if (mIn != null) {
                mIn.close();
            }
            if (mOut != null){
                mOut.flush();
                mOut.close();
            }

            if (mSocket != null && mSocket.isConnected()) {
                mSocket.close();
            }
        }catch(Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public Request sendRequest(Request req){
        Request callback = null;
        if (req == null) return null;
        if (!req.isSuccess()) return null;


        Client bg = new Client();
        bg.setRequest(req);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<Request> future = executorService.submit(bg);
        try {
            callback = future.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, e.toString());
        }
        executorService.shutdown();



        return callback;
    }



    private class Client implements Callable<Request> {

        private Request req = null;
        public void setRequest(Request req){
            this.req = req;
        }

        private Request sendRequest(Request req){
            Request callback = new Request(null, null);

            try{
                InetSocketAddress serverAddr = new InetSocketAddress(InetAddress.getByAddress(mAddress), mPort);
                mSocket = new Socket();
                mSocket.connect(serverAddr, 2000);
                mOut = mSocket.getOutputStream();
                mIn = mSocket.getInputStream();


                if (mOut != null){
                    mOut.write(req.writeInArray());
                }

                if (mIn != null){
                    int available = mIn.available();
                    while(available < 3){
                        available = mIn.available();
                    }

                    callback = new Request(mIn);
                }
            }catch(Exception ex){
                Log.e(TAG, ex.toString());
                if (ex.getClass() == SocketTimeoutException.class){
                    Log.w(TAG, "Server connect timeout exception");
                }
            }

            destroy();

            return callback;
        }

        @Nullable
        @Override
        public Request call() throws Exception {
            try {
                if (req != null){
                    return sendRequest(req);
                }
            }catch(Exception ex){
                Log.e(TAG, ex.toString());
            }
            return null;
        }
    }
}
