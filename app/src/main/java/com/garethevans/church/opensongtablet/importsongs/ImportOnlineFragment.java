package com.garethevans.church.opensongtablet.importsongs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.SettingsImportOnlineBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;

public class ImportOnlineFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsImportOnlineBinding myView;
    private final String[] sources = new String[]{"UltimateGuitar", "Chordie", "SongSelect", "WorshipTogether", "UkuTabs", "HolyChords"};
    private final String[] address = new String[]{"https://www.ultimate-guitar.com/search.php?page=1&tab_type_group=text&app_name=ugt&order=myweight&type=300&title=",
            "https://www.chordie.com/results.php?q=", "https://songselect.ccli.com/Search/Results?SearchText=",
            "https://worship-songs-resources.worshiptogether.com/search?w=", "https://ukutabs.com/?s=",
            "https://holychords.com/search/?id="};
    private String webSearchFull, webAddressFinal, source, webString;
    private Song newSong;
    private UltimateGuitar ultimateGuitar;
    private Chordie chordie;
    private SongSelect songSelect;
    private ExposedDropDownSelection exposedDropDownSelection1, exposedDropDownSelection2;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsImportOnlineBinding.inflate(inflater, container, false);
        mainActivityInterface.updateToolbar(getString(R.string.import_basic) + " " + getString(R.string.online));

        // Setup helper
        setupHelpers();

        // Setup views
        setupViews();

        // Listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupHelpers() {
        newSong = new Song();
        ultimateGuitar = new UltimateGuitar();
        chordie = new Chordie();
        songSelect = new SongSelect();
        exposedDropDownSelection1 = new ExposedDropDownSelection();
        exposedDropDownSelection2 = new ExposedDropDownSelection();
    }

    private void setupViews() {
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown, sources);
        myView.onlineSource.setAdapter(exposedDropDownArrayAdapter);
        // Set the position in the list to the chosen value
        exposedDropDownSelection1.keepSelectionPosition(myView.exposedSourceLayout, myView.onlineSource, sources);
        if (mainActivityInterface.getCheckInternet().getSearchPhrase() != null) {
            myView.searchPhrase.getEditText().setText(mainActivityInterface.getCheckInternet().getSearchPhrase());
        }
        if (mainActivityInterface.getCheckInternet().getSearchSite() != null) {
            myView.onlineSource.setText(mainActivityInterface.getCheckInternet().getSearchSite());
        }
        //TODO - remove the next 2 lines
        myView.searchPhrase.post(() -> myView.searchPhrase.getEditText().setText("I surrender all"));
        myView.onlineSource.post(() -> myView.onlineSource.setText("SongSelect"));

        setupWebView();
    }

    private void changeLayouts(boolean search, boolean web, boolean save) {
        if (search) {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.VISIBLE));
        } else {
            myView.searchLayout.post(() -> myView.searchLayout.setVisibility(View.GONE));
        }
        if (web) {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.VISIBLE));
        } else {
            myView.webLayout.post(() -> myView.webLayout.setVisibility(View.GONE));
        }
        if (save) {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.VISIBLE));
        } else {
            myView.saveLayout.post(() -> myView.saveLayout.setVisibility(View.GONE));
        }

    }

    private void setupListeners() {
        myView.searchButton.setOnClickListener(v -> checkConnection());
        myView.closeSearch.setOnClickListener(v -> changeLayouts(true, false, false));
        myView.backButton.setOnClickListener(v -> goBackBrowser());
        myView.saveButton.setOnClickListener(v -> processContent());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        myView.webView.setWebChromeClient(new WebChromeClient());
        myView.webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                myView.webView.post(() -> webAddressFinal = myView.webView.getUrl());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Run a check for the desired content
                extractContent();
            }
        });
        myView.webView.getSettings().getJavaScriptEnabled();
        myView.webView.getSettings().setJavaScriptEnabled(true);
        myView.webView.getSettings().setDomStorageEnabled(true);
        myView.webView.getSettings().setLoadWithOverviewMode(true);
        myView.webView.getSettings().setUseWideViewPort(true);
        myView.webView.getSettings().setSupportZoom(true);
        myView.webView.getSettings().setBuiltInZoomControls(true);
        myView.webView.getSettings().setDisplayZoomControls(false);
        myView.webView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        myView.webView.setScrollbarFadingEnabled(false);
        myView.webView.addJavascriptInterface(new MyJSInterface(requireContext(),mainActivityInterface,this), "HTMLOUT");
        /*try {
            requireContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Log.d("d", "Error registering download complete listener");
        }*/

        myView.webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            // Show the progressBar
            myView.progressBar.post(() -> myView.progressBar.setVisibility(View.VISIBLE));
            changeLayouts(false, false, false);

            if (url.startsWith("blob")) {
                myView.webView.loadUrl(MyJSInterface.getBase64StringFromBlobUrl(url));
            } else {
                myView.webView.loadUrl(MyJSInterface.doNormalDownLoad(url));
            }
        });
    }

    private void goBackBrowser() {
        try {
            myView.webView.post(() -> myView.webView.goBack());
        } catch (Exception e) {
            // Error going back in the web view
            e.printStackTrace();
        }
    }

