package com.garethevans.church.opensongtablet.customslides;

import android.content.Context;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class CustomSlide {

    // This object holds and deals with any custom slide objects

    private String xml;                 // Saved as a fake song
    private String type;                // The type of file (translation version) as a location
    private String folder;              // The folder for saving based on type
    private String file;                // A file safe version of the title
    private String title, lyrics, key;  // Hopefully obvious as the title, content and key
    private String author;              // For scripture:  The translation
    private String user1;               // For slideshow: The duration of the slide
    private String user2;               // For slideshow: A boolean if slides loop
    private String user3;               // For slideshow: Links to background images
    private String aka;                 //
    private String hymn_num;            //
    private String key_line;            //


    public void buildCustomSlide(Context c, MainActivityInterface mainActivityInterface, ArrayList<String> customSlide) {
        resetCustomSlide();
        if (customSlide!=null && customSlide.size()>2) {
            title = customSlide.get(1);
            lyrics = customSlide.get(2);
            file = mainActivityInterface.getStorageAccess().safeFilename(title);

            switch (customSlide.get(0)) {
                case "scripture":
                    author = customSlide.get(3);
                    folder = "Scripture";
                    type = c.getString(R.string.scripture);
                    // Add the translation to the filename
                    file = file+" "+mainActivityInterface.getStorageAccess().safeFilename(author);
                    break;
                case "note":
                    folder = "Notes";
                    type = c.getResources().getString(R.string.note);
                    break;
                case "slide":
                    user1 = customSlide.get(3);
                    user2 = customSlide.get(4);
                    user3 = customSlide.get(5);
                    folder = "Slides";
                    type = c.getResources().getString(R.string.slide);
                    break;
                case "image":
                    user1 = customSlide.get(3);
                    user2 = customSlide.get(4);
                    user3 = customSlide.get(5);
                    folder = "Images";
                    type = c.getResources().getString(R.string.image);
                    break;
            }
        }
    }

    private void resetCustomSlide() {
        xml = "";
        title = "";
        author = "";
        key = "";
        lyrics = "";
        user1 = "";
        user2 = "";
        user3 = "";
        aka = "";
        key_line = "";
        hymn_num = "";
        folder = "";
        type = "";
        file = "";
    }

    public void addItemToSet(Context c, MainActivityInterface mainActivityInterface, boolean reusable) {
        if (file != null && !file.isEmpty() && folder != null && !folder.isEmpty()) {
            // Prepare the custom slide so it can be viewed in the app
            // When exporting/saving the set, the contents get grabbed from this

            // If slide content is empty - put the title in
            if (lyrics.isEmpty()) {
                lyrics = title;
            }

            xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            xml += "<song>\n";
            xml += "  <title>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(title) + "</title>\n";
            xml += "  <author>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(author) + "</author>\n";
            xml += "  <key>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(key) + "</key>\n";
            xml += "  <user1>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user1) + "</user1>\n";
            xml += "  <user2>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user2) + "</user2>\n";
            xml += "  <user3>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(user3) + "</user3>\n";
            xml += "  <aka>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(aka) + "</aka>\n";
            xml += "  <key_line>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(key_line) + "</key_line>\n";
            xml += "  <hymn_number>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(hymn_num) + "</hymn_number>\n";
            xml += "  <lyrics>" + mainActivityInterface.getProcessSong().parseToHTMLEntities(lyrics) + "</lyrics>\n";
            xml += "</song>";

            // Make sure any & are encoded properly - first reset any currently encoded, then put back
            xml = xml.replace("&amp;", "&");
            xml = xml.replace("&", "&amp;");

            // Now prepare to save the file
            // If it is flagged to be reusable, it also gets saved in the top level folder
            // All custom slides get saved into the temp _cache folder for use with this set
            mainActivityInterface.getStorageAccess().doStringWriteToFile(c, mainActivityInterface, folder, "_cache", file, xml);

            if (reusable) {
                mainActivityInterface.getStorageAccess().doStringWriteToFile(c, mainActivityInterface, folder, "", file, xml);
            }

            // Add to set $**_**{customsfolder}/filename_***key***_**$
            String songforsetwork = "$**_**" + type + "/" + file + "_***" + key + "***__**$";
            mainActivityInterface.getCurrentSet().addSetItem(songforsetwork);
            mainActivityInterface.getCurrentSet().addSetValues("**" + folder, file, key);

            // Update the set menu
            //mainActivityInterface.updateSetList();
            mainActivityInterface.refreshSetList();


        } else {
            // Incorrect folder/filename
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.error));
        }
    }
}