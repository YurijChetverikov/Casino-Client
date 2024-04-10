package com.pgp.casinoclient.net;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.net.packages.Package;
import com.pgp.casinoclient.net.packages.PlayerPackage;
import com.pgp.casinoclient.ui.MainActivity;
import com.pgp.casinoclient.utils.event.Event;
import com.pgp.casinoclient.utils.event.eventArgs.PayloadReceivedEventArgs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TransportLayer {

    private static final String TAG = "AdvertiseFragment";
    private final String SERVER_NAME = "PGP_CASINO_CLIENT"; //idk what this should be.  doc's don't say.
    private final String SERVER_ID = "pgp.casinoserver";
    Boolean mIsDiscovering = false;

    private static boolean mIsConnected = false;

    private String ConnectedEndPointId;
    private Context mContext;
    private Strategy mStrategy = Strategy.P2P_POINT_TO_POINT; // Для сервера star надо


    private static Event mPayloadReceivedEvent = new Event();

    public TransportLayer(Context context) {
        mContext = context;
        //startDiscovering();
    }


    public static Event tryToSendRequest(Request req, Context context){
        if (req == null) return null;
        if (!req.isSuccess()) return null;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                TransportLayer tl = new TransportLayer(context);
                tl.startDiscovering();
                while(!mIsConnected){

                }
                try{
                    tl.send(req.writeInArray());
                }catch(Exception ex){
                    Log.e(TAG, ex.toString());
                }
            }
        });

        t.start();

        return mPayloadReceivedEvent;
    }


    /**
     * Sets the device to discovery mode.  Once an endpoint is found, it will initiate a connection.
     */
    protected void startDiscovering() {
        Nearby.getConnectionsClient(mContext).
                startDiscovery(
                        SERVER_ID,   //id for the service to be discovered.  ie, what are we looking for.

                        new EndpointDiscoveryCallback() {  //callback when we discovery that endpoint.
                            @Override
                            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                                //we found an end point.
                                logthis("We found an endpoint " + endpointId + " name is " + info.getEndpointName());
                                if (info.getEndpointName() == "PGP_CASINO_SERVER" && endpointId == "pgp.casinoserver"){
                                    //now make a initiate a connection to it.
                                    makeConnection(endpointId);
                                }
                            }

                            @Override
                            public void onEndpointLost(@NonNull String endpointId) {
                                logthis("End point lost  " + endpointId);
                            }
                        },
                        new DiscoveryOptions.Builder().setStrategy(mStrategy).build()
                )  //options for discovery.
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                mIsDiscovering = true;
                                logthis("We have started discovery.");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mIsDiscovering = false;
                                logthis("We failed to start discovery.");
                                e.printStackTrace();
                            }
                        });

    }

    /**
     * Stops discovery.
     */
    protected void stopDiscovering() {
        mIsDiscovering = false;
        Nearby.getConnectionsClient(mContext).stopDiscovery();
        logthis("Discovery Stopped.");
    }




    //the connection callback, both discovery and advertise use the same callback.
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(
                        @NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    // setups the callbacks to read data from the other connection.
                    Nearby.getConnectionsClient(mContext).acceptConnection(endpointId, //mPayloadCallback);
                            new PayloadCallback() {
                                @Override
                                public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {

                                    if (payload.getType() == Payload.Type.BYTES) {
                                        mPayloadReceivedEvent.Fire(new PayloadReceivedEventArgs(payload));
                                        logthis("Received data");
                                    } else if (payload.getType() == Payload.Type.FILE)
                                        logthis("We got a file.  not handled");
                                    else if (payload.getType() == Payload.Type.STREAM)
                                        //payload.asStream().asInputStream()
                                        logthis("We got a stream, not handled");
                                }

                                @Override
                                public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                                    //if stream or file, we need to know when the transfer has finished.  ignoring this right now.
                                }
                            });
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            stopDiscovering();
                            ConnectedEndPointId = endpointId;
                            mIsConnected = true;
                            logthis("Status ok, sending Hi message");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            logthis("Status rejected.  :(");
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            logthis("Status error.");
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    logthis("Connection disconnected :" + endpointId);
                    ConnectedEndPointId = "";
                }
            };


    /**
     * Simple helper function to initiate a connect to the end point
     * it uses the callback setup above this function.
     */

    public void makeConnection(String endpointId) {
        Nearby.getConnectionsClient(mContext)
                .requestConnection(
                        SERVER_NAME,   //human readable name for the local endpoint.  if null/empty, uses device name or model.
                        endpointId,
                        mConnectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                logthis("Successfully requested a connection");

                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                                logthis("failed requested a connection");
                                e.printStackTrace();
                            }
                        });

    }

    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     */
    private void send(byte[] data) {

        //basic error checking
        if (ConnectedEndPointId.compareTo("") == 0)   //empty string, no connection
            return;

        Payload payload = Payload.fromBytes(data);

        // sendPayload (List<String> endpointIds, Payload payload)  if more then one connection allowed.
        Nearby.getConnectionsClient(mContext).
                sendPayload(ConnectedEndPointId,  //end point to end to
                        payload)   //the actual payload of data to send.
                .addOnSuccessListener(new OnSuccessListener<Void>() {  //don't know if need this one.
                    @Override
                    public void onSuccess(Void aVoid) {
                        logthis("Message send successfully.");
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        logthis("Message send completed.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        logthis("Message send failed.");
                        e.printStackTrace();
                    }
                });
    }


    public void logthis(String msg) {
        Log.d(TAG, msg);
    }


    public Event getPayloadReceivedEvent(){
        return mPayloadReceivedEvent;
    }
}
