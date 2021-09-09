package com.garethevans.church.opensongtablet.autoscroll;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialEditText;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.Timer;
import java.util.TimerTask;

public class Autoscroll {

    private boolean isAutoscrolling, wasScrolling, autoscrollOK, isPaused = false, showOn = true,
            autoscrollAutoStart, autoscrollActivated = false, autoscrollUseDefaultTime;
    private MainActivityInterface mainActivityInterface;
    private int songDelay;
    private int songDuration;
    private int displayHeight;
    private int songHeight;
    private int scrollTime;
    private int flashCount;
    private final int flashTime = 600;
    private int autoscrollDefaultSongLength;
    private int autoscrollDefaultSongPreDelay;
    private int colorOn;
    private int colorOff;
    private final int updateTime = 60;
    private float scrollIncrement, scrollPosition, scrollCount;
    private LinearLayout autoscrollView;
    private MyZoomLayout myZoomLayout;
    private MaterialTextView autoscrollTimeText, autoscrollTotalTimeText;
    Timer timer;
    TimerTask timerTask;
    private String currentTimeString, totalTimeString;

    // The setters
    public void setIsAutoscrolling(boolean isAutoscrolling) {
        this.isAutoscrolling = isAutoscrolling;
    }
    public void setWasScrolling(boolean wasScrolling) {
        this.wasScrolling = wasScrolling;
    }
    public void setAutoscrollOK(boolean autoscrollOK) {
        this.autoscrollOK = autoscrollOK;
    }

    public void initialiseAutoscroll(MainActivityInterface mainActivityInterface,
                                     MyZoomLayout myZoomLayout, LinearLayout autoscrollView) {
        this.mainActivityInterface = mainActivityInterface;
        this.myZoomLayout = myZoomLayout;
        this.autoscrollView = autoscrollView;
        autoscrollTimeText = autoscrollView.findViewById(R.id.autoscrollTime);
        autoscrollTotalTimeText = autoscrollView.findViewById(R.id.autoscrollTotalTime);
    }

