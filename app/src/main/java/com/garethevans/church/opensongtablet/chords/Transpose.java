package com.garethevans.church.opensongtablet.chords;

import android.content.Context;
import android.util.Log;

import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.ArrayList;

public class Transpose {

    //  A  A#/Bb  B/Cb  C/B#  C#/Db    D    D#/Eb   E/Fb   E#/F   F#/Gb   G     G#/Ab
    //  A    B     H     C
    //  1    2     3     4      5      6      7      8      9      10     11     12

    // Sharp chords first
    private final String[] chordsharpsnumsa   = {"$.2.$",   "$.4.$",   "$.5.$",   "$.7.$",   "$.9.$",   "$.10.$",  "$.12.$"};
    private final String[] chordsharpsnumsb   = {"$.32.$",  "$.34.$",  "$.35.$",  "$.37.$",  "$.39.$",  "$.40.$",  "$.42.$"};
    private final String[] chordsharpsnumsc   = {"$.52.$",  "$.54.$",  "$.55.$",  "$.57.$",  "$.59.$",  "$.60.$",  "$.62.$"};
    private final String[] sharpchords1a      = {"A#",      "B#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    private final String[] sharpchords1b      = {"A#m",     "B#m",     "C#m",     "D#m",     "E#m",     "F#m",     "G#m"};      // For key only
    private final String[] sharpchords2       = {"A#",      "H#",      "C#",      "D#",      "E#",      "F#",      "G#"};
    private final String[] sharpchords3a      = {"Ais",     "His",     "Cis",     "Dis",     "Eis",     "Fis",     "Gis"};
    private final String[] sharpchords3b      = {" ais",    " his",    " cis",    " dis",    " eis",    " fis",    " gis"};
    private final String[] sharpchords3c      = {".ais",    ".his",    ".cis",    ".dis",    ".eis",    ".fis",    ".gis"};
    private final String[] sharpchords4       = {"La#",     "Si#",     "Do#",     "Ré#",     "Mi#",     "Fa#",     "Sol#"};

    private final String[] properchordsharpsnumsa   = {"$.2.$",   "$.5.$",   "$.7.$",   "$.10.$",  "$.12.$"};  // For number to chord
    private final String[] properchordsharpsnumsb   = {"$.32.$",  "$.35.$",  "$.37.$",  "$.40.$",  "$.42.$"};  // For number to chord
    private final String[] properchordsharpsnumsc   = {"$.52.$",  "$.55.$",  "$.57.$",  "$.60.$",  "$.62.$"};  // For number to chord
    private final String[] propersharpchords1a      = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    private final String[] propersharpchords1b      = {"A#m",     "C#m",     "D#m",     "F#m",     "G#m"};     // For number to chord
    private final String[] propersharpchords2       = {"A#",      "C#",      "D#",      "F#",      "G#"};      // For number to chord
    private final String[] propersharpchords3a      = {"Ais",     "Cis",     "Dis",     "Fis",     "Gis"};     // For number to chord
    private final String[] propersharpchords3b      = {" ais",    " cis",    " dis",    " fis",    " gis"};    // For number to chord
    private final String[] propersharpchords3c      = {".ais",    ".cis",    ".dis",    ".fis",    ".gis"};    // For number to chord
    private final String[] propersharpchords4       = {"La#",     "Do#",     "Ré#",     "Fa#",     "Sol#"};    // For number to chord

    // Flat chords next
    private final String[] chordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.3.$",   "$.5.$",   "$.7.$",   "$.8.$",   "$.10.$"};
    private final String[] chordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.33.$",  "$.35.$",  "$.37.$",  "$.38.$",  "$.40.$"};
    private final String[] chordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.53.$",  "$.55.$",  "$.57.$",  "$.58.$",  "$.60.$"};
    private final String[] flatchords1a       = {"Ab",      "Bb",      "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    private final String[] flatchords1b       = {"Abm",     "Bbm",     "Cbm",     "Dbm",     "Ebm",     "Fbm",     "Gbm"};      // For key only
    private final String[] flatchords2        = {"Ab",      "B",       "Cb",      "Db",      "Eb",      "Fb",      "Gb"};
    private final String[] flatchords3a       = {"As",      "B",       "Ces",     "Des",     "Es",      "Fes",     "Ges"};
    private final String[] flatchords3b       = {" as",     " b",      " ces",    " des",    " es",     " fes",    " ges"};
    private final String[] flatchords3c       = {".as",     ".b",      ".ces",    ".des",    ".es",     ".fes",    ".ges"};
    private final String[] flatchords4        = {"Lab",     "Sib",     "Dob",     "Réb",     "Mib",     "Fab",     "Solb"};

    private final String[] properchordflatsnumsa    = {"$.12.$",  "$.2.$",   "$.5.$",   "$.7.$",   "$.10.$"};// For number to chord
    private final String[] properchordflatsnumsb    = {"$.42.$",  "$.32.$",  "$.35.$",  "$.37.$",  "$.40.$"};// For number to chord
    private final String[] properchordflatsnumsc    = {"$.62.$",  "$.52.$",  "$.55.$",  "$.57.$",  "$.60.$"};// For number to chord
    private final String[] properflatchords1a       = {"Ab",      "Bb",      "Db",      "Eb",      "Gb"};    // For number to chord
    private final String[] properflatchords2        = {"Ab",      "B",       "Db",      "Eb",      "Gb"};    // For number to chord
    private final String[] properflatchords3a       = {"As",      "B",       "Des",     "Es",      "Ges"};   // For number to chord
    private final String[] properflatchords3b       = {" as",     " b",      " des",    " es",     " ges"};  // For number to chord
    private final String[] properflatchords3c       = {".as",     ".b",      ".des",    ".es",     ".ges"};  // For number to chord
    private final String[] properflatchords4        = {"Lab",     "Sib",     "Réb",     "Mib",     "Solb"};  // For number to chord

    // Finally the natural chords
    private final String[] chordnaturalnumsa  = {"$.1.$",   "$.3.$",   "$.4.$",   "$.6.$",   "$.8.$",   "$.9.$",   "$.11.$"};
    private final String[] chordnaturalnumsb  = {"$.31.$",  "$.33.$",  "$.34.$",  "$.36.$",  "$.38.$",  "$.39.$",  "$.41.$"};
    private final String[] chordnaturalnumsc  = {"$.51.$",  "$.53.$",  "$.54.$",  "$.56.$",  "$.58.$",  "$.59.$",  "$.61.$"};
    private final String[] naturalchords1a    = {"A",       "B",       "C",       "D",       "E",       "F",       "G"};
    private final String[] naturalchords1b    = {"Am",      "Bm",      "Cm",      "Dm",      "Em",      "Fm",      "Gm"};       // For key only
    private final String[] naturalchords2     = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    private final String[] naturalchords3a    = {"A",       "H",       "C",       "D",       "E",       "F",       "G"};
    private final String[] naturalchords3b    = {" a",      " h",      " c",      " d",      " e",      " f",      " g"};
    private final String[] naturalchords3c    = {".a",      ".h",      ".c",      ".d",      ".e",      ".f",      ".g"};
    private final String[] naturalchords4     = {"La",      "Si",      "Do",      "Ré",      "Mi",      "Fa",      "Sol"};

    private String originalkey;
    private String transposedLyrics, transposedChords;
    private int oldchordformat;

    private boolean usesflats;
    private boolean capousesflats;


    // The song is sent in and the song is sent back after processing (key and lyrics get changed)
    Song doTranspose(Context c, Song song, Preferences preferences, String transposeDirection,
                     int transposeTimes, boolean forcesharps, boolean forceflats) {

        try {
            // Go through each line and change each chord to $..$
            // This marks the bit to be changed
            String[] splitLyrics = song.getLyrics().split("\n");
            
            if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",true)) {
                oldchordformat = preferences.getMyPreferenceInt(c,"chordFormat",1);
            } else {
                oldchordformat = StaticVariables.detectedChordFormat;
            }
            
            // Transpose the key
            song.setKey(sortTheTransposedKey(c,song.getKey(),preferences,transposeDirection,transposeTimes));

            // Now we change the chords into numbers
            for (int x = 0; x < splitLyrics.length; x++) {
                if (splitLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    switch (oldchordformat) {
                        default:
                            splitLyrics[x] = chordToNumber1(splitLyrics[x]);
                            break;

                        case 2:
                            splitLyrics[x] = chordToNumber2(splitLyrics[x]);
                            break;

                        case 3:
                            splitLyrics[x] = chordToNumber3(splitLyrics[x]);
                            break;

                        case 4:
                            splitLyrics[x] = chordToNumber4(splitLyrics[x]);
                            break;
                    }
                }
            }

            // Next up we do the transposing

            splitLyrics = transposeChords(splitLyrics);

            StringBuilder sb = new StringBuilder();
            // Now we put the numbers back into chords in the correct format and using either the key preference or the forced sharps or flats
            for (int x = 0; x < splitLyrics.length; x++) {
                if (splitLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    switch (oldchordformat) {
                        default:
                            splitLyrics[x] = numberToChord1(splitLyrics[x],forceflats,forcesharps,usesflats);
                            break;

                        case 2:
                            splitLyrics[x] = numberToChord2(splitLyrics[x],forceflats,forcesharps,usesflats);
                            break;

                        case 3:
                            splitLyrics[x] = numberToChord3(splitLyrics[x],forceflats,forcesharps,usesflats);
                            break;

                        case 4:
                            splitLyrics[x] = numberToChord4(splitLyrics[x],forceflats,forcesharps,usesflats);
                            break;
                    }
                }

                // Add all the lines back up as a string
                sb.append(splitLyrics[x]).append("\n");
            }

            song.setLyrics(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return song;
    }

    private String sortTheTransposedKey(Context c, String key, Preferences preferences,
                                        String transposeDirection, int transposeTimes) {
        if (key != null && !key.isEmpty()) {
            String newkey = keyToNumber(key);

            // Transpose the key
            newkey = transposeKey(newkey, transposeDirection, transposeTimes);

            // Convert the keynumber to a key
            newkey = numberToKey(c, preferences, newkey);

            // Decide if flats should be used
            usesflats = keyUsesFlats(c, preferences, newkey);

            key = newkey;
        }
        return key;
    }
    
    String transposeThisString(Context c, Preferences preferences, String transposeDirection, String texttotranspose) {
        try {
            // Go through each line and change each chord to $..$
            // This marks the bit to be changed

            String[] splitLyrics = texttotranspose.split("\n");

            int transposeTimes = 1;

            oldchordformat = StaticVariables.detectedChordFormat;

            // Now we change the chords into numbers
            for (int x = 0; x < splitLyrics.length; x++) {
                if (splitLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    switch (oldchordformat) {
                        default:
                            splitLyrics[x] = chordToNumber1(splitLyrics[x]);
                            break;

                        case 2:
                            splitLyrics[x] = chordToNumber2(splitLyrics[x]);
                            break;

                        case 3:
                            splitLyrics[x] = chordToNumber3(splitLyrics[x]);
                            break;

                        case 4:
                            splitLyrics[x] = chordToNumber4(splitLyrics[x]);
                            break;

                        case 5:
                            boolean numeral = true;
                            splitLyrics[x] = fromNashville(splitLyrics[x], numeral);
                            break;

                    }
                }
            }

            // Next up we do the transposing

            transposeChords(splitLyrics);

            StringBuilder sb = new StringBuilder();
            // Now we put the numbers back into chords in the correct format and using either the key preference or the forced sharps or flats
            for (int x = 0; x < splitLyrics.length; x++) {
                if (splitLyrics[x].indexOf(".") == 0) {
                    // Since this line has chords, do the changing!
                    // Decide on the chord format to use
                    if (preferences.getMyPreferenceBoolean(c,"chordFormatUsePreferred",false)) {
                        // User has specified using their preferred chord format every time
                        oldchordformat = preferences.getMyPreferenceInt(c,"chordFormat",1);
                        // This is only true when the user clicks the option in the menu, so reset
                    }

                    switch (oldchordformat) {
                        default:
                            splitLyrics[x] = numberToChord1(splitLyrics[x],false,false,usesflats);
                            break;

                        case 2:
                            splitLyrics[x] = numberToChord2(splitLyrics[x],false,false,usesflats);
                            break;

                        case 3:
                            splitLyrics[x] = numberToChord3(splitLyrics[x],false,false,usesflats);
                            break;

                        case 4:
                            splitLyrics[x] = numberToChord4(splitLyrics[x],false,false,usesflats);
                            break;

                        case 5:
                            boolean numeral = true;
                            splitLyrics[x] = toNashville(splitLyrics[x],numeral);
                    }
                }

                // Add all the lines back up as a string
                sb.append(splitLyrics[x]).append("\n");
            }

            // Now that the chords have been changed, return the lyrics
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return texttotranspose;
        }
    }

    String keyToNumber(String key) {
        // Swap the key with the correct number

        // Look for minor keys first
        for (int z=0;z<sharpchords1b.length;z++) {
            key = key.replace(sharpchords1b[z],chordsharpsnumsb[z]);
        }
        for (int z=0;z<flatchords1b.length;z++) {
            key = key.replace(flatchords1b[z],chordflatsnumsb[z]);
        }
        for (int z=0;z<naturalchords1b.length;z++) {
            key = key.replace(naturalchords1b[z],chordnaturalnumsb[z]);
        }

        // Look for major keys next
        for (int z=0;z<sharpchords1a.length;z++) {
            key = key.replace(sharpchords1a[z],chordsharpsnumsa[z]);
        }
        for (int z=0;z<flatchords1a.length;z++) {
            key = key.replace(flatchords1a[z],chordflatsnumsa[z]);
        }
        for (int z=0;z<naturalchords1a.length;z++) {
            key = key.replace(naturalchords1a[z],chordnaturalnumsa[z]);
        }

        return key;
    }

    private String chordToNumber1(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords1a.length;z++) {
            line = line.replace(sharpchords1a[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords1a.length;z++) {
            line = line.replace(flatchords1a[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords1a.length;z++) {
            line = line.replace(naturalchords1a[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private String chordToNumber2(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords2.length;z++) {
            line = line.replace(sharpchords2[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords1a.length;z++) {
            line = line.replace(flatchords2[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords1a.length;z++) {
            line = line.replace(naturalchords2[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private String chordToNumber3(String line) {
        // Look for sharps first
        for (int z=0;z<sharpchords3c.length;z++) {
            line = line.replace(sharpchords3c[z],chordsharpsnumsc[z]);
        }
        for (int z=0;z<sharpchords3b.length;z++) {
            line = line.replace(sharpchords3b[z],chordsharpsnumsb[z]);
        }
        for (int z=0;z<sharpchords3a.length;z++) {
            line = line.replace(sharpchords3a[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords3c.length;z++) {
            line = line.replace(flatchords3c[z],chordflatsnumsc[z]);
        }
        for (int z=0;z<flatchords3b.length;z++) {
            line = line.replace(flatchords3b[z],chordflatsnumsb[z]);
        }
        for (int z=0;z<flatchords3a.length;z++) {
            line = line.replace(flatchords3a[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords3c.length;z++) {
            line = line.replace(naturalchords3c[z],chordnaturalnumsc[z]);
        }
        for (int z=0;z<naturalchords3b.length;z++) {
            line = line.replace(naturalchords3b[z],chordnaturalnumsb[z]);
        }
        for (int z=0;z<naturalchords3a.length;z++) {
            line = line.replace(naturalchords3a[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    private String chordToNumber4(String line) {

        // Change any Re into Ré and Ti into Si
        line = line.replace("Re","Ré");
        line = line.replace("Ti","Si");

        // Change lowercase into correct case
        line = line.replace("do","Do");
        line = line.replace("re","ré");
        line = line.replace("ré","Ré");
        line = line.replace("mi","Mi");
        line = line.replace("fa","Fa");
        line = line.replace("sol","Sol");
        line = line.replace("la","La");
        line = line.replace("si","Si");

        // Look for sharps first
        for (int z=0;z<sharpchords4.length;z++) {
            line = line.replace(sharpchords4[z],chordsharpsnumsa[z]);
        }

        // Now flats
        for (int z=0;z<flatchords4.length;z++) {
            line = line.replace(flatchords4[z],chordflatsnumsa[z]);
        }

        // Finally naturals
        for (int z=0;z<naturalchords4.length;z++) {
            line = line.replace(naturalchords4[z],chordnaturalnumsa[z]);
        }
        return line;
    }

    Song convertChords(Context c, Song song, StorageAccess storageAccess, Preferences preferences, String transposeDirection, int transposeTimes) {
        int convertTo = preferences.getMyPreferenceInt(c,"chordFormat",1);
        checkChordFormat(c,song.getLyrics(),preferences);

        Log.d("Transpose","convertTo="+convertTo+"\ndetetctedChordFormat="+StaticVariables.detectedChordFormat);
        if (StaticVariables.detectedChordFormat >= 5) {
            song.setLyrics(convertFromNumerals(c, song, storageAccess, preferences));
        }// Convert to a normal format to start with
        if (convertTo>=5) {
            // We want to convert to a numeral.  If it is normal format, just do it, otherwise, convert to a normal format
            // Now convert to the correct value
            song.setLyrics(convertToNumerals(c, song.getLyrics(), preferences));
        } else {
            transposeDirection = "0";
            checkChordFormat(c, song.getLyrics(), preferences);
            try {
                song = doTranspose(c, song, preferences, transposeDirection, transposeTimes,false, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return song;
    }

    private String convertToNumerals(Context c, String lyrics, Preferences preferences) {
        boolean numeral = false;
        if (preferences.getMyPreferenceInt(c,"chordFormat",1)==6) {
            numeral = true;
        }
        String[] splitLyrics = lyrics.split("\n");

        StringBuilder sb = new StringBuilder();
        // Convert these into standard chord format to start with
        for (String line:splitLyrics) {
            if (line.startsWith(".")) {
                // Chord line, so sort it
                Log.d("Transpose","old line="+line);
                line = toNashville(line,numeral);
                Log.d("Transpose","new line="+line);
            }
            // Add it back up
            sb.append(line).append("\n");
        }

        return sb.toString();

    }

    private String convertFromNumerals(Context c, Song song, StorageAccess storageAccess, Preferences preferences) {
        // This goes through the song and converts from Nashville numbering or numerals to standard chord format first
        String lyrics = song.getLyrics();
        if (StaticVariables.detectedChordFormat==5 || StaticVariables.detectedChordFormat==6) {
            // We currently have either a nashville system (numbers or numerals)
            String[] splitLyrics = song.getLyrics().split("\n");

            boolean numeral = StaticVariables.detectedChordFormat==6;
            StringBuilder sb = new StringBuilder();
            Log.d("Transpose","Converting from numerals first");
            // Convert these into standard chord format to start with
            for (String line:splitLyrics) {
                if (line.startsWith(".")) {
                    // Chord line, so sort it
                    Log.d("Transpose","old line="+line);
                    line = fromNashville(line,numeral);
                    Log.d("Transpose","new line="+line);

                }
                // Add it back up
                sb.append(line).append("\n");
            }

            lyrics = sb.toString();
            // If the new chordformat desired is also a numeral or number system, convert it to that
            if (preferences.getMyPreferenceInt(c,"chordFormat",1)==5 ||
                    preferences.getMyPreferenceInt(c, "chordFormat",1)==6) {
                song.setLyrics(convertToNumerals(c, song.getLyrics(), preferences));
            }
        } else {
            ShowToast.showToast(c,"No Nashville/Numeral chord format detected.");
        }
        return lyrics;
    }

    private String toNashville(String line, boolean numeral) {
        //  This compares the chords with the song key

        Log.d("Transpose","Converting to nashville");

        // A  A#/Bb  B/H  C  C#/Db  D  D#/Eb  E  F  F#/Gb  G  G#/Ab
        // 0    1     2   3    4    5    6    7  8    9    10   11

        String[] bitssharp;
        String[] bitsflat;
        switch(oldchordformat) {
            case 1:
            default:
                bitssharp = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 2:
            case 3:
                bitssharp = "A B H C C# D D# E F F# G G# A B H C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A B H C Db D Eb E F Gb G Ab A B H C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 4:
                bitssharp = "La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol# La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol#".split(" ");
                bitsflat =  "La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab".split(" ");
                break;
        }

        int root = 0;
        boolean stilllooking = true;
        for (int i=0;i<bitssharp.length;i++) {
            if (stilllooking && (bitssharp[i].equals(originalkey) || (bitssharp[i]+"m").equals(originalkey) ||
                    bitsflat[i].equals(originalkey) || (bitsflat[i]+"m").equals(originalkey))) {
                root = i;
                stilllooking = false;
            }
        }

        boolean ismajor = true;
        if (originalkey.contains("m")) {
            ismajor = false;
        }
        int num2=root+2, num3, num4=root+5, num5=root+7, num6, num7;
        if (ismajor) {
            num3 = root+4;
            num6 = root+9;
            num7 = root+11;
        } else {
            num3 = root+3;
            num6 = root+8;
            num7 = root+10;
        }

        String nash1_sharp = bitssharp[root];
        String nash1s_sharp = bitssharp[root+1];
        String nash2_sharp = bitssharp[num2];
        String nash2s_sharp = bitssharp[num2+1];
        String nash3_sharp = bitssharp[num3];
        String nash4_sharp = bitssharp[num4];
        String nash4s_sharp = bitssharp[num4+1];
        String nash5_sharp = bitssharp[num5];
        String nash5s_sharp = bitssharp[num5+1];
        String nash6_sharp = bitssharp[num6];
        String nash6s_sharp = bitssharp[num6+1];
        String nash7_sharp = bitssharp[num7];
        String nash1_flat = bitsflat[root];
        String nash2_flat = bitsflat[num2];
        String nash2b_flat = bitsflat[num2-1];
        String nash3_flat = bitsflat[num3];
        String nash3b_flat = bitsflat[num3-1];
        String nash4_flat = bitsflat[num4];
        String nash5_flat = bitsflat[num5];
        String nash5b_flat = bitsflat[num5-1];
        String nash6_flat = bitsflat[num6];
        String nash6b_flat = bitsflat[num6-1];
        String nash7_flat = bitsflat[num7];
        String nash7b_flat = bitsflat[num7-1];

        if (numeral) {
            line = fixDiminished(line);
            line = fixAugmented(line);
            line = fixMajorSeventh(line);

            String[] conditions = {"sharp","flat","natural"};
            for (String condition: conditions) {
                line = replaceBits(line,nash1_sharp,"I",condition);
                line = replaceBits(line,nash2_sharp,"II",condition);
                line = replaceBits(line,nash3_sharp,"III",condition);
                line = replaceBits(line,nash4_sharp,"IV",condition);
                line = replaceBits(line,nash5_sharp,"V",condition);
                line = replaceBits(line,nash6_sharp,"VI",condition);
                line = replaceBits(line,nash7_sharp,"VII",condition);
                line = replaceBits(line,nash1s_sharp,"#I",condition);
                line = replaceBits(line,nash2s_sharp,"#II",condition);
                line = replaceBits(line,nash4s_sharp,"#IV",condition);
                line = replaceBits(line,nash5s_sharp,"#V",condition);
                line = replaceBits(line,nash6s_sharp,"#VI",condition);
                line = replaceBits(line,nash1_flat,"I",condition);
                line = replaceBits(line,nash2_flat,"II",condition);
                line = replaceBits(line,nash3_flat,"III",condition);
                line = replaceBits(line,nash4_flat,"IV",condition);
                line = replaceBits(line,nash5_flat,"V",condition);
                line = replaceBits(line,nash6_flat,"VI",condition);
                line = replaceBits(line,nash7_flat,"VII",condition);
                line = replaceBits(line,nash2b_flat,"bII",condition);
                line = replaceBits(line,nash3b_flat,"bIII",condition);
                line = replaceBits(line,nash5b_flat,"bV",condition);
                line = replaceBits(line,nash6b_flat,"bVI",condition);
                line = replaceBits(line,nash7b_flat,"bVII",condition);

            }
            // Fix minor chords
            line = line.replace("IVm", "iv");
            line = line.replace("VIIm", "vii");
            line = line.replace("VIIo", "vii");
            line = line.replace("VIm", "vi");
            line = line.replace("Vm", "v");
            line = line.replace("IIIm", "iii");
            line = line.replace("IIm", "ii");
            line = line.replace("Im", "i");

        } else {
            line = line.replace(nash1_sharp, "1");
            line = line.replace(nash1s_sharp, "#1");
            line = line.replace(nash2_sharp, "2");
            line = line.replace(nash2s_sharp, "#2");
            line = line.replace(nash3_sharp, "3");
            line = line.replace(nash4_sharp, "4");
            line = line.replace(nash4s_sharp, "#4");
            line = line.replace(nash5_sharp, "5");
            line = line.replace(nash5s_sharp, "#5");
            line = line.replace(nash6_sharp, "6");
            line = line.replace(nash6s_sharp, "#6");
            line = line.replace(nash7_sharp, "7");
            line = line.replace(nash1_flat, "1");
            line = line.replace(nash2_flat, "2");
            line = line.replace(nash2b_flat, "b2");
            line = line.replace(nash3_flat, "3");
            line = line.replace(nash3b_flat, "b3");
            line = line.replace(nash4_flat, "4");
            line = line.replace(nash5_flat, "5");
            line = line.replace(nash5b_flat, "b5");
            line = line.replace(nash6_flat, "6");
            line = line.replace(nash6b_flat, "b6");
            line = line.replace(nash7_flat, "7");
            line = line.replace(nash7b_flat, "b7");
        }

        return line;
    }

    private String replaceBits(String line, String what, String with, String condition) {
        if (condition.equals("sharp") && what.contains("#")) {
            Log.d("Transpose", "replace '"+what+"' with '"+with+"'");
            return line.replace(what,with);
        } else if (condition.equals("flat") && what.contains("b")) {
            Log.d("Transpose", "Fixing "+condition+": replace '"+what+"' with '"+with+"'");return line.replace(what,with);
        } else if (condition.equals("natural") && !what.contains("#") && !what.contains("b")) {
            Log.d("Transpose", "Fixing "+condition+": replace '"+what+"' with '"+with+"'");return line.replace(what,with);
        } else {
            Log.d("Transpose", "Fixing "+condition+": not replacing '"+what+"' with '"+with+"'");
            return line;
        }
    }
    private String fromNashville(String line, boolean numeral) {
        String[] bitssharp;
        String[] bitsflat;
        switch(oldchordformat) {
            case 1:
            default:
                bitssharp = "A A# B C C# D D# E F F# G G# A A# B C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A Bb B C Db D Eb E F Gb G Ab A Bb B C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 2:
            case 3:
                bitssharp = "A B H C C# D D# E F F# G G# A B H C C# D D# E F F# G G#".split(" ");
                bitsflat  = "A B H C Db D Eb E F Gb G Ab A B H C Db D Eb E F Gb G Ab".split(" ");
                break;

            case 4:
                bitssharp = "La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol# La La# Si Do Do# Ré Ré# Mi Fa Fa# Sol Sol#".split(" ");
                bitsflat =  "La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab La Sib Si Do Réb Ré Mib Mi Fa Solb Sol Lab".split(" ");
        }

        int root = 0;
        boolean stilllooking = true;
        for (int i=0;i<bitssharp.length;i++) {
            if (stilllooking && (bitssharp[i].equals(originalkey) || (bitssharp[i]+"m").equals(originalkey) ||
                    bitsflat[i].equals(originalkey) || (bitsflat[i]+"m").equals(originalkey))) {
                root = i;
                stilllooking = false;
            }
        }

        boolean ismajor = true;
        if (originalkey.contains("m")) {
            ismajor = false;
        }
        int num2=root+2, num3, num4=root+5, num5=root+7, num6, num7;
        if (ismajor) {
            num3 = root+4;
            num6 = root+9;
            num7 = root+11;
        } else {
            num3 = root+3;
            num6 = root+8;
            num7 = root+10;
        }

        String[] bitsdefault;
        String[] conditions;
        if (originalkey.contains("b")) {
            bitsdefault = bitsflat.clone();
            conditions = new String[]{"flat","sharp","natural"};
        } else {
            bitsdefault = bitssharp.clone();
            conditions = new String[]{"sharp","flat","natural"};
        }

        if (numeral) {
            for (String condition: conditions) {

                line = replaceBits(line,"#VII",bitssharp[root],condition);
                line = replaceBits(line,"#VI",bitssharp[num6+1],condition);
                line = replaceBits(line,"#IV",bitssharp[num4+1],condition);
                line = replaceBits(line,"#V",bitssharp[num5+1],condition);
                line = replaceBits(line,"#III",bitssharp[num3+1],condition);
                line = replaceBits(line,"#II",bitssharp[num2+1],condition);
                line = replaceBits(line,"#I",bitssharp[root+1],condition);

                line = replaceBits(line,"bVII",bitsflat[num7-1],condition);
                line = replaceBits(line,"bVI",bitsflat[num6-1],condition);
                line = replaceBits(line,"bIV",bitsflat[num4-1],condition);
                line = replaceBits(line,"bV",bitsflat[num5-1],condition);
                line = replaceBits(line,"bIII",bitsflat[num3-1],condition);
                line = replaceBits(line,"bII",bitsflat[num2-1],condition);
                line = replaceBits(line,"bI",bitsflat[root+11],condition);

                line = replaceBits(line,"VII",bitsdefault[num7],condition);
                line = replaceBits(line,"VI",bitsdefault[num6],condition);
                line = replaceBits(line,"IV",bitsdefault[num4],condition);
                line = replaceBits(line,"V",bitsdefault[num5],condition);
                line = replaceBits(line,"III",bitsdefault[num3],condition);
                line = replaceBits(line,"II",bitsdefault[num2],condition);
                line = replaceBits(line,"I",bitsdefault[root],condition);

                line = replaceBits(line, "#vii", bitssharp[num7+1]+"m", condition);
                line = replaceBits(line, "#vi", bitssharp[num6+1]+"m", condition);
                line = replaceBits(line, "#iv", bitssharp[num4+1]+"m", condition);
                line = replaceBits(line, "#v", bitssharp[num5+1]+"m", condition);
                line = replaceBits(line, "#iii", bitssharp[num3+1]+"m", condition);
                line = replaceBits(line, "#ii", bitssharp[num2+1]+"m", condition);
                line = replaceBits(line, "#i", bitssharp[root+1]+"m", condition);

                line = replaceBits(line, "bvii", bitsflat[num7-1]+"m", condition);
                line = replaceBits(line, "bvi", bitsflat[num6-1]+"m", condition);
                line = replaceBits(line, "biv", bitsflat[num4-1]+"m", condition);
                line = replaceBits(line, "bv", bitsflat[num5-1]+"m", condition);
                line = replaceBits(line, "biii", bitsflat[num3-1]+"m", condition);
                line = replaceBits(line, "bii", bitsflat[num2-1]+"m", condition);
                line = replaceBits(line, "bi", bitsflat[root+11]+"m", condition);

                if (originalkey.endsWith("m")) {
                    line = replaceBits(line,"vii", bitsdefault[num7]+"m",condition);
                } else {
                    line = replaceBits(line,"vii", bitsdefault[num7]+"o",condition);
                }
                line = replaceBits(line, "vi", bitsdefault[num6]+"m", condition);
                line = replaceBits(line, "iv", bitsdefault[num4]+"m", condition);
                line = replaceBits(line, "v", bitsdefault[num5]+"m", condition);
                line = replaceBits(line, "iii", bitsdefault[num3]+"m", condition);
                if (originalkey.endsWith("m")) {
                    line = replaceBits(line, "ii", bitsdefault[num2]+"o", condition);
                } else {
                    line = replaceBits(line, "ii", bitsdefault[num2]+"m", condition);
                }
                line = replaceBits(line, "i", bitsdefault[root]+"m", condition);

            }


        } else {

            for (String condition: conditions) {

                line = replaceBits(line,".#7","."+bitssharp[root],condition);
                line = replaceBits(line," #7"," "+bitssharp[root],condition);
                line = replaceBits(line,"/#7","/"+bitssharp[root],condition);
                line = replaceBits(line,".#6","."+bitssharp[num6+1],condition);
                line = replaceBits(line," #6"," "+bitssharp[num6+1],condition);
                line = replaceBits(line,"/#6","/"+bitssharp[num6+1],condition);
                line = replaceBits(line,".#5","."+bitssharp[num5+1],condition);
                line = replaceBits(line," #5"," "+bitssharp[num5+1],condition);
                line = replaceBits(line,"/#5","/"+bitssharp[num5+1],condition);
                line = replaceBits(line,".#4","."+bitssharp[num4+1],condition);
                line = replaceBits(line," #4"," "+bitssharp[num4+1],condition);
                line = replaceBits(line,"/#4","/"+bitssharp[num4+1],condition);
                line = replaceBits(line,".#3","."+bitssharp[num3+1],condition);
                line = replaceBits(line," #3"," "+bitssharp[num3+1],condition);
                line = replaceBits(line,"/#3","/"+bitssharp[num3+1],condition);
                line = replaceBits(line,".#2","."+bitssharp[num2+1],condition);
                line = replaceBits(line," #2"," "+bitssharp[num2+1],condition);
                line = replaceBits(line,"/#2","/"+bitssharp[num2+1],condition);
                line = replaceBits(line,".#1","."+bitssharp[root+1],condition);
                line = replaceBits(line," #1"," "+bitssharp[root+1],condition);
                line = replaceBits(line," /1","/"+bitssharp[root+1],condition);

                line = replaceBits(line,".b7","."+bitssharp[num7-1],condition);
                line = replaceBits(line," b7"," "+bitssharp[num7-1],condition);
                line = replaceBits(line,"/b7","/"+bitssharp[num7-1],condition);
                line = replaceBits(line,".b6","."+bitssharp[num6-1],condition);
                line = replaceBits(line," b6"," "+bitssharp[num6-1],condition);
                line = replaceBits(line,"/b6","/"+bitssharp[num6-1],condition);
                line = replaceBits(line,".b5","."+bitssharp[num5-1],condition);
                line = replaceBits(line," b5"," "+bitssharp[num5-1],condition);
                line = replaceBits(line,"/b5","/"+bitssharp[num5-1],condition);
                line = replaceBits(line,".b4","."+bitssharp[num4-1],condition);
                line = replaceBits(line," b4"," "+bitssharp[num4-1],condition);
                line = replaceBits(line,"/b4","/"+bitssharp[num4-1],condition);
                line = replaceBits(line,".b3","."+bitssharp[num3-1],condition);
                line = replaceBits(line," b3"," "+bitssharp[num3-1],condition);
                line = replaceBits(line,"/b3","/"+bitssharp[num3-1],condition);
                line = replaceBits(line,".b2","."+bitssharp[num2-1],condition);
                line = replaceBits(line," b2"," "+bitssharp[num2-1],condition);
                line = replaceBits(line,"/b2","/"+bitssharp[num2-1],condition);
                line = replaceBits(line,".b1","."+bitssharp[root+11],condition);
                line = replaceBits(line," b1"," "+bitssharp[root+11],condition);
                line = replaceBits(line,"/b1","/"+bitssharp[root+11],condition);

                line = replaceBits(line,".7","."+bitsdefault[num7],condition);
                line = replaceBits(line," 7"," "+bitsdefault[num7],condition);
                line = replaceBits(line,"/7","/"+bitsdefault[num7],condition);
                line = replaceBits(line,".6","."+bitsdefault[num6],condition);
                line = replaceBits(line," 6"," "+bitsdefault[num6],condition);
                line = replaceBits(line,"/6","/"+bitsdefault[num6],condition);
                line = replaceBits(line,".5","."+bitsdefault[num5],condition);
                line = replaceBits(line," 5"," "+bitsdefault[num5],condition);
                line = replaceBits(line,"/5","/"+bitsdefault[num5],condition);
                line = replaceBits(line,".4","."+bitsdefault[num4],condition);
                line = replaceBits(line," 4"," "+bitsdefault[num4],condition);
                line = replaceBits(line,"/4","/"+bitsdefault[num4],condition);
                line = replaceBits(line,".3","."+bitsdefault[num3],condition);
                line = replaceBits(line," 3"," "+bitsdefault[num3],condition);
                line = replaceBits(line,"/3","/"+bitsdefault[num3],condition);
                line = replaceBits(line,".2","."+bitsdefault[num2],condition);
                line = replaceBits(line," 2"," "+bitsdefault[num2],condition);
                line = replaceBits(line,"/2","/"+bitsdefault[num2],condition);
                line = replaceBits(line,".1","."+bitsdefault[root],condition);
                line = replaceBits(line," 1"," "+bitsdefault[root],condition);
                line = replaceBits(line,"/1","/"+bitsdefault[root],condition);

            }
        }
        if (!line.startsWith(".")) {
            line = "."+line;
        }
        return line;
    }

    String transposeKey(String getkeynum, String direction, int transposetimes) {
        if (direction.equals("+1")) {
            // Put the numbers up by one.
            // Last step then fixes 13 to be 1

            // Repeat this as often as required.
            for (int repeatTranspose = 0; repeatTranspose < transposetimes; repeatTranspose++) {
                getkeynum = getkeynum.replace("$.12.$", "$.13.$");
                getkeynum = getkeynum.replace("$.11.$", "$.12.$");
                getkeynum = getkeynum.replace("$.10.$", "$.11.$");
                getkeynum = getkeynum.replace("$.9.$", "$.10.$");
                getkeynum = getkeynum.replace("$.8.$", "$.9.$");
                getkeynum = getkeynum.replace("$.7.$", "$.8.$");
                getkeynum = getkeynum.replace("$.6.$", "$.7.$");
                getkeynum = getkeynum.replace("$.5.$", "$.6.$");
                getkeynum = getkeynum.replace("$.4.$", "$.5.$");
                getkeynum = getkeynum.replace("$.3.$", "$.4.$");
                getkeynum = getkeynum.replace("$.2.$", "$.3.$");
                getkeynum = getkeynum.replace("$.1.$", "$.2.$");
                getkeynum = getkeynum.replace("$.13.$", "$.1.$");

                getkeynum = getkeynum.replace("$.42.$", "$.43.$");
                getkeynum = getkeynum.replace("$.41.$", "$.42.$");
                getkeynum = getkeynum.replace("$.40.$", "$.41.$");
                getkeynum = getkeynum.replace("$.39.$", "$.40.$");
                getkeynum = getkeynum.replace("$.38.$", "$.39.$");
                getkeynum = getkeynum.replace("$.37.$", "$.38.$");
                getkeynum = getkeynum.replace("$.36.$", "$.37.$");
                getkeynum = getkeynum.replace("$.35.$", "$.36.$");
                getkeynum = getkeynum.replace("$.34.$", "$.35.$");
                getkeynum = getkeynum.replace("$.33.$", "$.34.$");
                getkeynum = getkeynum.replace("$.32.$", "$.33.$");
                getkeynum = getkeynum.replace("$.31.$", "$.32.$");
                getkeynum = getkeynum.replace("$.43.$", "$.31.$");

                getkeynum = getkeynum.replace("$.62.$", "$.63.$");
                getkeynum = getkeynum.replace("$.61.$", "$.62.$");
                getkeynum = getkeynum.replace("$.60.$", "$.61.$");
                getkeynum = getkeynum.replace("$.59.$", "$.60.$");
                getkeynum = getkeynum.replace("$.58.$", "$.59.$");
                getkeynum = getkeynum.replace("$.57.$", "$.58.$");
                getkeynum = getkeynum.replace("$.56.$", "$.57.$");
                getkeynum = getkeynum.replace("$.55.$", "$.56.$");
                getkeynum = getkeynum.replace("$.54.$", "$.55.$");
                getkeynum = getkeynum.replace("$.53.$", "$.54.$");
                getkeynum = getkeynum.replace("$.52.$", "$.53.$");
                getkeynum = getkeynum.replace("$.51.$", "$.52.$");
                getkeynum = getkeynum.replace("$.63.$", "$.51.$");
            }


        } else if (direction.equals("-1")) {
            // Put the numbers down by one.
            // Last step then fixes 0 to be 12

            // Repeat this as often as required.
            for (int repeatTranspose = 0; repeatTranspose < transposetimes; repeatTranspose++) {
                getkeynum = getkeynum.replace("$.1.$", "$.0.$");
                getkeynum = getkeynum.replace("$.2.$", "$.1.$");
                getkeynum = getkeynum.replace("$.3.$", "$.2.$");
                getkeynum = getkeynum.replace("$.4.$", "$.3.$");
                getkeynum = getkeynum.replace("$.5.$", "$.4.$");
                getkeynum = getkeynum.replace("$.6.$", "$.5.$");
                getkeynum = getkeynum.replace("$.7.$", "$.6.$");
                getkeynum = getkeynum.replace("$.8.$", "$.7.$");
                getkeynum = getkeynum.replace("$.9.$", "$.8.$");
                getkeynum = getkeynum.replace("$.10.$", "$.9.$");
                getkeynum = getkeynum.replace("$.11.$", "$.10.$");
                getkeynum = getkeynum.replace("$.12.$", "$.11.$");
                getkeynum = getkeynum.replace("$.0.$", "$.12.$");

                getkeynum = getkeynum.replace("$.31.$", "$.30.$");
                getkeynum = getkeynum.replace("$.32.$", "$.31.$");
                getkeynum = getkeynum.replace("$.33.$", "$.32.$");
                getkeynum = getkeynum.replace("$.34.$", "$.33.$");
                getkeynum = getkeynum.replace("$.35.$", "$.34.$");
                getkeynum = getkeynum.replace("$.36.$", "$.35.$");
                getkeynum = getkeynum.replace("$.37.$", "$.36.$");
                getkeynum = getkeynum.replace("$.38.$", "$.37.$");
                getkeynum = getkeynum.replace("$.39.$", "$.38.$");
                getkeynum = getkeynum.replace("$.40.$", "$.39.$");
                getkeynum = getkeynum.replace("$.41.$", "$.40.$");
                getkeynum = getkeynum.replace("$.42.$", "$.41.$");
                getkeynum = getkeynum.replace("$.30.$", "$.42.$");

                getkeynum = getkeynum.replace("$.51.$", "$.50.$");
                getkeynum = getkeynum.replace("$.52.$", "$.51.$");
                getkeynum = getkeynum.replace("$.53.$", "$.52.$");
                getkeynum = getkeynum.replace("$.54.$", "$.53.$");
                getkeynum = getkeynum.replace("$.55.$", "$.54.$");
                getkeynum = getkeynum.replace("$.56.$", "$.55.$");
                getkeynum = getkeynum.replace("$.57.$", "$.56.$");
                getkeynum = getkeynum.replace("$.58.$", "$.57.$");
                getkeynum = getkeynum.replace("$.59.$", "$.58.$");
                getkeynum = getkeynum.replace("$.60.$", "$.59.$");
                getkeynum = getkeynum.replace("$.61.$", "$.60.$");
                getkeynum = getkeynum.replace("$.62.$", "$.61.$");
                getkeynum = getkeynum.replace("$.50.$", "$.62.$");
            }
        }
        return getkeynum;
    }

    private String[] transposeChords(String[] splitLyrics) {
        // Go through each line in turn
        for (int x = 0; x < splitLyrics.length; x++) {

            // Only do transposing if it is a chord line (starting with .)
            if (splitLyrics[x].startsWith(".")) {
                if (StaticVariables.transposeDirection.equals("+1")) {
                    // Put the numbers up by one.
                    // Last step then fixes 12 to be 0

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < StaticVariables.transposeTimes; repeatTranspose++) {
                        splitLyrics[x] = splitLyrics[x].replace("$.12.$", "$.13.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.11.$", "$.12.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.10.$", "$.11.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.9.$", "$.10.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.8.$", "$.9.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.7.$", "$.8.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.6.$", "$.7.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.5.$", "$.6.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.4.$", "$.5.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.3.$", "$.4.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.2.$", "$.3.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.1.$", "$.2.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.13.$", "$.1.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.42.$", "$.43.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.41.$", "$.42.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.40.$", "$.41.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.39.$", "$.40.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.38.$", "$.39.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.37.$", "$.38.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.36.$", "$.37.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.35.$", "$.36.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.34.$", "$.35.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.33.$", "$.34.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.32.$", "$.33.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.31.$", "$.32.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.43.$", "$.31.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.62.$", "$.63.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.61.$", "$.62.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.60.$", "$.61.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.59.$", "$.60.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.58.$", "$.59.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.57.$", "$.58.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.56.$", "$.57.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.55.$", "$.56.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.54.$", "$.55.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.53.$", "$.54.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.52.$", "$.53.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.51.$", "$.52.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.63.$", "$.51.$");
                    }
                }

                if (StaticVariables.transposeDirection.equals("-1")) {
                    // Put the numbers down by one.
                    // Move num 0 down to -1 (if it goes to 11 it will be moved
                    // later)
                    // Last step then fixes -1 to be 11

                    // Repeat this as often as required.
                    for (int repeatTranspose = 0; repeatTranspose < StaticVariables.transposeTimes; repeatTranspose++) {
                        splitLyrics[x] = splitLyrics[x].replace("$.1.$", "$.0.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.2.$", "$.1.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.3.$", "$.2.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.4.$", "$.3.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.5.$", "$.4.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.6.$", "$.5.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.7.$", "$.6.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.8.$", "$.7.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.9.$", "$.8.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.10.$", "$.9.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.11.$", "$.10.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.12.$", "$.11.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.0.$", "$.12.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.31.$", "$.30.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.32.$", "$.31.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.33.$", "$.32.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.34.$", "$.33.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.35.$", "$.34.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.36.$", "$.35.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.37.$", "$.36.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.38.$", "$.37.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.39.$", "$.38.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.40.$", "$.39.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.41.$", "$.40.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.42.$", "$.41.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.30.$", "$.42.$");

                        splitLyrics[x] = splitLyrics[x].replace("$.51.$", "$.50.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.52.$", "$.51.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.53.$", "$.52.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.54.$", "$.53.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.55.$", "$.54.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.56.$", "$.55.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.57.$", "$.56.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.58.$", "$.57.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.59.$", "$.58.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.60.$", "$.59.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.61.$", "$.60.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.62.$", "$.61.$");
                        splitLyrics[x] = splitLyrics[x].replace("$.50.$", "$.62.$");
                    }
                }
            }
        }
        return splitLyrics;
    }

    String numberToKey(Context c, Preferences preferences, String key) {
        // We need to decide which key the user likes the best for each one
        // Convert the key number into either a sharp or natural first
        // Then we swap sharps to flats if the user prefers these

        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            key = key.replace(properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        for (int z=0; z<properchordsharpsnumsb.length; z++) {
            key = key.replace(properchordsharpsnumsb[z],propersharpchords1b[z]);
        }
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            key = key.replace(chordnaturalnumsa[z],naturalchords1a[z]);
        }
        for (int z=0; z<chordnaturalnumsb.length; z++) {
            key = key.replace(chordnaturalnumsb[z],naturalchords1b[z]);
        }

        if (key.equals("G#") && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true)) {
            key = "Ab";
        } else if (key.equals("G#m") && preferences.getMyPreferenceBoolean(c,"prefKeyAbm",false)) {
            key = "Abm";
        } else if (key.equals("A#") && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true)) {
            key = "Bb";
        } else if (key.equals("A#m") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true)) {
            key = "Bbm";
        } else if (key.equals("C#") && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false)) {
            key = "Db";
        } else if (key.equals("C#m") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",true)) {
            key = "Dbm";
        } else if (key.equals("D#") && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true)) {
            key = "Eb";
        } else if (key.equals("D#m") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true)) {
            key = "Ebm";
        } else if (key.equals("F#") && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false)) {
            key = "Gb";
        } else if (key.equals("F#m") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) {
            key = "Gbm";
        }

        return key;
    }

    private String numberToChord1(String line, boolean forceflats, boolean forcesharps, boolean thisorcapousesflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forceflats || (thisorcapousesflats && !forcesharps)) {
            line = useFlats1(line);
        } else {
            line = useSharps1(line);
        }
        // Replace the naturals
        line = useNaturals1(line);
        return line;
    }

    private String numberToChord2(String line, boolean forceflats, boolean forcesharps, boolean thisorcapousesflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forceflats || (thisorcapousesflats && !forcesharps)) {
            line = useFlats2(line);
        } else {
            line = useSharps2(line);
        }
        // Replace the naturals
        line = useNaturals2(line);
        return line;
    }

    private String numberToChord3(String line, boolean forceflats, boolean forcesharps, boolean thisorcapousesflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forceflats || (thisorcapousesflats && !forcesharps)) {
            line = useFlats3(line);
        } else {
            line = useSharps3(line);
        }
        // Replace the naturals
        line = useNaturals3(line);
        return line;
    }

    private String numberToChord4(String line, boolean forceflats, boolean forcesharps, boolean thisorcapousesflats) {
        // If we are forcing sharps or flats do that, otherwise use our key preferences
        if (forceflats || (thisorcapousesflats && !forcesharps)) {
            line = useFlats4(line);
        } else {
            line = useSharps4(line);
        }
        // Replace the naturals
        line = useNaturals4(line);
        return line;
    }

    private boolean keyUsesFlats(Context c, Preferences preferences, String testkey) {

        boolean result;
        result = (testkey.equals("Ab") && preferences.getMyPreferenceBoolean(c,"prefKeyAb",true)) ||
                (testkey.equals("Bb") && preferences.getMyPreferenceBoolean(c,"prefKeyBb",true)) ||
                (testkey.equals("Db") && preferences.getMyPreferenceBoolean(c,"prefKeyDb",false)) ||
                (testkey.equals("Eb") && preferences.getMyPreferenceBoolean(c,"prefKeyEb",true)) ||
                (testkey.equals("Gb") && preferences.getMyPreferenceBoolean(c,"prefKeyGb",false)) ||
                (testkey.equals("Bbm") && preferences.getMyPreferenceBoolean(c,"prefKeyBbm",true)) ||
                (testkey.equals("Dbm") && preferences.getMyPreferenceBoolean(c,"prefKeyDbm",false)) ||
                (testkey.equals("Ebm") && preferences.getMyPreferenceBoolean(c,"prefKeyEbm",true)) ||
                (testkey.equals("Gbm") && preferences.getMyPreferenceBoolean(c,"prefKeyGbm",false)) ||
                testkey.equals("C") ||
                testkey.equals("F") ||
                testkey.equals("Dm") ||
                testkey.equals("Gm") ||
                testkey.equals("Cm");
        return result;
    }

    private String useFlats1(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords1a[z]);
        }
        return line;
    }

    private String useFlats2(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords2[z]);
        }
        return line;
    }

    private String useFlats3(String line) {
        for (int z=0; z<properchordflatsnumsc.length; z++) {
            line = line.replace(properchordflatsnumsc[z],properflatchords3c[z]);
        }
        for (int z=0; z<properchordflatsnumsb.length; z++) {
            line = line.replace(properchordflatsnumsb[z],properflatchords3b[z]);
        }
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords3a[z]);
        }
        return line;
    }

    private String useFlats4(String line) {
        for (int z=0; z<properchordflatsnumsa.length; z++) {
            line = line.replace(properchordflatsnumsa[z],properflatchords4[z]);
        }
        return line;
    }

    private String useSharps1(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords1a[z]);
        }
        return line;
    }

    private String useSharps2(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords2[z]);
        }
        return line;
    }

    private String useSharps3(String line) {
        for (int z=0; z<properchordsharpsnumsc.length; z++) {
            line = line.replace(properchordsharpsnumsc[z],propersharpchords3c[z]);
        }
        for (int z=0; z<properchordsharpsnumsb.length; z++) {
            line = line.replace(properchordsharpsnumsb[z],propersharpchords3b[z]);
        }
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords3a[z]);
        }
        return line;
    }

    private String useSharps4(String line) {
        for (int z=0; z<properchordsharpsnumsa.length; z++) {
            line = line.replace(properchordsharpsnumsa[z],propersharpchords4[z]);
        }
        return line;
    }

    private String useNaturals1(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords1a[z]);
        }
        return line;
    }

    private String useNaturals2(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords2[z]);
        }
        return line;
    }

    private String useNaturals3(String line) {
        for (int z=0; z<chordnaturalnumsc.length; z++) {
            line = line.replace(chordnaturalnumsc[z],naturalchords3c[z]);
        }
        for (int z=0; z<chordnaturalnumsb.length; z++) {
            line = line.replace(chordnaturalnumsb[z],naturalchords3b[z]);
        }
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords3a[z]);
        }
        return line;
    }

    private String useNaturals4(String line) {
        for (int z=0; z<chordnaturalnumsa.length; z++) {
            line = line.replace(chordnaturalnumsa[z],naturalchords4[z]);
        }
        return line;
    }

    private String fixDiminished(String line) {
        line = line.replace("m7b5", "ø"); // Half diminished
        line = line.replace("-7b5", "ø"); // Half diminished

        line = line.replace("dim7", "o"); // Diminished
        line = line.replace("dim", "o");
        line = line.replace("o7", "o");
        return line;
    }

    private String fixMajorSeventh(String line) {
        line = line.replace("maj7","Δ7");
        line = line.replace("ma7", "Δ7");
        return line;
    }

    private String fixAugmented(String line) {
        line = line.replace("aug","+");
        line = line.replace("#5","+");
        return line;
    }

    private String transposeDownOne(String string) {
        string = string.replace("$.1.$", "$.0.$");
        string = string.replace("$.2.$", "$.1.$");
        string = string.replace("$.3.$", "$.2.$");
        string = string.replace("$.4.$", "$.3.$");
        string = string.replace("$.5.$", "$.4.$");
        string = string.replace("$.6.$", "$.5.$");
        string = string.replace("$.7.$", "$.6.$");
        string = string.replace("$.8.$", "$.7.$");
        string = string.replace("$.9.$", "$.8.$");
        string = string.replace("$.10.$", "$.9.$");
        string = string.replace("$.11.$", "$.10.$");
        string = string.replace("$.12.$", "$.11.$");
        string = string.replace("$.0.$", "$.12.$");

        string = string.replace("$.31.$", "$.30.$");
        string = string.replace("$.32.$", "$.31.$");
        string = string.replace("$.33.$", "$.32.$");
        string = string.replace("$.34.$", "$.33.$");
        string = string.replace("$.35.$", "$.34.$");
        string = string.replace("$.36.$", "$.35.$");
        string = string.replace("$.37.$", "$.36.$");
        string = string.replace("$.38.$", "$.37.$");
        string = string.replace("$.39.$", "$.38.$");
        string = string.replace("$.40.$", "$.39.$");
        string = string.replace("$.41.$", "$.40.$");
        string = string.replace("$.42.$", "$.41.$");
        string = string.replace("$.30.$", "$.42.$");

        string = string.replace("$.51.$", "$.50.$");
        string = string.replace("$.52.$", "$.51.$");
        string = string.replace("$.53.$", "$.52.$");
        string = string.replace("$.54.$", "$.53.$");
        string = string.replace("$.55.$", "$.54.$");
        string = string.replace("$.56.$", "$.55.$");
        string = string.replace("$.57.$", "$.56.$");
        string = string.replace("$.58.$", "$.57.$");
        string = string.replace("$.59.$", "$.58.$");
        string = string.replace("$.60.$", "$.59.$");
        string = string.replace("$.61.$", "$.60.$");
        string = string.replace("$.62.$", "$.61.$");
        string = string.replace("$.50.$", "$.62.$");

        return string;
    }

    Song capoTranspose(Context c, Preferences preferences, Song song) {

        int numtimes = Integer.parseInt(song.getCapo());
        String capokey;

        // Get the capokey if it hasn't been set
        if (song.getKey()!=null) {
            capokey = keyToNumber(song.getKey());
            capokey = transposeKey(capokey,"-1",numtimes);
            capokey = numberToKey(c, preferences, capokey);
            // Decide if flats should be used
            capousesflats = keyUsesFlats(c, preferences, capokey);
        }

        // Now we change the chords into numbers
        switch (StaticVariables.detectedChordFormat) {
            default:
                transposedChords = chordToNumber1(transposedChords);
                break;

            case 2:
                transposedChords = chordToNumber2(transposedChords);
                break;

            case 3:
                transposedChords = chordToNumber3(transposedChords);
                break;

            case 4:
                transposedChords = chordToNumber4(transposedChords);
                break;
        }

        // Try to do a sensible capo change.
        // Do a for loop for each capo chord changing it by one each time until the desired fret change
        for (int s = 0; s < numtimes; s++) {
            transposedChords = transposeDownOne(transposedChords);
        }

        // Now convert the numbers back to the appropriate chords
        switch (StaticVariables.detectedChordFormat) {
            default:
                transposedChords = numberToChord1(transposedChords,false,false,capousesflats);
                break;

            case 2:
                transposedChords = numberToChord2(transposedChords,false,false,capousesflats);
                break;

            case 3:
                transposedChords = numberToChord3(transposedChords,false,false,capousesflats);
                break;

            case 4:
                transposedChords = numberToChord4(transposedChords,false,false,capousesflats);
                break;
        }

        return song;
    }

    ArrayList<String> quickCapoKey(Context c, Preferences preferences, String key) {
        // This is used to give the user a list of either simple fret number or fret number with new capo key
        ArrayList<String> al = new ArrayList<>();
        // Add a blank entry
        al.add("");

        boolean keyisset = false;
        String keynum = key;
        if (key!=null && !key.equals("") && !key.isEmpty()) {
            // A key has been set, so convert this to a number
            keynum = keyToNumber(key);
            keyisset = true;
        }

        // Loop through this 12 times to get the key options for each fret
        // The capo key goes down a semitone as the capo fret goes up a semitone!
        String whattoadd;
        for (int i=1; i<=11; i++) {
            if (keyisset) {
                keynum = transposeKey(keynum, "-1", 1);
                key = numberToKey(c, preferences, keynum);
                whattoadd = i + " (" + key + ")";
            } else {
                whattoadd = i + "";
            }
            al.add(whattoadd);
        }
        return al;
    }

    void checkChordFormat(Context c, String lyrics, Preferences preferences) {
        String[] splitLyrics = lyrics.split("\n");
        StaticVariables.detectedChordFormat = preferences.getMyPreferenceInt(c, "chordFormat", 1);

        // The user wants the app to guess the chord formatting, so we will detect formatting
        boolean contains_es_is = false;
        boolean contains_H = false;
        boolean contains_do = false;
        boolean contains_nash = false;
        boolean contains_nashnumeral = false;

        // Check if the user is using the same chord format as the song
        // Go through the chord lines and look for clues
        for (String splitLyric : splitLyrics) {
            if (splitLyric.startsWith(".")) {
                // Chord line
                if (splitLyric.contains("es") || splitLyric.contains("is") ||
                        splitLyric.contains(" a") || splitLyric.contains(".a") ||
                        //splitLyrics[x].contains(" b") || splitLyrics[x].contains(".b") || // Can't use for flat numeral chords
                        splitLyric.contains(" h") || splitLyric.contains(".h") ||
                        splitLyric.contains(" c") || splitLyric.contains(".c") ||
                        splitLyric.contains(" d") || splitLyric.contains(".d") ||
                        splitLyric.contains(" e") || splitLyric.contains(".e") ||
                        splitLyric.contains(" f") || splitLyric.contains(".f") ||
                        splitLyric.contains(" g") || splitLyric.contains(".g")) {
                    contains_es_is = true;
                } else if (splitLyric.contains("H")) {
                    contains_H = true;
                } else if (splitLyric.contains("Do") || splitLyric.contains("Re") || splitLyric.contains("Ré") ||
                        splitLyric.contains("Me") || splitLyric.contains("Fa") ||
                        splitLyric.contains("Sol") || splitLyric.contains("La") ||
                        splitLyric.contains("Si")) {
                    contains_do = true;
                } else if (splitLyric.contains(".2") || splitLyric.contains(" 2") ||
                        splitLyric.contains(".3") || splitLyric.contains(" 3") ||
                        splitLyric.contains(".4") || splitLyric.contains(" 4") ||
                        splitLyric.contains(".5") || splitLyric.contains(" 5") ||
                        splitLyric.contains(".6") || splitLyric.contains(" 6") ||
                        splitLyric.contains(".7") || splitLyric.contains(" 7")) {
                    contains_nash = true;
                } else if (splitLyric.contains(".I") || splitLyric.contains(" I") ||
                        splitLyric.contains(".V") || splitLyric.contains(" V") ||
                        splitLyric.contains(".IV") || splitLyric.contains(" IV")) {
                    contains_nashnumeral = true;
                }
            }
        }

        //int detected = 0;
        // Set the chord style detected
        if (contains_do && !preferences.getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            StaticVariables.detectedChordFormat = 4;
        } else if (contains_H && !contains_es_is && !preferences.getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            StaticVariables.detectedChordFormat = 2;
        } else if ((contains_H || contains_es_is) && !preferences.getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            StaticVariables.detectedChordFormat = 3;
        } else if (contains_nash && !preferences.getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            StaticVariables.detectedChordFormat = 5;
        } else if (contains_nashnumeral && !preferences.getMyPreferenceBoolean(c, "chordFormatUsePreferred", false)) {
            StaticVariables.detectedChordFormat = 6;
        } else {
            StaticVariables.detectedChordFormat = 1;
        }

        // Ok so the user chord format may not quite match the song - it might though!
    }

}


