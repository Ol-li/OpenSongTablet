package com.garethevans.church.opensongtablet;

import android.content.Context;
import androidx.fragment.app.DialogFragment;

class OpenFragment {

    public static DialogFragment openFragment(Context c) {

        DialogFragment newFragment = null;
        String message;

        switch (FullscreenActivity.whattodo) {
            case "loadset":
            case "saveset":
            case "deleteset":
            case "exportset":
            case "managesets":
                newFragment = PopUpListSetsFragment.newInstance();
                break;

            case "bluetoothmidi":
                newFragment = PopUpBluetoothMidiFragment.newInstance();
                break;

            case "midicommands":
                newFragment = PopUpBuildMidiMessageFragment.newInstance();
                break;

            case "usbmidi":
                newFragment = PopUpUSBMidiFragment.newInstance();
                break;

            case "showmidicommands":
                newFragment = PopUpShowMidiMessageFragment.newInstance();
                break;

            case "customise_exportsong":
            case "customise_exportset":
                newFragment = PopUpExportFragment.newInstance();
                break;

            case "resetcolours":
                message = c.getResources().getString(R.string.reset_colours);
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "clearset":
                message = c.getResources().getString(R.string.options_clearthisset);
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "deletesong":
                message = c.getResources().getString(R.string.delete) +
                        " \"" + StaticVariables.songfilename + "\"?";
                newFragment = PopUpAreYouSureFragment.newInstance(message);
                break;

            case "randomsong":
                newFragment = PopUpRandomSongFragment.newInstance();
                break;

            case "customcreate":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "scrollsettings":
                newFragment = PopUpScrollSettingsFragment.newInstance();
                break;

            case "swipesettings":
                newFragment = PopUpSwipeSettingsFragment.newInstance();
                break;

            case "localbible":
                newFragment = PopUpBibleXMLFragment.newInstance();
                break;

            case "customreusable_scripture":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "customreusable_slide":
                newFragment = PopUpCustomSlideFragment.newInstance();
                break;

            case "songdetails":
                newFragment = PopUpSongDetailsFragment.newInstance();
                break;

            case "alert":
                newFragment = PopUpAlertFragment.newInstance();
                break;

            case "presenter_audio":
                newFragment = PopUpMediaStoreFragment.newInstance();
                break;

            case "presentationorder":
                newFragment = PopUpPresentationOrderFragment.newInstance();
                break;

            case "presenter_db":
                newFragment = PopUpSoundLevelMeterFragment.newInstance();
                break;

            case "editset":
            case "setitemvariation":
                newFragment = PopUpSetViewNew.newInstance();
                break;

            case "editsong":
            case "editsongpdf":
                newFragment = PopUpEditSongFragment.newInstance();
                break;

            case "editnotes":
                newFragment = PopUpEditStickyFragment.newInstance();
                break;

            case "abcnotation":
            case "abcnotation_edit":
            case "abcnotation_editsong":
                newFragment = PopUpABCNotationFragment.newInstance();
                break;

            case "ccli_church":
            case "ccli_licence":
            case "ccli_view":
            case "ccli_reset":
                newFragment = PopUpCCLIFragment.newInstance();
                break;

            case "connect_name":
                newFragment = PopUpConnectFragment.newInstance();
                break;

            case "renamesong":
            case "duplicate":
                newFragment = PopUpSongRenameFragment.newInstance();
                break;

            case "createsong":
                newFragment = PopUpSongCreateFragment.newInstance();
                break;

            case "transpose":
                newFragment = PopUpTransposeFragment.newInstance();
                break;

            case "chordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            case "changetheme":
                newFragment = PopUpThemeChooserFragment.newInstance();
                break;

            case "autoscale":
                newFragment = PopUpScalingFragment.newInstance();
                break;

            case "changefonts":
                newFragment = PopUpFontsFragment.newInstance();
                break;

            case "connecteddisplay":
                newFragment = PopUpLayoutFragment.newInstance();
                break;

            case "pagebuttons":
                newFragment = PopUpPageButtonsFragment.newInstance();
                break;

            case "groupedpagebuttons":
                newFragment = PopUpGroupedPageButtonsFragment.newInstance();
                break;

            case "popupsettings":
                newFragment = PopUpDefaultsFragment.newInstance();
                break;

            case "extra":
                newFragment = PopUpExtraInfoFragment.newInstance();
                break;

            case "actionbarinfo":
                newFragment = PopUpActionBarInfoFragment.newInstance();
                break;

            case "footpedal":
                newFragment = PopUpPedalsFragment.newInstance();
                break;

            case "quicklaunch":
                newFragment = PopUpQuickLaunchSetup.newInstance();
                break;

            case "gestures":
                newFragment = PopUpGesturesFragment.newInstance();
                break;

            case "menuoptions":
                newFragment = PopUpMenuSettingsFragment.newInstance();
                break;

            case "newfolder":
                message = "create";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "editfoldername":
                message = "rename";
                newFragment = PopUpSongFolderRenameFragment.newInstance(message);
                break;

            case "exportsonglist":
                newFragment = PopUpExportSongListFragment.newInstance();
                break;

            case "filechooser":
                /*newFragment = PopUpDirectoryChooserFragment.newInstance();
                Bundle args2 = new Bundle();
                args2.putString("type", "file");
                newFragment.setArguments(args2);*/
                break;

            case "choosefile": //For connected display backgrounds
                newFragment = PopUpFileChooseFragment.newInstance();
                break;

            case "errorlog":
            case "browsefonts":
                newFragment = PopUpWebViewFragment.newInstance();
                break;

            case "crossfade":
                newFragment = PopUpCrossFadeFragment.newInstance();
                break;

            case "autoscrolldefaults":
                newFragment = PopUpAutoScrollDefaultsFragment.newInstance();
                break;

            case "language":
                newFragment = PopUpLanguageFragment.newInstance();
                break;

            case "fullsearch":
                newFragment = PopUpFullSearchFragment.newInstance();
                break;

            case "choosefolder":
                newFragment = PopUpChooseFolderFragment.newInstance();
                break;

            case "choosechordformat":
                newFragment = PopUpChordFormatFragment.newInstance();
                break;

            case "promptbackup":
                newFragment = PopUpBackupPromptFragment.newInstance();
                break;

            case "processimportosb":
            case "exportosb":
                newFragment = PopUpImportExportOSBFragment.newInstance();
                break;

            case "importos":
            case "doimport":
            case "doimportset":
                newFragment = PopUpImportExternalFile.newInstance();
                break;

            case "custompads":
                newFragment = PopUpCustomPadsFragment.newInstance();
                break;

            case "page_pad":
                newFragment = PopUpPadFragment.newInstance();
                break;

            case "page_autoscroll":
                newFragment = PopUpAutoscrollFragment.newInstance();
                break;

            case "page_metronome":
                newFragment = PopUpMetronomeFragment.newInstance();
                break;

            case "page_chords":
                newFragment = PopUpChordsFragment.newInstance();
                break;

            case "customchords":
                newFragment = PopUpCustomChordsFragment.newInstance();
                break;

            case "page_links":
                newFragment = PopUpLinks.newInstance();
                break;

            case "page_sticky":
                newFragment = PopUpStickyFragment.newInstance();
                break;

            case "drawnotes":
                newFragment = PopUpCreateDrawingFragment.newInstance();
                break;

            case "page_pageselect":
                newFragment = PopUpPagesFragment.newInstance();
                break;

            case "songlongpress":
                newFragment = PopUpLongSongPressFragment.newInstance();
                break;

            case "chordie":
            case "ultimate-guitar":
            case "worshipready":
            case "songselect":
            case "worshiptogether":
            case "ukutabs":
            case "holychords":
                newFragment = PopUpFindNewSongsFragment.newInstance();
                break;

            case "savecameraimage":
                newFragment = PopUpSongCreateFragment.newInstance();
                break;

        }
        return newFragment;
    }

    static String getMessage(Context c) {
        String message = "dialog";

        switch (FullscreenActivity.whattodo) {
            case "clearset":
                message = c.getResources().getString(R.string.options_clearthisset);
                break;

            case "deletesong":
                message = c.getResources().getString(R.string.delete) +
                        " \"" + StaticVariables.songfilename + "\"?";
                break;

            case "newfolder":
                message = "create";
                break;

            case "editfoldername":
                message = "rename";
                break;

        }
        return message;
    }
}