/*    private final BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            changeLayouts(false, false, true);
            newSong.setTitle(downloadedFile.getLastPathSegment());
            try {
                requireContext().unregisterReceiver(onComplete);
            } catch (Exception e) {
                Log.d("d", "Error unregistering receiver");
            }
        }
    };*/


    @SuppressLint("StaticFieldLeak")
    private class GetSourceCode extends AsyncTask<Object, String, String> {

        final String html;

        GetSourceCode(String s) {
            html = s;
        }

        @Override
        protected String doInBackground(Object... objects) {
            /*if (html.contains("<div id=\"LyricsText\"")) {
                filecontents = extractSongSelectUsr(html, FullscreenActivity.phrasetosearchfor);
            } else {
                filecontents = extractSongSelectChordPro(html, FullscreenActivity.phrasetosearchfor);
            }*/
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                /*if (filecontents != null && !filecontents.equals("")) {
                    //TODO
                    // setFileNameAndFolder();
                } else {
                    if (downloadcomplete) {
                        StaticVariables.myToastMessage = getActivity().getString(R.string.pdfonly);
                    } else {
                        StaticVariables.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                    }

                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                 }*/
            }
        }
    }


    @JavascriptInterface
    public void processHTML(String html) {
    }

    private void checkConnection() {
        // Check we have an internet connection
        mainActivityInterface.getCheckInternet().checkConnection(this, R.id.importOnlineFragment, mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        Log.d("d", "connected=" + connected);
        // Received back from the MainActivity after being told if we have a valid internet connection or not
        if (connected) {
            // Get the search string and build the web address
            String webAddress = "";
            String extra = "";
            if (((TextInputEditText) myView.searchPhrase.getEditText()).getText() != null) {
                mainActivityInterface.getCheckInternet().
                        setSearchPhrase(((TextInputEditText) myView.searchPhrase.getEditText()).getText().toString());
            }
            if (myView.onlineSource.getText() != null) {
                mainActivityInterface.getCheckInternet().
                            setSearchSite(myView.onlineSource.getText().toString());
                source = myView.onlineSource.getText().toString();
                if (source.equals("Chordie")) {
                    extra = "&np=0&ps=10&wf=2221&s=RPD&wf=2221&wm=wrd&type=&sp=1&sy=1&cat=&ul=&np=0";
                }
                for (int x = 0; x < sources.length; x++) {
                    if (sources[x].equals(source)) {
                        webAddress = address[x];
                    }
                }
            }
            if (mainActivityInterface.getCheckInternet().getSearchPhrase() != null &&
                    !mainActivityInterface.getCheckInternet().getSearchPhrase().isEmpty() &&
                    !webAddress.isEmpty()) {
                changeLayouts(false, true, false);
                webSearchFull = webAddress + mainActivityInterface.getCheckInternet().getSearchPhrase() + extra;
                // TODO
                //webSearchFull = "https://www.google.com/search?q=sample+pdf+download&rlz=1C5CHFA_enGB945GB945&oq=sample+pdf+download&aqs=chrome..69i57j0i20i263j0l8.5386j0j9&sourceid=chrome&ie=UTF-8";
                myView.webView.post(() -> myView.webView.loadUrl(webSearchFull));
                Log.d("d", webSearchFull);
            }
        }
    }

    private void extractContent() {
        Log.d("g", "Extract content");
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.VISIBLE));

        // Run the rest in a new thread
        new Thread(() -> {
            webString = "";
            myView.webView.post(() -> myView.webView.evaluateJavascript("javascript:document.getElementsByTagName('html')[0].innerHTML",webContent));
        }).start();
    }

    private final ValueCallback<String> webContent = new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            JsonReader reader = new JsonReader(new StringReader(value));
            reader.setLenient(true);
            try {
                if (reader.peek() == JsonToken.STRING) {
                    webString = reader.nextString();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            webString = webString.replace("\r","\n");
            String[] lines = webString.split("\n");
            for (String line:lines) {
                Log.d("d", "line:" + line);
            }
            showSaveButton();
        }
    };


    private void showSaveButton() {
        boolean show = false;
        if (webString==null) {
            webString = "";
        }
        switch (source) {
            case "UltimateGuitar":
                if (webString.contains("<div class=\"ugm-b-tab--content js-tab-content\">")) {
                    show = true;
                }
                break;
            case "Chordie":
                if (webString.contains("<textarea id=\"chordproContent\"")) {
                    show = true;
                }
                break;
            case "SongSelect":
                if (webString.contains("<span class=\"cproTitleLine\">")) {
                    show = true;
                }
                break;
        }
        if (show) {
            myView.saveButton.post(() -> myView.saveButton.show());
        } else {
            myView.saveButton.post(() -> myView.saveButton.hide());
        }
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.GONE));
    }

    private void processContent() {
        Log.d("ImportOnline","getting here");
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.VISIBLE));
        newSong = new Song();

        switch (source) {
            case "UltimateGuitar":
                Log.d("ImportOnline","getting here");
                newSong = ultimateGuitar.processContent(requireContext(),mainActivityInterface,newSong,webString);
                Log.d("ImportOnline","title="+newSong.getTitle());
                Log.d("ImportOnline","author="+newSong.getAuthor());
                Log.d("ImportOnline","key="+newSong.getKey());
                Log.d("ImportOnline","capo="+newSong.getCapo());
                Log.d("ImportOnline","lyrics="+newSong.getLyrics());
                break;
            case "Chordie":
                Log.d("ImportOnline","getting here");
                newSong = chordie.processContent(requireContext(),mainActivityInterface,newSong,webString);
                Log.d("ImportOnline","title="+newSong.getTitle());
                Log.d("ImportOnline","author="+newSong.getAuthor());
                Log.d("ImportOnline","key="+newSong.getKey());
                Log.d("ImportOnline","capo="+newSong.getCapo());
                Log.d("ImportOnline","lyrics="+newSong.getLyrics());
                break;
            case "SongSelect":
                Log.d("ImportOnline","getting here SongSelect");
                newSong = songSelect.processContent(mainActivityInterface,newSong,webString);
                Log.d("ImportOnline","title="+newSong.getTitle());
                Log.d("ImportOnline","author="+newSong.getAuthor());
                Log.d("ImportOnline","key="+newSong.getKey());
                Log.d("ImportOnline","copyright="+newSong.getCopyright());
                Log.d("ImportOnline","tempo="+newSong.getTempo());
                Log.d("ImportOnline","timesig="+newSong.getTimesig());
                Log.d("ImportOnline","ccli="+newSong.getCcli());
                Log.d("ImportOnline","lyrics="+newSong.getLyrics());
                break;
        }

        // Set up the save layout
        //setupSaveLayout();
    }

    public void finishedDownloadPDF(Uri uri) {
        // This is sent from MainActivity after a pdf file was downloaded
        // Fix the views
        if (uri!=null) {
            myView.progressBar.setVisibility(View.GONE);
            changeLayouts(false, false, true);
            String filename = myView.saveFilename.getEditText().getText().toString();
            if (uri != null) {
                filename = uri.getLastPathSegment();
            }
            setupSaveLayout();
            myView.saveFilename.getEditText().setText(filename);
            myView.saveSong.setOnClickListener(v -> copyPDF(uri));
        }
    }

    private void setupSaveLayout() {
        // Set up the save layout
        myView.saveFilename.post(() -> myView.saveFilename.getEditText().setText(newSong.getTitle()));
        // Get the folders available
        ArrayList<String> availableFolders = mainActivityInterface.getStorageAccess().getSongFolders(requireContext(),
                mainActivityInterface.getStorageAccess().listSongs(requireContext(), mainActivityInterface.getPreferences(), mainActivityInterface.getLocale()), true, null);
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.exposed_dropdown,availableFolders);
        myView.folderChoice.setAdapter(exposedDropDownArrayAdapter);
        myView.folderChoice.setText(mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)));
        // Set the position in the list to the chosen value
        exposedDropDownSelection2.keepSelectionPosition(myView.exposedFolderLayout,myView.folderChoice,availableFolders);
        changeLayouts(false,false,true);
        myView.saveSong.setOnClickListener(v -> saveTheSong());
        myView.progressBar.post(() -> myView.progressBar.setVisibility(View.GONE));
    }

    // This is the save function for downloaded PDF files
    private void copyPDF(Uri inputUri) {
        // Prepare the output file
        String filename = "SongSelect.pdf";
        String folder = mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername));
        if (myView.folderChoice.getText()!=null) {
            folder = myView.folderChoice.getText().toString();
        }
        if (myView.saveFilename.getEditText()!=null && myView.saveFilename.getEditText().getText()!=null) {
            filename = myView.saveFilename.getEditText().getText().toString();
        }
        if (!filename.toLowerCase().endsWith(".pdf")) {
            filename = filename + ".pdf";
        }

        // Update the song pdf values
        newSong.setTitle(filename);
        newSong.setFilename(filename);
        newSong.setFolder(folder);
        newSong.setFiletype("PDF");

        Uri outputUri = mainActivityInterface.getStorageAccess().getUriForItem(requireContext(),
                mainActivityInterface.getPreferences(),"Songs",folder,filename);
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(requireContext(),
                mainActivityInterface.getPreferences(),outputUri,null,"Songs",folder,filename);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(requireContext(),outputUri);
        InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(requireContext(),inputUri);
        try {
            // Copy the file
            mainActivityInterface.getStorageAccess().copyFile(inputStream,outputStream);

            // Update the current song
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"whichSongFolder",folder);
            mainActivityInterface.getPreferences().setMyPreferenceString(requireContext(),"songfilename",filename);

            // Update the main and nonopensong databases
            mainActivityInterface.getSQLiteHelper().createSong(requireContext(),mainActivityInterface,folder,filename);
            mainActivityInterface.getSQLiteHelper().updateSong(requireContext(),mainActivityInterface,newSong);
            mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(requireContext(),mainActivityInterface,folder,filename);
            mainActivityInterface.getNonOpenSongSQLiteHelper().updateSong(requireContext(),mainActivityInterface,newSong);

            // Let the user know and show the song
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.success));
            mainActivityInterface.navHome();

        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.error));
        }
    }

    private void saveTheSong() {
        // Update the newSong values from what the user chose
        String getName = newSong.getTitle();
        String getFolder = getString(R.string.mainfoldername);
        if (myView.saveFilename.getEditText().getText()!=null) {
            getName = myView.saveFilename.getEditText().getText().toString();
        }
        if (myView.folderChoice.getText()!=null) {
            getFolder = myView.folderChoice.getText().toString();
        }
        newSong.setTitle(getName);
        newSong.setFilename(getName);
        newSong.setFolder(getFolder);
        newSong.setSongid(mainActivityInterface.getCommonSQL().getAnySongId(getFolder,getName));
        if (mainActivityInterface.getSaveSong().
                doSave(requireContext(),mainActivityInterface,newSong,newSong,false,false)) {
            // Update the songid file (used later)
            mainActivityInterface.getStorageAccess().writeSongIDFile(requireContext(),
                    mainActivityInterface.getPreferences(),
                    mainActivityInterface.getStorageAccess().getSongIDsFromFile(requireContext()));

            // Add the song to the database
            mainActivityInterface.getSQLiteHelper().createSong(requireContext(), mainActivityInterface,
                    newSong.getFolder(), newSong.getFilename());
            mainActivityInterface.getSQLiteHelper().updateSong(requireContext(), mainActivityInterface, newSong);

            // Set the current song to this
            mainActivityInterface.getPreferences().
                    setMyPreferenceString(requireContext(), "whichSongFolder", newSong.getFolder());
            mainActivityInterface.getPreferences().
                    setMyPreferenceString(requireContext(), "songfilename", newSong.getFilename());

            // Send an instruction to update the song menu (no need for full reindex)
            mainActivityInterface.updateSongMenu(newSong);
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.success));

            mainActivityInterface.navHome();
        } else {
            mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.error));
        }
    }

