package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class PopUpUSBMidiFragment extends DialogFragment {

    static PopUpUSBMidiFragment newInstance() {
        PopUpUSBMidiFragment frag;
        frag = new PopUpUSBMidiFragment();
        return frag;
    }

    private ArrayList<String> usbNames, usbManufact;
    private Handler selected;
    private Runnable runnable;
    private ProgressBar progressBar;
    private Button scanStartStop, disconnectDevice;
    private ListView usbDevices;
    private LinearLayout currentDevice;
    private TextView currentDeviceName, currentDeviceAddress;
    private MidiDeviceInfo[] infos;
    private Midi m;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        View V = inflater.inflate(R.layout.popup_mididevices, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getString(R.string.midi_usb));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, getContext());
            closeMe.setEnabled(false);
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        Preferences preferences = new Preferences();

        // Initialise the basic views
        progressBar = V.findViewById(R.id.progressBar);
        scanStartStop = V.findViewById(R.id.scanStartStop);
        usbDevices = V.findViewById(R.id.bluetoothDevices);
        currentDevice = V.findViewById(R.id.currentDevice);
        currentDeviceName = V.findViewById(R.id.currentDeviceName);
        currentDeviceAddress = V.findViewById(R.id.currentDeviceAddress);
        disconnectDevice = V.findViewById(R.id.disconnectDevice);
        Button testDevice = V.findViewById(R.id.testDevice);

        // Initialise the Midi classes
        m = new Midi();
        try {
            StaticVariables.midiManager = (MidiManager) requireActivity().getSystemService(Context.MIDI_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getContext());
        }

        selected = new Handler();
        runnable = this::displayCurrentDevice;

        displayCurrentDevice();

        disconnectDevice.setOnClickListener(view -> disconnectDevices(true));
        testDevice.setOnClickListener(view -> sendTestNote());
        progressBar.setVisibility(View.GONE);
        scanStartStop.setEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionAllowed();
        }

        scanStartStop.setOnClickListener(view -> {
            if (permissionAllowed()) {
                progressBar.setVisibility(View.VISIBLE);
                usbDevices.setEnabled(false);
                scanStartStop.setEnabled(false);
                startScan();
            } else {
                progressBar.setVisibility(View.GONE);
                scanStartStop.setEnabled(true);
                usbDevices.setEnabled(true);
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean permissionAllowed() {
        boolean allowed = true;
        int permissionCheck = requireActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            allowed = false;
            if (!requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
        return allowed;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateDevices() {
        try {
            Log.d("d", "update devices");
            if (infos != null && infos.length > 0 && usbNames != null && usbNames.size() > 0) {
                ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, usbNames);
                aa.notifyDataSetChanged();
                usbDevices.setAdapter(aa);
                usbDevices.setOnItemClickListener((adapterView, view, i, l) -> {
                    disconnectDevices(false);
                    // Display the current device
                    StaticVariables.midiDeviceName = usbNames.get(i);
                    StaticVariables.midiDeviceAddress = usbManufact.get(i);
                    StaticVariables.midiManager.openDevice(infos[i],
                            midiDevice -> {
                                StaticVariables.midiDevice = midiDevice;
                                // Check the ports aren't opened!
                                StaticVariables.midiInputPort = null;
                                StaticVariables.midiOutputPort = null;
                                Log.d("d", "Device opened = " + midiDevice);
                                MidiDeviceInfo midiDeviceInfo = midiDevice.getInfo();
                                int numInputs = midiDeviceInfo.getInputPortCount();
                                int numOutputs = midiDeviceInfo.getOutputPortCount();
                                Log.d("d", "Input ports = " + numInputs + ", Output ports = " + numOutputs);

                                boolean foundinport = false;  // We will only grab the first one
                                boolean foundoutport = false; // We will only grab the first one

                                MidiDeviceInfo.PortInfo[] portInfos = midiDeviceInfo.getPorts();
                                for (MidiDeviceInfo.PortInfo pi : portInfos) {
                                    switch (pi.getType()) {
                                        case MidiDeviceInfo.PortInfo.TYPE_INPUT:
                                            if (!foundinport) {
                                                Log.d("d", "Input port found = " + pi.getPortNumber());
                                                StaticVariables.midiInputPort = StaticVariables.midiDevice.openInputPort(pi.getPortNumber());
                                                foundinport = true;
                                            }
                                            break;
                                        case MidiDeviceInfo.PortInfo.TYPE_OUTPUT:
                                            if (!foundoutport) {
                                                Log.d("d", "Output port found = " + pi.getPortNumber());
                                                StaticVariables.midiOutputPort = StaticVariables.midiDevice.openOutputPort(pi.getPortNumber());
                                                foundoutport = true;
                                            }
                                            break;
                                    }
                                }
                                selected.postDelayed(runnable, 1000);
                            }, null);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getContext());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        if (StaticVariables.midiManager!=null) {
            infos = StaticVariables.midiManager.getDevices();
            usbNames = new ArrayList<>();
            usbManufact = new ArrayList<>();
            for (MidiDeviceInfo md : infos) {
                String manuf = "Unknown";
                String device = "Unknown";
                try {
                    device = md.getProperties().getString(MidiDeviceInfo.PROPERTY_NAME);
                    manuf = md.getProperties().getString(MidiDeviceInfo.PROPERTY_MANUFACTURER);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (device != null) {
                    usbNames.add(device);
                } else {
                    usbNames.add("Unknown");
                }
                if (manuf != null) {
                    usbManufact.add(manuf);
                } else {
                    usbManufact.add("Unknown");
                }
            }
            progressBar.setVisibility(View.GONE);
            scanStartStop.setEnabled(true);
            usbDevices.setEnabled(true);
            updateDevices();
        } else {
            StaticVariables.myToastMessage = getString(R.string.nothighenoughapi);
            ShowToast.showToast(getContext());
        }
    }

    private void displayCurrentDevice() {
        Log.d("d","displayCurrentDevice()");
        if (StaticVariables.midiDevice!=null && StaticVariables.midiDeviceName!=null && StaticVariables.midiDeviceAddress!=null) {
            currentDevice.setVisibility(View.VISIBLE);
            currentDeviceName.setText(StaticVariables.midiDeviceName);
            currentDeviceAddress.setText(StaticVariables.midiDeviceAddress);
            String d = getString(R.string.connections_disconnect) + " " + StaticVariables.midiDeviceName;
            disconnectDevice.setText(d);
        } else {
            currentDevice.setVisibility(View.GONE);
            StaticVariables.midiDeviceName = "";
            StaticVariables.midiDeviceAddress = "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendTestNote() {
        try {
            String s1 = m.buildMidiString("NoteOn",1,60,100);
            byte[] buffer1 = m.returnBytesFromHexText(s1);
            //byte[] buffer1 = m.buildMidiCommand("NoteOn","C5","127","1",null);
            m.sendMidi(buffer1);

            Handler h = new Handler();
            h.postDelayed(() -> {
                String s2 = m.buildMidiString("NoteOff",1,60,0);
                byte[] buffer2 = m.returnBytesFromHexText(s2);
                //byte[] buffer2 = m.buildMidiCommand("NoteOn","C5","0","1",null);
                m.sendMidi(buffer2);
            },1000);
            StaticVariables.myToastMessage = getString(R.string.ok);
            ShowToast.showToast(getContext());
        } catch (Exception e) {
            e.printStackTrace();
            StaticVariables.myToastMessage = getString(R.string.error);
            ShowToast.showToast(getContext());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void disconnectDevices(boolean doUpdate) {
        m.disconnectDevice();
        if (doUpdate) {
            displayCurrentDevice();
        }
    }
}