/*} else {

                String[] conditions = {"sharp","flat","natural"};
                for (String condition: conditions) {
                    line = replaceBits(line,"VII",bitssharp[num7],condition);
                    line = replaceBits(line,"VI",bitssharp[num6],condition);
                    line = replaceBits(line,"IV",bitssharp[num4],condition);
                    line = replaceBits(line,"V",bitssharp[num5],condition);
                    line = replaceBits(line,"III",bitssharp[num3],condition);
                    line = replaceBits(line,"II",bitssharp[num2],condition);
                    line = replaceBits(line,"I",bitssharp[root],condition);

                    line = replaceBits(line,"#VII",bitssharp[root],condition);
                    line = replaceBits(line,"#VI",bitssharp[num6+1],condition);
                    line = replaceBits(line,"#IV",bitssharp[num4+1],condition);
                    line = replaceBits(line,"#V",bitssharp[num5+1],condition);
                    line = replaceBits(line,"#III",bitssharp[num3+1],condition);
                    line = replaceBits(line,"#II",bitssharp[num2+1],condition);
                    line = replaceBits(line,"#I",bitssharp[root+1],condition);

                    line = replaceBits(line,"VII",bitsflat[num7],condition);
                    line = replaceBits(line,"VI",bitsflat[num6],condition);
                    line = replaceBits(line,"IV",bitsflat[num4],condition);
                    line = replaceBits(line,"V",bitsflat[num5],condition);
                    line = replaceBits(line,"III",bitsflat[num3],condition);
                    line = replaceBits(line,"II",bitsflat[num2],condition);
                    line = replaceBits(line,"I",bitsflat[root],condition);

                    line = replaceBits(line,"bVII",bitsflat[num7-1],condition);
                    line = replaceBits(line,"bVI",bitsflat[num6-1],condition);
                    line = replaceBits(line,"bIV",bitsflat[num4-1],condition);
                    line = replaceBits(line,"bV",bitsflat[num5-1],condition);
                    line = replaceBits(line,"bIII",bitsflat[num3-1],condition);
                    line = replaceBits(line,"bII",bitsflat[num2-1],condition);
                    line = replaceBits(line,"bI",bitsflat[root+11],condition);

                    line = replaceBits(line, "#vii", bitssharp[num7+1]+"m", condition);
                    line = replaceBits(line, "#vi", bitssharp[num6+1]+"m", condition);
                    line = replaceBits(line, "#iv", bitssharp[num4+1]+"m", condition);
                    line = replaceBits(line, "#v", bitssharp[num5+1]+"m", condition);
                    line = replaceBits(line, "#iii", bitssharp[num3+1]+"m", condition);
                    line = replaceBits(line, "#ii", bitssharp[num2+1]+"m", condition);
                    line = replaceBits(line, "#i", bitssharp[root+1]+"m", condition);

                    line = replaceBits(line, "bvii", bitsflat[num7-1]+"m", condition);
                    line = replaceBits(line, "bvi", bitsflat[num6-1]+"m", condition);
                    line = replaceBits(line, "biv", bitsflat[num4-1]+"m", condition);
                    line = replaceBits(line, "bv", bitsflat[num5-1]+"m", condition);
                    line = replaceBits(line, "biii", bitsflat[num3-1]+"m", condition);
                    line = replaceBits(line, "bii", bitsflat[num2-1]+"m", condition);
                    line = replaceBits(line, "bi", bitsflat[root+11]+"m", condition);

                    line = replaceBits(line, "vii", bitssharp[num7]+"m", condition);
                    line = replaceBits(line, "vi", bitssharp[num6]+"m", condition);
                    line = replaceBits(line, "iv", bitssharp[num4]+"m", condition);
                    line = replaceBits(line, "v", bitssharp[num5]+"m", condition);
                    line = replaceBits(line, "iii", bitssharp[num3]+"m", condition);
                    line = replaceBits(line, "ii", bitssharp[num2]+"m", condition);
                    line = replaceBits(line, "i", bitssharp[root]+"m", condition);
                    if (originalkey.endsWith("m")) {
                        line = replaceBits(line,"vii", bitssharp[num7]+"m",condition);
                    } else {
                        line = replaceBits(line,"vii", bitssharp[num7]+"o",condition);
                    }
                    line = replaceBits(line, "vi", bitsflat[num6]+"m", condition);
                    line = replaceBits(line, "iv", bitsflat[num4]+"m", condition);
                    line = replaceBits(line, "v", bitsflat[num5]+"m", condition);
                    line = replaceBits(line, "iii", bitsflat[num3]+"m", condition);
                    if (originalkey.endsWith("m")) {
                        line = replaceBits(line, "ii", bitsflat[num2]+"o", condition);
                    } else {
                        line = replaceBits(line, "ii", bitsflat[num2]+"m", condition);
                    }

                    line = replaceBits(line, "i", bitsflat[root]+"m", condition);
                }
            }*/