    public void initialiseSongAutoscroll(Context c, int songHeight, int displayHeight) {
        this.displayHeight = displayHeight;
        this.songHeight = songHeight;
        autoscrollAutoStart = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "autoscrollAutoStart", false);
        autoscrollUseDefaultTime = mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,
                "autoscrollUseDefaultTime", true);
        autoscrollDefaultSongPreDelay = mainActivityInterface.getPreferences().getMyPreferenceInt(c,
                "autoscrollDefaultSongPreDelay", 20);
        autoscrollDefaultSongLength = mainActivityInterface.getPreferences().getMyPreferenceInt(c,
                "autoscrollDefaultSongLength", 180);
        autoscrollTotalTimeText.setOnClickListener(view -> isPaused = !isPaused);
        autoscrollTimeText.setOnClickListener(view -> isPaused = !isPaused);
        float alpha = mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha();
        int color = mainActivityInterface.getMyThemeColors().getPageButtonsSplitColor();
        autoscrollView.setBackgroundColor(color);
        autoscrollView.setAlpha(alpha);
        autoscrollView.setAlpha(mainActivityInterface.getMyThemeColors().getPageButtonsSplitAlpha());
        colorOn = mainActivityInterface.getMyThemeColors().getExtraInfoTextColor();
        colorOff = mainActivityInterface.getMyThemeColors().getPageButtonsColor();
    }

    public void pauseAutoscroll() {
        isPaused = !isPaused;
    }

    private void figureOutTimes() {
        scrollPosition = 0;
        scrollCount = 0;
        scrollTime = 0;
        songDelay = stringToInt(mainActivityInterface.getSong().getAutoscrolldelay());
        songDuration = stringToInt(mainActivityInterface.getSong().getAutoscrolllength());
        if (songDuration==0 && autoscrollUseDefaultTime) {
            songDelay = autoscrollDefaultSongPreDelay;
            songDuration = autoscrollDefaultSongLength;
        }

        if (songDuration>0) {
            // We have valid times, so good to go.  Calculate the autoscroll values
            calculateAutoscroll();
            resetTimers();

            totalTimeString = " / " + mainActivityInterface.getTimeTools().timeFormatFixer(songDuration);
            autoscrollTotalTimeText.post(() -> autoscrollTotalTimeText.setText(totalTimeString));

            setupTimer(mainActivityInterface);

            setAutoscrollOK(true);

            if (autoscrollActivated && autoscrollAutoStart) {
                // We have already initiated scrolling and want it to start automatically
                startAutoscroll();
            }
        }
    }

    public void startAutoscroll() {
        myZoomLayout.setIsUserTouching(false);
        figureOutTimes();

        if (autoscrollOK) {
            if (timer==null) {
                timer = new Timer();
            }
            if (timerTask==null) {
                setupTimer(mainActivityInterface);
            }
            myZoomLayout.scrollTo(0,0);
            isPaused = false;
            autoscrollActivated = true;
            setIsAutoscrolling(true);
            autoscrollView.post(() -> autoscrollView.setVisibility(View.VISIBLE));
            try {
                timer.scheduleAtFixedRate(timerTask, 0, updateTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void stopAutoscroll() {
        // Force stopped, so reset activated
        autoscrollActivated = false;
        // Now end
        endAutoscroll();
    }
    private void endAutoscroll() {
        // Called at normal end.  Doesn't reset activated
        setIsAutoscrolling(false);
        autoscrollView.postDelayed(() -> autoscrollView.setVisibility(View.GONE),1000);
        resetTimers();
    }

    private void resetTimers() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }
    private void setupTimer(MainActivityInterface mainActivityInterface) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                flashCount = flashCount + updateTime;
                if (flashCount>flashTime) {
                    showOn = !showOn;
                    flashCount = 0;
                }
                if (!isPaused) {
                    autoscrollTimeText.setTextColor(colorOn);
                    currentTimeString = mainActivityInterface.getTimeTools().timeFormatFixer((int)((float)scrollTime/1000f));
                    if (scrollTime < songDelay*1000f) {
                        // This is predelay, so set the alpha down
                        autoscrollTimeText.post(() -> {
                            autoscrollTimeText.setAlpha(0.6f);
                            autoscrollTimeText.setText(currentTimeString);
                        });
                    } else {
                        // Fix the alpha back and set the text
                        autoscrollTimeText.post(() -> {
                            autoscrollTimeText.setAlpha(1.0f);
                            autoscrollTimeText.setText(currentTimeString);
                        });

                        // Do the scroll as long as the user isn't touching the screen
                        if (!myZoomLayout.getIsUserTouching()) {
                            // Rather than assume the scroll position (as it could've been manually dragged)
                            // Compare the scroll count (float) and scroll position (int).
                            myZoomLayout.post(() -> {
                                scrollCount = scrollCount + scrollIncrement;
                                if ((Math.max(scrollPosition,myZoomLayout.getScrollPos()) -
                                        Math.min(scrollPosition,myZoomLayout.getScrollPos()))<1) {
                                    // Don't get stuck on float rounding being compounded - use the scroll count
                                    scrollPosition = scrollCount;
                                } else {
                                    // We've moved (more than 1px), so get the actual start position
                                    scrollPosition = myZoomLayout.getScrollPos() + scrollIncrement;
                                }
                                myZoomLayout.smoothScrollTo(0, (int) scrollPosition, updateTime);
                            });
                        }
                    }
                    scrollTime = scrollTime + updateTime;
                    if (Math.ceil(scrollPosition) >= (songHeight - displayHeight)) {
                        // Scrolling is done as we've reached the end
                        endAutoscroll();
                    }
                } else {
                    autoscrollTimeText.post(() -> {
                        if (showOn) {
                            autoscrollTimeText.setTextColor(colorOn);
                        } else {
                            autoscrollTimeText.setTextColor(colorOff);
                        }
                    });

                }
            }
        };
    }

    // The getters
    public boolean getIsAutoscrolling() {
        return isAutoscrolling;
    }
    public boolean getWasScrolling() {
        return wasScrolling;
    }
    public boolean getAutoscrollOK() {
        return autoscrollOK;
    }

    // The calculations
    private void calculateAutoscroll() {
        // The total scroll amount is the height of the view - the screen height.
        // If this is less than 0, no scrolling is required.
        int scrollHeight = songHeight - displayHeight;

        if (scrollHeight>0) {
            // The scroll happens every 1 sec (updateTime).
            // The number of times this will happen is calculated as follows
            float numberScrolls = ((songDuration-songDelay)*1000f)/updateTime;
            // The scroll distance for each scroll is calculated as follows
            scrollIncrement = (float)scrollHeight / numberScrolls;
        } else {
            scrollIncrement = 0;
        }

        flashCount = 0;
    }

    public void speedUpAutoscroll() {
        // This increases the increment by 25%
        scrollIncrement = 1.25f * scrollIncrement;
    }
    public void slowDownAutoscroll() {
        // This decreases the increment by 25%
        scrollIncrement = 0.75f * scrollIncrement;
    }
    private int stringToInt(String string) {
        if (string!=null && !string.isEmpty()) {
            try {
                return Integer.parseInt(string);
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    // This is called from both the Autoscroll settings and bottom sheet to activate the link audio button
    public void checkLinkAudio(Context c, MainActivityInterface mainActivityInterface,
                                MaterialButton materialButton, MaterialEditText durationText,
                                MaterialEditText delayText, final int delay) {
        // If link audio is set and time is valid get it and set the button action
        if (!mainActivityInterface.getSong().getLinkaudio().isEmpty()) {
            Uri uri = mainActivityInterface.getStorageAccess().fixLocalisedUri(c,
                    mainActivityInterface, mainActivityInterface.getSong().getLinkaudio());
            if (uri!=null && mainActivityInterface.getStorageAccess().uriExists(c, uri)) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(c, uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mediaPlayer1 -> materialButton.setOnClickListener(v -> {
                    // Updating the text fields triggers the listener which saves
                    int duration = mediaPlayer1.getDuration()/1000;
                    if (delay > duration) {
                        delayText.setText(duration+"");
                    }
                    durationText.setText(""+duration);
                    materialButton.setVisibility(View.VISIBLE);
                }));
            } else {
                materialButton.setVisibility(View.GONE);
            }
        }
    }
}