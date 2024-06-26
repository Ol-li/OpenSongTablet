package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.SimpleArrayMap;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class NearbyConnections implements NearbyInterface {

    final Activity activity;
    final Context context;
    final OptionMenuListeners optionMenuListeners;
    final Preferences preferences;
    final StorageAccess storageAccess;
    final ProcessSong processSong;
    final SQLiteHelper sqLiteHelper;

    private boolean isDiscovering = false, isAdvertising = false;

    final NearbyReturnActionsInterface nearbyReturnActionsInterface;

    private final ArrayList<String> connectedEndPoints;
    private final ArrayList<String> connectedEndPointsNames;
    private final ArrayList<String> connectedDeviceIds;

    // Handler for stop of discovery
    private final Handler stopDiscoveryHandler = new Handler();
    private final Runnable stopDiscoveryRunnable = this::stopDiscovery;

    private String payLoadTransferIds = "";
    private String latestfoldernamepair = "";

    private int pendingCurrentSection = 0;

    NearbyConnections(Activity activity, Preferences preferences, StorageAccess storageAccess,
                      ProcessSong processSong, OptionMenuListeners optionMenuListeners,
                      SQLiteHelper sqLiteHelper) {
        this.context = activity;
        this.activity = activity;
        this.optionMenuListeners = optionMenuListeners;
        this.preferences = preferences;
        this.storageAccess = storageAccess;
        this.processSong = processSong;
        this.sqLiteHelper = sqLiteHelper;
        connectedEndPoints = new ArrayList<>();
        connectedEndPointsNames = new ArrayList<>();
        connectedDeviceIds = new ArrayList<>();
        nearbyReturnActionsInterface = (NearbyReturnActionsInterface) context;
    }

    String incomingPrevious = "";
    String connectionId;
    String connectionEndPointName;

    // The stuff used for Google Nearby for connecting devices
    final String serviceId = "com.garethevans.church.opensongtablet";
    private void updateConnectionLog(String newMessage) {
        if (newMessage!=null) {
            StaticVariables.connectionLog += newMessage + "\n";
            try {
                optionMenuListeners.updateConnectionsLog();
            } catch (Exception e) {
                Log.d("NearbyConnections", "Error updating connection log: " + e);
            }
        }
    }

    // Start or stop the required service
    @Override
    public void startAdvertising() {
        if (!isAdvertising) {
            Log.d("NearbyConnections", "Nearby.getConnectionsClient(context)=" + Nearby.getConnectionsClient(context));
            Log.d("NearbyConnections", "startAdvertising()");
            AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
            Nearby.getConnectionsClient(context)
                    .startAdvertising(getUserNickname(), serviceId, connectionLifecycleCallback(), advertisingOptions)
                    .addOnSuccessListener(
                            (Void unused) -> {
                                // We're advertising!
                                updateConnectionLog(context.getResources().getString(R.string.connections_broadcast) + " " + getUserNickname());
                                Log.d("NearbyConnections", "startAdvertising() - success");
                                isAdvertising = true;
                            })
                    .addOnFailureListener(
                            (Exception e) -> {
                                // We were unable to start advertising.
                                updateConnectionLog(context.getResources().getString(R.string.connections_failure) + " " + getUserNickname());
                                Log.d("NearbyConnections", "startAdvertising() - failure: " + e);
                            });
        } else {
            Log.d("NearbyConnections", "startAdvertising() - already advertising");
        }
    }
    @Override
    public void startDiscovery() {
        // IV - Only if still in use
        if (StaticVariables.usingNearby) {
            Log.d("NearbyConnections", "startDiscovery()");
            if (!isDiscovering) {
                DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build();
                Nearby.getConnectionsClient(context)
                        .startDiscovery(serviceId, endpointDiscoveryCallback(), discoveryOptions)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We're discovering!
                                    updateConnectionLog(context.getResources().getString(R.string.connections_discover));
                                    isDiscovering = true;
                                    Log.d("NearbyConnections", "startDiscovery() - success");
                                })
                        .addOnFailureListener(
                                (Exception e) -> {
                                    // We're unable to start discovering.
                                    stopDiscovery();
                                    Log.d("NearbyConnections", "startDiscovery() - failure: " + e);
                                });
            } else {
                Log.d("NearbyConnections", "startDiscovery() - already discovering");
            }
            // IV - Stop 30s after this (latest) call
            if (isDiscovering) {
                stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
                stopDiscoveryHandler.postDelayed(stopDiscoveryRunnable, 30000);
            }
        }
    }
    @Override
    public void stopAdvertising() {
        Log.d("NearbyConnections", "stopAdvertising()");
        if (isAdvertising) {
            isAdvertising = false;
            try {
                Nearby.getConnectionsClient(context).stopAdvertising();
                updateConnectionLog(context.getResources().getString(R.string.connections_service_stop));
                Log.d("NearbyConnections", "stopAdvertising() - success");
            } catch (Exception e) {
                Log.d("NearbyConnections", "stopAdvertising() - failure: " + e);
            }

        } else {
            Log.d("NearbyConnections", "stopAdvertising() - wasn't advertising");
        }
    }
    @Override
    public void stopDiscovery() {
        Log.d("NearbyConnections", "stopDiscovery()");
        if (isDiscovering) {
            isDiscovering = false;
            try {
                Nearby.getConnectionsClient(context).stopDiscovery();
                updateConnectionLog(context.getResources().getString(R.string.connections_discover_stop));
            } catch (Exception e) {
                Log.d("NearbyConnections", "stopDiscovery() - failure: " + e);
            }
        } else {
            Log.d("NearbyConnections", "stopDiscovery() -  wasn't discovering");
        }
    }

    private String getDeviceNameFromId(String endpointId) {
        // When requesting connections, the proper device name is stored in an arraylist like endpointId__deviceName
        for (String s:connectedEndPointsNames) {
            if (s.startsWith(endpointId+"__")) {
                return s.replace(endpointId+"__","");
            }
        }
        return endpointId;
    }

    public String getUserNickname() {
        try {
            // IV - If deviceName is null, determine the current deviceName.
            if (StaticVariables.deviceName == null) {
                // IV - If user has set a device id then use, otherwise use Bluetooth name. If no Bluetooth name (should never happen), use a random name.
                StaticVariables.deviceName = preferences.getMyPreferenceString(context, "deviceId", "");
                if (StaticVariables.deviceName.isEmpty()) {
                    if (FullscreenActivity.mBluetoothName != null) {
                        StaticVariables.deviceName = FullscreenActivity.mBluetoothName;
                    } else {
                        StaticVariables.deviceName = UUID.randomUUID().toString().substring(0, 8).toUpperCase(StaticVariables.locale);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StaticVariables.deviceName;
    }
    private String userConnectionInfo(String endpointId, ConnectionInfo connectionInfo) {
        Log.d("NearbyConnections", "endpointId=" + endpointId + "  name=" + connectionInfo.getEndpointName() + "  getAuthenticationToken=" + connectionInfo.getAuthenticationDigits());
        return endpointId + "__" + connectionInfo.getEndpointName();
    }
    private void delayAcceptConnection(String endpointId, ConnectionInfo connectionInfo) {
        // For stability add a small delay
        Handler waitAccept = new Handler();
        waitAccept.postDelayed(() -> {
            // Add a note of the nice name matching the endpointId
            String id = userConnectionInfo(endpointId, connectionInfo);
            Log.d("NearbyConnections", "looking for " + id);

            // Take a note of the nice name matching the endpointId to use on connection STATUS_OK
            connectionId = userConnectionInfo(endpointId, connectionInfo);
            connectionEndPointName = connectionInfo.getEndpointName();

            // The user confirmed, so we can accept the connection.
            Nearby.getConnectionsClient(activity)
                    .acceptConnection(endpointId, payloadCallback());
        }, 200);
    }

    private ConnectionLifecycleCallback connectionLifecycleCallback() {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {

                // If the device was previously connected, try to reconnect silently
                if (connectedDeviceIds.contains(connectionInfo.getEndpointName())) {
                    delayAcceptConnection(endpointId, connectionInfo);
                } else {
                    // Allow clients to connect to the host when the Connect menu is open, or the user switches off the requirement for the Connect menu to be open
                    if (StaticVariables.whichOptionMenu.equals("CONNECT") || (StaticVariables.isHost && !preferences.getMyPreferenceBoolean(context, "nearbyHostMenuOnly", false))) {
                        new AlertDialog.Builder(context)
                                .setTitle(context.getResources().getString(R.string.accept_connection) + " " + connectionInfo.getEndpointName())
                                .setMessage(context.getResources().getString(R.string.accept_code) + " " + connectionInfo.getAuthenticationDigits())
                                .setPositiveButton(
                                        context.getResources().getString(R.string.ok),
                                        (DialogInterface dialog, int which) -> delayAcceptConnection(endpointId, connectionInfo))
                                .setNegativeButton(
                                        context.getResources().getString(R.string.cancel),
                                        (DialogInterface dialog, int which) ->
                                                // The user canceled, so we should reject the connection.
                                                Nearby.getConnectionsClient(context).rejectConnection(endpointId))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        // The user is not accepting new connections, so we should reject the connection.
                        Nearby.getConnectionsClient(context).rejectConnection(endpointId);
                    }
                }
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution connectionResolution) {
                switch (connectionResolution.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                    case ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT:
                        // IV - Added handling of when already connected
                        // We're connected! Can now start sending and receiving data.
                        connectedEndPoints.add(endpointId);
                        StaticVariables.isConnected = true;
                        updateConnectionLog(context.getResources().getString(R.string.connections_connected) + " " + connectionEndPointName);
                        if (!connectedEndPointsNames.contains(connectionId)) {
                            connectedEndPointsNames.add(connectionId);
                            Log.d("NearbyConnections", connectionId + " not found, adding");
                        }
                        if (!connectedDeviceIds.contains(connectionEndPointName)) {
                            connectedDeviceIds.add(connectionEndPointName);
                            Log.d("NearbyConnections", connectionEndPointName + " not found, adding");
                        }

                        if (StaticVariables.isHost) {
                            // try to send the current song payload
                            sendSongPayload();
                        } else {
                            // We can stop discovery now
                            stopDiscovery();
                        }
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        // The connection was rejected by one or both sides.
                        updateConnectionLog(context.getResources().getString(R.string.connections_failure) + " " + getUserNickname() + " <-> " + getDeviceNameFromId(endpointId));
                        break;
                    default:
                        // Unknown status code
                        Log.d("NearbyConnections", "Unknown status code");
                        break;
                }
            }

            @Override
            public void onDisconnected(@NonNull String endpointId) {
                StaticVariables.isConnected = false;
                Log.d("NearbyConnections", "On disconnect");
                connectedEndPoints.remove(endpointId);
                String deviceName = getDeviceNameFromId(endpointId);
                connectedEndPointsNames.remove(endpointId + "__" + deviceName);
                // IV - Only if still in use
                if (StaticVariables.usingNearby) {
                    updateConnectionLog(context.getResources().getString(R.string.connections_disconnect) + " " + deviceName);
                }

                if (StaticVariables.isHost) {
                    // Check if we have valid connections
                    StaticVariables.isConnected = stillValidConnections();
                } else {
                    // Clients should try to connect again after 2 seconds
                    StaticVariables.isConnected = false;
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(), 2000);
                }
            }
        };
    }
    private boolean stillValidConnections() {
        if (connectedEndPoints.size() >= 1) {
            for (String s : connectedEndPoints) {
                if (s != null && !s.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // Called by clients
    private EndpointDiscoveryCallback endpointDiscoveryCallback() {
        return new EndpointDiscoveryCallback() {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
                if (!findEndpoints(endpointId, discoveredEndpointInfo)) {
                    // Only attempt a connection if we aren't already connected
                    Nearby.getConnectionsClient(context)
                            .requestConnection(getUserNickname(), endpointId, connectionLifecycleCallback())
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        Log.d("NearbyConnections", "Trying to connect to host: " + endpointId);
                                        // We successfully requested a connection. Now both sides
                                        // must accept before the connection is established.
                                        updateConnectionLog(context.getResources().getString(R.string.connections_searching));
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        // IV - Added handling of when already connected
                                        if (((ApiException) e).getStatusCode() == ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT) {
                                            StaticVariables.isConnected = true;
                                            updateConnectionLog(context.getResources().getString(R.string.connections_connected) + " " + getDeviceNameFromId(endpointId));
                                            // IV - Already connected so replay last incoming song
                                            if (!incomingPrevious.equals("")) {
                                                String incoming = incomingPrevious;
                                                incomingPrevious = "";
                                                payloadOpenSong(incoming);
                                            }
                                            // We can stop discovery now
                                            stopDiscovery();
                                        } else {
                                            // Nearby Connections failed to request the connection.
                                            updateConnectionLog(context.getResources().getString(R.string.connections_failure) + " " + getDeviceNameFromId(endpointId));
                                        }
                                        Log.d("NearbyConnections", "Connections failure: " + e);
                                    });
                }
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId) {
                Log.d("NearbyConnections", "onEndPointlost");
                updateConnectionLog(context.getResources().getString(R.string.connections_disconnect) + " " + getDeviceNameFromId(endpointId));
                // Check if we have valid connections
                StaticVariables.isConnected = stillValidConnections();
                // Try to connect again after 2 seconds
                if (!StaticVariables.isHost) {
                    Handler h = new Handler();
                    h.postDelayed(() -> startDiscovery(), 2000);
                }
            }
        };
    }

    private boolean findEndpoints(String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
        Log.d("NearbyConnections", "findEndpoints: endpointId=" + endpointId);
        Log.d("NearbyConnections", "findEndpoints: discoveredEnpointInfo.getEndpointName()=" + discoveredEndpointInfo.getEndpointName());
        for (String s : connectedEndPoints) {
            Log.d("NearbyConnections", "findEndpoints: ArrayList connectedEndPoints:" + s);
        }
        for (String s : connectedEndPointsNames) {
            Log.d("NearbyConnections", "findEndpoints: ArrayList connectedEndPointsNames:" + s);
        }
        return connectedEndPointsNames.contains(discoveredEndpointInfo.getEndpointName());
    }
    public boolean sendSongPayload() {
        // IV - HOST: Cancel previous song transfers - a new song is being sent
        cancelTransferIds();

        String infoPayload;
        String infoFilePayload = null;
        Payload payloadFile;
        boolean largePayLoad = false;

        Uri uri = storageAccess.getUriForItem(context, preferences, "Songs", StaticVariables.whichSongFolder, StaticVariables.songfilename);

        // IV - Process each end point - we need a unique ParcelFileDescriptor if a file is sent
        for (String endpointId : connectedEndPoints) {
            // IV - Send current section as a pending section change (-ve offset by 1) for use by CLIENT song load
            infoPayload = "___section___-" + (1 + StaticVariables.currentSection);
            Nearby.getConnectionsClient(context).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));

            infoPayload = null;
            if (FullscreenActivity.isSong) {
                // By default, this should be smaller than 32kb, so probably going to send as bytes
                // We'll measure the actual size to check though
                infoPayload = StaticVariables.whichSongFolder + "_xx____xx_" + StaticVariables.songfilename +
                        "_xx____xx_" + FullscreenActivity.whichDirection + "_xx____xx_" +
                        FullscreenActivity.myXML;
                // Check the size.  If it is bigger than the 32kb (go 30kb to play safe!) allowed for bytes, switch to file
                byte[] mybytes = infoPayload.getBytes();
                if (mybytes.length > 30000) {
                    infoPayload = null;
                } else {
                    // Just send the bytes
                    Nearby.getConnectionsClient(context).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
                }
            }

            if (infoPayload == null) {
                payloadFile = null;
                // We will send as a file
                try {
                    ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                    if (parcelFileDescriptor != null) {
                        payloadFile = Payload.fromFile(parcelFileDescriptor);
                        infoFilePayload = "FILE:" + payloadFile.getId() + ":" +
                                StaticVariables.whichSongFolder + "_xx____xx_" + StaticVariables.songfilename +
                                "_xx____xx_" + FullscreenActivity.whichDirection;
                    }
                } catch (Exception e) {
                    Log.d("NearbyConnections", "Error trying to send file: " + e);
                    payloadFile = null;
                }
                if (payloadFile != null) {
                    // Send the file descriptor as bytes, then the file
                    Nearby.getConnectionsClient(context).sendPayload(endpointId, Payload.fromBytes(infoFilePayload.getBytes()));
                    Nearby.getConnectionsClient(context).sendPayload(endpointId, payloadFile);
                    // IV - Check the size.  If it is large then indicate to inform user
                    if (Objects.requireNonNull(payloadFile.asFile()).getSize() > 30000) {
                        largePayLoad = true;
                    }
                }
            }
        }
        return largePayLoad;
    }
    public void doSendPayloadBytes(String infoPayload) {
        if (StaticVariables.isHost) {
            for (String endpointId : connectedEndPoints) {
                Nearby.getConnectionsClient(context).sendPayload(endpointId, Payload.fromBytes(infoPayload.getBytes()));
            }
        }
    }
    private void payloadAutoscroll(String incoming) {
        // It sends autoscroll startstops as autoscroll_start or autoscroll_stop
        if (StaticVariables.whichMode.equals("Performance")) {
            // Adjust only when not already in the correct state
            if (nearbyReturnActionsInterface != null && !(StaticVariables.isautoscrolling == incoming.equals("autoscroll_start"))) {
                nearbyReturnActionsInterface.gesture5();
            }
        }
    }
    private void payloadSection(String incoming) {
        if (!StaticVariables.whichMode.equals("Performance")) {
            //Log.d("NearbyConnections", "pendingCurrentSection In " + pendingCurrentSection);
            int mysection = processSong.getNearbySection(incoming);
            //Log.d("NearbyConnections", "mysection " + mysection);
            if (mysection >= 0) {
                if (pendingCurrentSection < 0) {
                    // IV - A song load is pending - 'Store' the section change (-ve offset by 1) for use by song load
                    pendingCurrentSection = -(1 + mysection);
                } else {
                    if (StaticVariables.currentSection != mysection &&
                        nearbyReturnActionsInterface != null) {
                        // IV - Do the section change
                        StaticVariables.currentSection = mysection;
                        if (!FullscreenActivity.alreadyloading) {
                            nearbyReturnActionsInterface.selectSection(mysection);
                        }
                    }
                }
            } else {
                // IV - A Host has passed a section directly into pending state ready for a following song load
                pendingCurrentSection = mysection;
            }
            //Log.d("NearbyConnections", "pendingCurrentSection Out " + pendingCurrentSection);
        }
    }
    private void payloadOpenSong(String incoming) {
        // IV - CLIENT: Cancel previous song transfers - a new song has arrived
        cancelTransferIds();

        // New method sends OpenSong songs in the format of
        //  FOLDER_xx____xx_FILENAME_xx____xx_R2L/L2R_xx____xx_<?xml>

        ArrayList<String> receivedBits = processSong.getNearbyIncoming(incoming);
        boolean incomingChange = (!incoming.equals(incomingPrevious));

        if (incomingChange) {
            incomingPrevious = incoming;
            OutputStream outputStream;

            // If 'Receiving host songs' then only BYTES songs with xml arrive here - we use the  4th <?xml> bit
            // If not 'Receiving host songs' then all songs arrive here including FILES: which have a dummy 4th <?xml> bit - we do not use xml
            boolean songReceived = (receivedBits.size() >= 4);

            if (songReceived) {
                Log.d("NearbyConnections", "foldernamepair " + receivedBits.get(0) + "_xx____xx_" + receivedBits.get(1));
                if (!StaticVariables.isHost && StaticVariables.isConnected && StaticVariables.receiveHostFiles) {
                    FullscreenActivity.whichDirection = receivedBits.get(2);

                    // If the user wants to keep the host file, we will save it to our storage.
                    // If we already have it, it will overwrite it, if not, we add it
                    Uri newLocation;
                    if (StaticVariables.keepHostFiles) {
                        // Prepare the output stream in the client Songs folder
                        // Check the folder exists, if not, create it
                        storageAccess.createFile(context, preferences, DocumentsContract.Document.MIME_TYPE_DIR, "Songs", receivedBits.get(0), "");
                        newLocation = storageAccess.getUriForItem(context, preferences, "Songs", receivedBits.get(0), receivedBits.get(1));
                        // Create the file if it doesn't exist
                        storageAccess.lollipopCreateFileForOutputStream(context, preferences, newLocation, null, "Songs", receivedBits.get(0), receivedBits.get(1));
                        outputStream = storageAccess.getOutputStream(context, newLocation);
                        StaticVariables.whichSongFolder = receivedBits.get(0);
                        StaticVariables.songfilename = receivedBits.get(1);
                        // Add to the sqldatabase
                        sqLiteHelper.createSong(context, StaticVariables.whichSongFolder, StaticVariables.songfilename);
                    } else {
                        newLocation = storageAccess.getUriForItem(context, preferences, "Received", "", "ReceivedSong");
                        // Prepare the output stream in the Received folder - just keep a temporary version
                        storageAccess.lollipopCreateFileForOutputStream(context, preferences, newLocation, null, "Received", "", "ReceivedSong");
                        outputStream = storageAccess.getOutputStream(context, newLocation);
                        StaticVariables.whichSongFolder = "../Received";
                        StaticVariables.songfilename = "ReceivedSong";
                        // IV - Store the received song filename in case the user wants to duplicate the received song
                        StaticVariables.receivedSongfilename = receivedBits.get(1);
                    }

                    // Write the file to the desired output stream and load
                    if (nearbyReturnActionsInterface != null) {
                        storageAccess.writeFileFromString(receivedBits.get(3), outputStream);
                        StaticVariables.currentSection = pendingCurrentSection;
                        nearbyReturnActionsInterface.loadSong();
                    }
                } else if (!StaticVariables.isHost && StaticVariables.isConnected) {
                    // We just want to trigger loading the song on our device (if we have it).
                    // If not, we get notified it doesn't exits
                    StaticVariables.whichSongFolder = receivedBits.get(0);
                    StaticVariables.songfilename = receivedBits.get(1);
                    FullscreenActivity.whichDirection = receivedBits.get(2);

                    // Now load the song
                    if (nearbyReturnActionsInterface != null) {
                        StaticVariables.currentSection = pendingCurrentSection;
                        nearbyReturnActionsInterface.loadSong();
                    }
                }
            }

        } else {
            Log.d("NearbyConnections", "payloadOpenSong - no change as unchanged payload");
        }
        // IV - 0 is no pending
        pendingCurrentSection = 0;
    }
    private void payloadFile(Payload payload, String foldernamepair) {
        // IV - CLIENT: Cancel previous song transfers - a new song has arrived
        cancelTransferIds();

        // If songs are too big, then we receive them as a file rather than bytes
        Log.d("NearbyConnections", "foldernamepair " + foldernamepair);
        String[] bits = foldernamepair.split("_xx____xx_");
        String folder = bits[0];
        String filename = bits[1];
        FullscreenActivity.whichDirection = bits[2];
        boolean movepage = false;
        if ((folder.equals(StaticVariables.whichSongFolder) || StaticVariables.whichSongFolder.equals("../Received"))
                && filename.equals(StaticVariables.songfilename) && filename.toLowerCase(StaticVariables.locale).endsWith(".pdf")) {
            // We are likely trying to move page to an already received file
            movepage = true;
        } else {
            FullscreenActivity.pdfPageCurrent = 0;
            StaticVariables.currentSection = 0;
        }
        Uri newLocation = null;
        if (!StaticVariables.isHost && StaticVariables.isConnected && StaticVariables.receiveHostFiles && StaticVariables.keepHostFiles) {
            // The new file goes into our main Songs folder
            newLocation = storageAccess.getUriForItem(context, preferences, "Songs", folder, filename);
            storageAccess.lollipopCreateFileForOutputStream(context, preferences, newLocation, null, "Songs", folder, filename);
        } else if (!StaticVariables.isHost && StaticVariables.isConnected && StaticVariables.receiveHostFiles) {
            // The new file goes into our Received folder
            folder = "../Received";
            // IV - Store the received song filename in case the user wants to duplicate the received song
            StaticVariables.receivedSongfilename = filename;
            newLocation = storageAccess.getUriForItem(context, preferences, "Received", "", filename);
            storageAccess.lollipopCreateFileForOutputStream(context, preferences, newLocation, null, "Received", "", filename);
        }
        StaticVariables.whichSongFolder = folder;
        StaticVariables.songfilename = filename;
        Log.d("NearbyConnections", "newLocation=" + newLocation);
        if (movepage) {
            if (FullscreenActivity.whichDirection.equals("L2R")) {
                // Go back
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.goToPreviousItem();
                }
            } else {
                // Go forward
                if (nearbyReturnActionsInterface != null) {
                    nearbyReturnActionsInterface.goToNextItem();
                }
            }
        } else if (newLocation != null && payload != null && payload.asFile() != null) { // i.e. we have received the file by choice
            InputStream inputStream = new FileInputStream(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor());
            Uri originalUri = Uri.parse(Objects.requireNonNull(payload.asFile()).asParcelFileDescriptor().getFileDescriptor().toString());
            OutputStream outputStream = storageAccess.getOutputStream(context, newLocation);
            if (storageAccess.copyFile(inputStream, outputStream)) {
                if (nearbyReturnActionsInterface != null) {
                    StaticVariables.currentSection = pendingCurrentSection;
                    nearbyReturnActionsInterface.loadSong();
                    // IV - End pending
                    pendingCurrentSection = 0;
                }
            }
            Log.d("NearbyConnections", "originalUri=" + originalUri);
            try {
                if (storageAccess.uriExists(context, originalUri)) {
                    storageAccess.deleteFile(context, originalUri);
                }
            } catch (Exception e) {
                Log.d("NearbyConnections", "Error trying to delete originalUri");
            }
        } else {
            if (nearbyReturnActionsInterface != null) {
                StaticVariables.currentSection = pendingCurrentSection;
                nearbyReturnActionsInterface.loadSong();
                // IV - End pending
                pendingCurrentSection = 0;
            }
        }
    }
    private void payloadEndpoints(String incomingEndpoints) {
        if (!StaticVariables.isHost) {
            Log.d("NearbyConnections", "I am a client, but the host has sent me this info about connected endpoints: " + incomingEndpoints);
            // The host has sent a note of the connected endpoints in case we become the host
            connectedEndPoints.clear();
            String[] incomingEPs = incomingEndpoints.split("_ep__ep_");
            Collections.addAll(connectedEndPoints, incomingEPs);
        }
    }
    private SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
    private SimpleArrayMap<Long, String> fileNewLocation = new SimpleArrayMap<>();
    private PayloadCallback payloadCallback() {
        return new PayloadCallback() {
            @Override
            public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
                if (!StaticVariables.isHost) {
                    // We can deal with the incoming payload!
                    if (payload.getType() == Payload.Type.FILE) {
                        Log.d("NearbyConnections","FILE NOTED " + payload.getId());
                        // Make a note of it.  Nothing happens until complete
                        incomingFilePayloads.put(payload.getId(), payload);
                    } else if (payload.getType() == Payload.Type.BYTES) {
                        // We're dealing with bytes
                        String incoming = null;
                        if (payload.asBytes() != null) {
                            byte[] bytes = payload.asBytes();
                            if (bytes != null) {
                                incoming = new String(bytes);
                            }
                        }
                        if (incoming != null) {
                            if (incoming.startsWith("FILE:")) {
                                Log.d("NearbyConnections","BYTES for FILE " + payload.getId());
                                // Add the file location to the arraymap
                                incoming = incoming.replaceFirst("FILE:", "");
                                String id = incoming.substring(0, incoming.indexOf(":"));
                                id = id.replace(":", "");
                                String foldernamepair = incoming.substring(incoming.indexOf(":"));
                                foldernamepair = foldernamepair.replace(":", "");
                                if (StaticVariables.receiveHostFiles) {
                                    fileNewLocation.put(Long.parseLong(id), foldernamepair);
                                } else {
                                    // IV - CLIENT: Proceed as if BYTES (with unused dummy 4th bit) as we are not receiving FILES
                                    Log.d("NearbyConnections", "USED AS BYTES as FILE is not needed");
                                    payloadOpenSong(foldernamepair + "_xx____xx_<?xml>");
                                }
                                latestfoldernamepair = foldernamepair;
                            } else if (incoming.contains("autoscroll_")) {
                                // IV - Autoscroll only in Performance mode when user option is selected
                                if (StaticVariables.whichMode.equals("Performance") && StaticVariables.receiveHostAutoscroll) {
                                    payloadAutoscroll(incoming);
                                }
                            } else if (incoming.contains("___section___")) {
                                //Log.d("NearbyConnections", "Section " + incoming);
                                // IV - Section change only in Stage and Presentation mode when user option is selected
                                if (!StaticVariables.whichMode.equals("Performance") && StaticVariables.receiveHostSongSections) {
                                    payloadSection(incoming);
                                }
                            } else if (incoming.contains("_ep__ep_")) {
                                payloadEndpoints(incoming);
                            } else if (incoming.contains("_xx____xx_")) {
                                Log.d("NearbyConnections","BYTES for " + payload.getId());
                                latestfoldernamepair = incoming.substring(0,incoming.indexOf("_xx____xx_<"));
                                payloadOpenSong(incoming);
                            }
                        }
                    }
                    // not dealing with files as it is complex with scoped storage access
                    // also don't want user's download folder getting clogged!
                }
            }


            @Override
            public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                // IV - If we are a client and not 'receiving host files' then cancel these uneeded FILE transfers
                if (!StaticVariables.isHost && !StaticVariables.receiveHostFiles) {
                    if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                        Log.d("NearbyConnections", "Cancelled Id " + payloadTransferUpdate.getPayloadId());
                        Nearby.getConnectionsClient(context).cancelPayload(payloadTransferUpdate.getPayloadId());
                    }
                } else {
                    if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        // For bytes this is sent automatically, but it's the file we are interested in here
                        Payload payload;
                        if (incomingFilePayloads.containsKey(payloadTransferUpdate.getPayloadId())) {
                            payload = incomingFilePayloads.get(payloadTransferUpdate.getPayloadId());
                            String foldernamepair = fileNewLocation.get(payloadTransferUpdate.getPayloadId());
                            if (foldernamepair == null) {
                                foldernamepair = "../Received_xx____xx_ReceivedSong";
                            }
                            incomingFilePayloads.remove(payloadTransferUpdate.getPayloadId());
                            fileNewLocation.remove(payloadTransferUpdate.getPayloadId());
                            Log.d("NearbyConnections","FILE for " + payloadTransferUpdate.getPayloadId() + " " + foldernamepair);
                            // IV - A file payload needs to match with the latest foldernamepair received to be used
                            if (foldernamepair.equals(latestfoldernamepair)) {
                                // IV - The detection of song change needs reset
                                incomingPrevious = "";
                                payloadFile(payload,foldernamepair);
                            }
                        }
                    }
                    // IV - Keep a record of Ids
                    if (!(payLoadTransferIds.contains(payloadTransferUpdate.getPayloadId() + " "))) {
                        payLoadTransferIds = payLoadTransferIds + payloadTransferUpdate.getPayloadId() + " ";
                        Log.d("NearbyConnections","Id History " + payLoadTransferIds);
                    }
                }
            }
        };
    }
    @Override
    public void turnOffNearby() {
        Log.d("NearbyConnections", "turnOffNearby()");
        Nearby.getConnectionsClient(context).stopAllEndpoints();
        // IV - Sets isAdvertising = false;
        stopAdvertising();
        stopDiscoveryHandler.removeCallbacks(stopDiscoveryRunnable);
        // IV - Sets isDiscovering = false;
        stopDiscovery();
        StaticVariables.isHost = false;
        StaticVariables.isConnected = false;
        StaticVariables.usingNearby = false;
        incomingPrevious = "";
    }
    public void cancelTransferIds() {
        // IV - Used to cancel earlier transfer Ids
        Log.d("NearbyConnections", "Cancel Ids " + payLoadTransferIds);
        if (!payLoadTransferIds.equals("")) {
            String [] Ids = payLoadTransferIds.trim().split(" ");
            payLoadTransferIds = "";
            for (String Id : Ids) {
                Nearby.getConnectionsClient(context).cancelPayload(Long.parseLong((Id.trim())));
            }
            incomingFilePayloads = new SimpleArrayMap<>();
            fileNewLocation = new SimpleArrayMap<>();
        }
    }
}