/*
    @SuppressLint("StaticFieldLeak")
    private class DownloadWebTextTask extends AsyncTask<String, Void, String> {


        @Override
        protected void onPostExecute(String result) {
*/
            // Split the result into lines
            //Now look to see if the webcontent has the ChordPro text in it
            // Check we aren't trying to use the tab-pro page!
            /*try {
                String address = webresults_WebView.getUrl();
                // Fix the foreign characters
                if (result!=null) {
                    result = fixForeignLanguageHTML(result);
                }

                if (address != null && (address.contains("/tab-pro/") || address.contains("/chords-pro/"))) {
                    StaticVariables.myToastMessage = requireActivity().getResources().getText(R.string.not_allowed).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                } else if (result != null && (result.contains("<textarea id=\"chordproContent\"") ||
                        result.contains("<h1 class=\"titleLeft\""))) {
                    // Using Chordie
                    fixChordieContent(result);
                    setFileNameAndFolder();

                } else if (result != null && (result.contains("<div class=\"tb_ct\">") || result.contains("ultimate-guitar"))) {
                    // Using UG
                    fixUGContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("http://worship-songs-resources.worshiptogether.com/")) {
                    // Using WorshipTogether
                    fixWTContent(result);
                    setFileNameAndFolder();

                } else if (result !=null && result.contains("UkuTabs")) {
                    // Using UkuTabs.com
                    fixUkutabsContent(result);
                    setFileNameAndFolder();

                } else if (result!=null && result.contains("CCLI")) {
                    // Using SongSelect chord page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("<div id=\"LyricsText\"")) {
                    // Using SongSelect USR page
                    webresults_WebView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                } else if (result!=null && result.contains("holychords.com")) {
                    // Using Holychords page
                    fixHolyChordsContent(result);
                    setFileNameAndFolder();

                } else {
                    StaticVariables.myToastMessage = requireActivity().getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
                if (getActivity()!=null) {
                    StaticVariables.myToastMessage = getActivity().getResources().getText(R.string.chordpro_false).toString();
                    ShowToast.showToast(getActivity());
                    grabSongData_ProgressBar.setVisibility(View.INVISIBLE);
                }
            }*/
/*
        }
    }
*/
    private void grabchordpro(String content) {
        // Need to run a async task to grab html text


       /* // If we are in songselect, trigger the download to keep the stats live
        if (StaticVariables.whattodo.equals("songselect")) {
            try {
                // Trigger the download of the pdf
                webresults_WebView.loadUrl("javascript:document.getElementById('chordSheetDownloadButton').click()");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
