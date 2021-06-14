package com.garethevans.church.opensongtablet.backupandrestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.FragmentOsbDetailsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ImportOSBFragment extends Fragment {

    // This fragment allows users to import an OSB backup file
    // It uses the same layout as the export fragment, but changes the appopriate text

    private MainActivityInterface mainActivityInterface;
    private FragmentOsbDetailsBinding myView;

    private String importFilename;
    private Uri importUri;
    private ArrayList<String> foundFolders;
    private ArrayList<String> checkedFolders;
    private ArrayList<String> allZipItems;
    private boolean error;

    private Thread thread;
    private Runnable runnable;
    private boolean alive = true;

    private InputStream inputStream;
    private ZipInputStream zipInputStream;
    private ZipEntry ze;
    private OutputStream outputStream;
    private int zipContents;
    private int zipProgress;
    private int item;
    private String message;
    private boolean canoverwrite;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = FragmentOsbDetailsBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.import_basic));

        // Set up helpers
        setupHelpers();

        // Set up the correct values
        setupValues();

        // Find folders in the file
        findFolders();

        return myView.getRoot();
    }

    private void setupHelpers() {
        importFilename = mainActivityInterface.getImportFilename();
        importUri = mainActivityInterface.getImportUri();
    }

    private void setupValues() {
        myView.importTitle.setText(getString(R.string.import_osb));
        myView.backupName.getEditText().setText(importFilename);
        myView.backupName.getEditText().setEnabled(true);
        myView.backupName.setFocusable(false);
        myView.overWrite.setVisibility(View.VISIBLE);
    }

    private void findFolders() {
        // We need to parse the .osb (zip) file to extract a list of folders it contains as AsyncTask
        error = false;
        allZipItems = new ArrayList<>();
        
        runnable = () -> {

            requireActivity().runOnUiThread(() -> {
                // Let the user know we're processing the file
                if (alive) {
                    myView.progressBar.setVisibility(View.VISIBLE);
                    myView.progressText.setVisibility(View.VISIBLE);
                    myView.progressText.setText(getString(R.string.processing));
                }
            });

            try {
                zipContents = 0;
                inputStream = mainActivityInterface.getStorageAccess().getInputStream(getActivity(),importUri);
                zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
                ZipEntry ze;

                // Add the main folder
                foundFolders = new ArrayList<>();
                if (alive) {
                    foundFolders.add(getString(R.string.mainfoldername));
                }

                // Look for directories
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    if (ze.getName().contains("/")) {
                        allZipItems.add(ze.getName());
                    } else {
                        if (alive) {
                            allZipItems.add(getString(R.string.mainfoldername) + "/" + ze.getName());
                        }
                    }
                    if (ze.isDirectory() || ze.getName().contains("/")) {
                        String thisfolder = ze.getName();
                        if (thisfolder.contains("/")) {
                            thisfolder = thisfolder.substring(0,thisfolder.lastIndexOf("/"));
                        }
                        // Only add it if we don't already have it
                        if (!foundFolders.contains(thisfolder)) {
                            // Only add if it isn't already in the array
                            foundFolders.add(thisfolder);
                        }
                    }
                }

                // Get the number of items
                zipContents = allZipItems.size();

            } catch (Exception e) {
                e.printStackTrace();
                error = true;
            } finally {
                if (zipInputStream!=null) {
                    try {
                        zipInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error = true;
                    }
                }
            }
        requireActivity().runOnUiThread(() -> {
            if (error && alive) {
                myView.progressText.setText(getString(R.string.error));
            } else if (alive){
                message = getString(R.string.songs) + ": " + zipContents;
                myView.progressText.setText(message);

                // Update the found folders
                // Sort the folders
                Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
                coll.setStrength(Collator.SECONDARY);
                Collections.sort(foundFolders, coll);

                for (String folder:foundFolders) {
                    CheckBox checkBox = new CheckBox(getContext());
                    checkBox.setText(folder);
                    checkBox.setTag(folder);
                    checkBox.setChecked(true);
                    checkBox.setPadding(16,32,16,32);
                    myView.foundFoldersListView.addView(checkBox);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        int songs = getCurrentSongs();
                        message = getString(R.string.songs) + ": " + songs;
                        myView.progressText.setText(message);
                    });
                }
                myView.createBackupFAB.setOnClickListener(v -> doImport());
                myView.backupName.setOnClickListener(v -> changeBackupFile());
            }
            myView.progressBar.setVisibility(View.GONE);
        });

        };
        thread = new Thread(runnable);
        thread.start();
    }

    private int getCurrentSongs() {
        // This matches how many songs are in the chosen folders
        zipContents = 0;
        getCheckedFolders();
        if (checkedFolders.size()>0) {
            for (String item:allZipItems) {
                if (item.contains("/")) {
                    item = item.substring(0,item.lastIndexOf("/"));
                } else {
                    item = getString(R.string.mainfoldername);
                }
                for (String checked:checkedFolders) {
                    if (checked.equals(item)) {
                        zipContents++;
                    }
                }
            }
            return zipContents;
        } else {
            return 0;
        }
    }
    private void getCheckedFolders() {
        checkedFolders = new ArrayList<>();
        for (int x=0; x<myView.foundFoldersListView.getChildCount();x++) {
            CheckBox checkBox = (CheckBox) myView.foundFoldersListView.getChildAt(x);
            if (checkBox!=null && checkBox.isChecked() && checkBox.getTag()!=null) {
                checkedFolders.add(checkBox.getTag().toString());
            }
        }
    }

    private void changeBackupFile() {
        Intent intent = mainActivityInterface.getStorageAccess().selectFileIntent(new String[] {"application/zip","application/octet-stream"});
        requireActivity().startActivityForResult(intent,
                mainActivityInterface.getPreferences().getFinalInt("REQUEST_OSB_FILE"));
    }
    private void doImport() {
        // Get the folders we've selected
        getCheckedFolders();
        zipProgress = 0;
        item = 0;

        // The actual importing runs in a new thread
        runnable = () -> {

            inputStream = mainActivityInterface.getStorageAccess().getInputStream(getActivity(), importUri);
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

            requireActivity().runOnUiThread(() -> {
                if (alive) {
                    myView.progressBar.setVisibility(View.VISIBLE);
                    canoverwrite = myView.overWrite.isChecked();
                    myView.createBackupFAB.setEnabled(false);
                    mainActivityInterface.getAb().setHomeButtonEnabled(false);
                }
            });
            
            // Go through the checked folders and check they exist on the local storage
            // If not, create them
            for (String folder : checkedFolders) {
                requireActivity().runOnUiThread(() -> {
                    if (alive) {
                        message = getString(R.string.folder) + ": " + folder;
                        myView.progressText.setText(message);
                    }
                });
                if (alive) {
                    mainActivityInterface.getStorageAccess().createFile(getActivity(), mainActivityInterface.getPreferences(), DocumentsContract.Document.MIME_TYPE_DIR,
                            "Songs", folder, "");
                }
            }
            
            // Now deal with the zip entries
            try {
                byte[] buffer = new byte[8192];
                while ((ze = zipInputStream.getNextEntry()) != null) {
                    item ++;
                    if (!ze.isDirectory()) {
                        // Get a uri for the song
                        Uri file_uri = null;
                        boolean exists = false;
                        boolean wantit = false;
                        String filename;
                        String filefolder = "";
                        if (alive) {
                            file_uri = mainActivityInterface.getStorageAccess().getUriForItem(getContext(), mainActivityInterface.getPreferences(), "Songs", "", ze.getName());
                            // If the file exists and we have allowed overwriting, or it doesn't exist and it is in the checked folders, write it
                            exists = mainActivityInterface.getStorageAccess().uriExists(getContext(), file_uri);
                            if (alive) {
                                filefolder = getString(R.string.mainfoldername);
                            }
                            if (ze.getName().contains("/")) {
                                filefolder = ze.getName().substring(0, ze.getName().lastIndexOf("/"));
                            }

                            wantit = checkedFolders.contains(filefolder);
                        }
                        if (wantit && (!exists || canoverwrite)) {
                            // We want it and either it doesn't exist, or we've selected overwriting
                            // Update the disply
                            zipProgress++;
                            requireActivity().runOnUiThread(() -> {
                                String name;
                                if (ze==null || ze.getName()==null) {
                                    name = "";
                                } else {
                                    name = ze.getName();
                                }
                                if (alive) {
                                    message = getString(R.string.processing) + " (" + zipProgress + "/" + zipContents + "):\n" + name;
                                    myView.progressText.setText(message);
                                }
                            });

                            // Make sure the file exists (might be non-existent)
                            if (!exists && alive) {
                                filename = ze.getName().replace(filefolder,"").replace("/","");
                                mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(getContext(),mainActivityInterface.getPreferences(),
                                        file_uri,null,"Songs",filefolder,filename);
                            }
                            if (alive) {
                                outputStream = mainActivityInterface.getStorageAccess().getOutputStream(getContext(), file_uri);
                            }

                            // Write the file
                            int count;
                            try {
                                if (outputStream != null && alive) {
                                    while ((count = zipInputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, count);
                                    }
                                } else {
                                    error = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    error = true;
                                }
                            }
                            if (error) {
                                error = false;
                                if (alive) {
                                    requireActivity().runOnUiThread(() -> myView.progressText.setText(getString(R.string.error)));
                                }
                            }
                        } else {
                            if (alive) {
                                requireActivity().runOnUiThread(() -> {
                                    message = getString(R.string.connections_searching) + " (" + item + "/" + allZipItems.size() + ")";
                                    myView.progressText.setText(message);
                                });
                            }
                        }
                    }
                    zipInputStream.closeEntry();
                }

                if (alive) {
                    requireActivity().runOnUiThread(() -> {
                        mainActivityInterface.getAb().setHomeButtonEnabled(true);
                        myView.progressBar.setVisibility(View.GONE);
                        myView.progressText.setText("");
                        myView.progressText.setVisibility(View.GONE);
                        myView.createBackupFAB.setEnabled(true);
                        mainActivityInterface.closeDrawer(true);

                        // Update the song index
                        mainActivityInterface.fullIndex();

                        // Navigate back to the home
                        mainActivityInterface.navHome();
                    });
                }

            } catch (Exception e) {
                // Likely the user navigated away before the process completed
                e.printStackTrace();
                mainActivityInterface.getAb().setHomeButtonEnabled(true);
                if (getContext()!=null && alive) {
                    requireActivity().runOnUiThread(() -> myView.progressText.setText(getString(R.string.error)));
                    myView.progressBar.setVisibility(View.GONE);
                    myView.createBackupFAB.setEnabled(true);
                }
            }
        };
        thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        killThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killThread();
        myView = null;
    }

    private void killThread() {
        alive = false;
        if (thread!=null) {
            thread.interrupt();
            runnable = null;
            thread = null;
        }
    }
}