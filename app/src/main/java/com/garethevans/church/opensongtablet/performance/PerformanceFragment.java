package com.garethevans.church.opensongtablet.performance;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.request.RequestOptions;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.controls.GestureListener;
import com.garethevans.church.opensongtablet.customviews.GlideApp;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
import com.garethevans.church.opensongtablet.databinding.ModePerformanceBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.pdf.PDFPageAdapter;
import com.garethevans.church.opensongtablet.stickynotes.StickyPopUp;

import java.util.Locale;

public class PerformanceFragment extends Fragment {

    private final String TAG = "PerformanceFragment";
    // Helper classes for the heavy lifting
    private StickyPopUp stickyPopUp;

    private MainActivityInterface mainActivityInterface;

    // The variables used in the fragment
    private boolean songAutoScaleColumnMaximise,
            songAutoScaleOverrideFull, songAutoScaleOverrideWidth;
    private int screenHeight;
    public int songViewWidth, songViewHeight, screenWidth, swipeMinimumDistance,
            swipeMaxDistanceYError, swipeMinimumVelocity;
    private String autoScale;
    private ModePerformanceBinding myView;
    private Animation animSlideIn, animSlideOut;
    private GestureDetector gestureDetector;
    private PDFPageAdapter pdfPageAdapter;

    // Attaching and destroying
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"Performance");
    }
    @Override
    public void onDetach() {
        super.onDetach();
        dealWithStickyNotes(false,true);
        if (mainActivityInterface.getAppActionBar()!=null) {
            mainActivityInterface.getAppActionBar().setPerformanceMode(false);
        }
        mainActivityInterface.registerFragment(null,"Performance");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    // The logic to start this fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = ModePerformanceBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Register this fragment
        mainActivityInterface.registerFragment(this,"Performance");

        // Initialise the helper classes that do the heavy lifting
        initialiseHelpers();

        // Pass view references to the Autoscroll class
        mainActivityInterface.getAutoscroll().initialiseAutoscroll(myView.zoomLayout);

        mainActivityInterface.lockDrawer(false);
        //mainActivityInterface.hideActionBar(false);
        mainActivityInterface.hideActionButton(false);

        //mainActivityInterface.changeActionBarVisible(false,false);
        // Load in preferences
        loadPreferences();

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));

        // Set listeners for the scroll/scale/gestures
        setGestureListeners();

        // Show the actionBar and hide it after a time if that's the user's preference
        mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"hideActionBar",false);

        mainActivityInterface.getAppActionBar().setHideActionBar(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"hideActionBar",false));
        mainActivityInterface.getAppActionBar().setPerformanceMode(true);
        mainActivityInterface.getAppActionBar().showActionBar();

        // Set tutorials
        Handler h = new Handler();
        Runnable r = () -> mainActivityInterface.showTutorial("performanceView");
        h.postDelayed(r,1000);

        return root;
    }

    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        stickyPopUp = new StickyPopUp();
        mainActivityInterface.getPerformanceGestures().setZoomLayout(myView.zoomLayout);
        mainActivityInterface.getPerformanceGestures().setPDFRecycler(myView.pdfView);
    }
    private void loadPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(),mainActivityInterface);
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
        swipeMinimumDistance = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumDistance", 250);
        swipeMaxDistanceYError = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMaxDistanceYError", 200);
        swipeMinimumVelocity = mainActivityInterface.getPreferences().getMyPreferenceInt(getActivity(), "swipeMinimumVelocity", 600);
        songAutoScaleOverrideWidth = false;
        songAutoScaleOverrideFull = false;
        myView.mypage.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
    }

    // Displaying the song
    public void doSongLoad(String folder, String filename) {
        // Loading the song is dealt with in this fragment as specific actions are required

        // During the load song call, the song is cleared
        // However if first extracts the folder and filename we've just set
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename((filename));

        new Thread(() -> {
            // Quick fade the current page
            requireActivity().runOnUiThread(() -> {
                if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
                    animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_left);
                } else {
                    animSlideOut = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_out_right);
                }
                if (myView.pdfView.getVisibility()==View.VISIBLE) {
                    myView.pdfView.startAnimation(animSlideOut);
                } else {
                    myView.pageHolder.startAnimation(animSlideOut);
                }

                // Reset the views
                mainActivityInterface.setSectionViews(null);
                mainActivityInterface.setSongSheetTitleLayout(null);

                // Now reset the song
                mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                        mainActivityInterface.getSong(),false));

                mainActivityInterface.moveToSongInSongMenu();
                prepareSongViews();
            });
        }).start();
    }
    private void prepareSongViews() {
        // This is called on UI thread above;

        // Set the default color
        myView.pageHolder.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());

        // Remove old views
        myView.songSheetTitle.removeAllViews();
        myView.songSheetTitle.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        if (mainActivityInterface.getSongSheetTitleLayout() != null &&
                mainActivityInterface.getSongSheetTitleLayout().getParent() != null) {
            ((LinearLayout) mainActivityInterface.getSongSheetTitleLayout().getParent()).removeAllViews();
        }
        myView.pdfView.setVisibility(View.GONE);
        myView.zoomLayout.setVisibility(View.GONE);
        myView.songView.setY(0);

        if (mainActivityInterface.getSong().getFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            // We populate the PDF recycler view with the pages
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int availWidth = getResources().getDisplayMetrics().widthPixels;
                int availHeight = getResources().getDisplayMetrics().heightPixels - mainActivityInterface.getMyActionBar().getHeight();
                pdfPageAdapter = new PDFPageAdapter(requireContext(), mainActivityInterface,
                        availWidth, availHeight);

                myView.pdfView.post(() -> {
                    myView.pdfView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false));
                    myView.pdfView.setAdapter(pdfPageAdapter);
                    myView.pdfView.setVisibility(View.VISIBLE);
                    // Set up the type of animate in
                    if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
                        animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
                    } else {
                        animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
                    }
                    myView.pdfView.startAnimation(animSlideIn);
                });
            }
        } else if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
            // Now prepare the song sections views so we can measure them for scaling using a view tree observer
            mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().
                    setSongInLayout(requireContext(), mainActivityInterface,
                            mainActivityInterface.getSong().getLyrics(), false, false));

            // We now have the 1 column layout ready, so we can set the view observer to measure once drawn
            setUpTestViewListener();

        }

        // Update the toolbar with the song (null)
        mainActivityInterface.updateToolbar(null);
    }

    private void setUpTestViewListener() {
        ViewTreeObserver testObs = myView.testPane.getViewTreeObserver();
        testObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // The views are ready so prepare to create the song page
                songIsReadyToDisplay();
                // We can now remove this listener
                myView.testPane.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        for (View view:mainActivityInterface.getSectionViews()) {
            Log.d(TAG,"numViews= "+mainActivityInterface.getSectionViews().size());
            myView.testPane.addView(view);
        }
    }
    private void songIsReadyToDisplay(){
        // Decide if we want to show the song title header
        // Add a listener to scale the title and resize it to move the contents down
        // Get the song sheet headers - will be null if not required
        mainActivityInterface.setSongSheetTitleLayout(mainActivityInterface.getSongSheetHeaders().getSongSheet(requireContext(),
                mainActivityInterface, mainActivityInterface.getSong(), mainActivityInterface.getProcessSong().getScaleComments(), false));

        if (mainActivityInterface.getSongSheetTitleLayout()!=null) {
            myView.songSheetTitle.setVisibility(View.INVISIBLE);
            myView.songSheetTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Now it is drawn, set the height
                    try {
                        myView.songSheetTitle.post(() -> {
                            int height = (int)((float)myView.songSheetTitle.getMeasuredHeight());
                            myView.songSheetTitle.getLayoutParams().height = height;
                            //int height = (int)((float)myView.songSheetTitle.getMeasuredHeight() * scaleFactor);
                            Log.d(TAG,"bottom: "+myView.songSheetTitle.getBottom());
                            Log.d(TAG, "height=" + height);
                            Log.d(TAG, "scaleFactor=" + scaleFactor);
                            myView.songSheetTitle.setVisibility(View.VISIBLE);
                            myView.songSheetTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            myView.songSheetTitle.addView(mainActivityInterface.getSongSheetTitleLayout());

        } else {
            myView.songSheetTitle.post(() -> myView.songSheetTitle.setVisibility(View.GONE));
        }

        // All views have now been drawn, so measure the arraylist views

        for (View v:mainActivityInterface.getSectionViews())  {
            int width = v.getMeasuredWidth();
            int height = v.getMeasuredHeight();
            mainActivityInterface.addSectionSize(width,height);
        }

        screenWidth = myView.mypage.getMeasuredWidth();
        screenHeight = myView.mypage.getMeasuredHeight();

        myView.zoomLayout.setVisibility(View.VISIBLE);
        myView.zoomLayout.setPageSize(screenWidth,screenHeight);

        scaleFactor = mainActivityInterface.getProcessSong().addViewsToScreen(getContext(),
                mainActivityInterface,
                myView.testPane, myView.pageHolder, myView.songView, myView.songSheetTitle,
                screenWidth, screenHeight,
                myView.col1, myView.col2, myView.col3);

        // Pass this scale factor to the zoom layout
        myView.zoomLayout.setCurrentScale(scaleFactor);

        // Set up the type of animate in
        if (mainActivityInterface.getDisplayPrevNext().getSwipeDirection().equals("R2L")) {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        } else {
            animSlideIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
        }

        // Now that the view is being drawn, set a view tree observer to get the sizes once done
        // Then we can switch on the highlighter, sticky notes, etc.
        ViewTreeObserver songViewObs = myView.songView.getViewTreeObserver();
        songViewObs.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (myView != null && mainActivityInterface!=null &&
                        mainActivityInterface.getSectionWidths()!=null && mainActivityInterface.getSectionWidths().size()>0) {
                    float maxWidth = 0;
                    float totalHeight = 0;
                    for (int x=0;x<mainActivityInterface.getSectionViews().size();x++) {
                        maxWidth = Math.max(maxWidth,mainActivityInterface.getSectionWidths().get(x)*scaleFactor);
                        totalHeight += mainActivityInterface.getSectionHeights().get(x)*scaleFactor;
                    }
                    final int w = (int)maxWidth;
                    final int h = (int)totalHeight;

                    myView.zoomLayout.setSongSize(w,h);

                    // Now deal with the highlighter file
                    dealWithHighlighterFile(w,h);

                    // Load up the sticky notes if the user wants them
                    dealWithStickyNotes(false,false);

                    // Send the autoscroll information (if required)
                    mainActivityInterface.getAutoscroll().initialiseSongAutoscroll(requireContext(), h, screenHeight);

                    // Now take a screenshot (only runs is w!=0 and h!=0)
                    // TODO rethink this as may not be attached 2 sec later!
                    /*try {
                        if (getActivity()!=null && isAdded()) {
                            myView.songView.postDelayed(() -> requireActivity().runOnUiThread(() -> getScreenshot(w, h)), 2000);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    // Now remove this viewtree observer
                    myView.songView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
        myView.pageHolder.startAnimation(animSlideIn);

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (mainActivityInterface.getSong().getCurrentSection() < 0) {
            mainActivityInterface.getSong().setCurrentSection(-(1 + mainActivityInterface.getSong().getCurrentSection()));
        }

        // Set the previous/next if we want to
        mainActivityInterface.getDisplayPrevNext().setPrevNext();

        // Start the pad (if the pads are activated and the pad is valid)
        mainActivityInterface.getPad().autoStartPad(requireContext());

        // Update any midi commands (if any)
        mainActivityInterface.getMidi().buildSongMidiMessages();

        // Update the secondary display (if present)
        mainActivityInterface.updateDisplay("info");
        mainActivityInterface.updateDisplay("content");
    }

    private void dealWithHighlighterFile(int w, int h) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")) {
            // Set the highlighter image view to match
            myView.highlighterView.setVisibility(View.INVISIBLE);
            // Once the view is ready at the required size, deal with it
            ViewTreeObserver highlighterVTO = myView.highlighterView.getViewTreeObserver();
            highlighterVTO.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Load in the bitmap with these dimensions
                    Bitmap highlighterBitmap = mainActivityInterface.getProcessSong().
                            getHighlighterFile(requireContext(), mainActivityInterface, w, h);
                    if (highlighterBitmap != null &&
                            mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(), "drawingAutoDisplay", true)) {
                        myView.highlighterView.setVisibility(View.VISIBLE);
                        RequestOptions scaleOption = new RequestOptions().sizeMultiplier(myView.zoomLayout.getScaleFactor());
                        RequestOptions sizeOption = new RequestOptions().override(w,h);
                        GlideApp.with(requireContext()).load(highlighterBitmap).override(w,h).
                                apply(scaleOption).apply(sizeOption).into(myView.highlighterView);
                        myView.highlighterView.setScaleY(myView.zoomLayout.getScaleFactor());
                        // Hide after a certain length of time
                        int timetohide = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(), "timeToDisplayHighlighter", 0);
                        if (timetohide != 0) {
                            new Handler().postDelayed(() -> myView.highlighterView.setVisibility(View.GONE), timetohide);
                        }
                    } else {
                        try {
                            myView.highlighterView.post(() -> myView.highlighterView.setVisibility(View.GONE));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        myView.highlighterView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            myView.songView.post(() -> myView.songView.getLayoutParams().height = h);
            //myView.pageHolder.post(() -> myView.pageHolder.getLayoutParams().height = h);
            myView.highlighterView.post(() -> {
                myView.highlighterView.getLayoutParams().height = h;
                myView.highlighterView.getLayoutParams().width = w;
                myView.highlighterView.requestLayout();
                myView.highlighterView.invalidate();
            });
        } else {
            myView.highlighterView.post(() -> myView.highlighterView.setVisibility(View.GONE));
        }
    }
    public void dealWithStickyNotes(boolean forceShow, boolean hide) {
        if (hide) {
            if (stickyPopUp!=null) {
                stickyPopUp.closeSticky();
            }
        } else {
            if ((mainActivityInterface != null && mainActivityInterface.getSong() != null &&
                    mainActivityInterface.getSong().getNotes() != null &&
                    !mainActivityInterface.getSong().getNotes().isEmpty() &&
                    mainActivityInterface.getPreferences().
                            getMyPreferenceBoolean(requireContext(), "stickyAuto", true)) || forceShow) {
                // This is called from the MainActivity when we clicked on the page button
                stickyPopUp.floatSticky(requireContext(), mainActivityInterface, myView.pageHolder, forceShow);
            } }
    }

    // The scale and gesture bits of the code
    private float scaleFactor = 1.0f;
    @SuppressLint("ClickableViewAccessibility")
    private void setGestureListeners(){
        // get the gesture detector
        gestureDetector = new GestureDetector(requireContext(), new GestureListener(mainActivityInterface,
                mainActivityInterface.getPerformanceGestures(),swipeMinimumDistance, swipeMaxDistanceYError,swipeMinimumVelocity));

        // Any interaction with the screen should trigger the display prev/next (if required)
        myView.zoomLayout.setOnTouchListener((view, motionEvent) -> {
            mainActivityInterface.getDisplayPrevNext().showAndHide();
            return gestureDetector.onTouchEvent(motionEvent);
        });
        myView.pdfView.setOnTouchListener((view, motionEvent) -> {
            mainActivityInterface.getDisplayPrevNext().showAndHide();
            return gestureDetector.onTouchEvent(motionEvent);
        });
    }

    public void pdfScrollToPage(int pageNumber) {
        Log.d(TAG,"trying to scroll to "+pageNumber);
        //mainActivityInterface.getSong().setPdfPageCurrent(pageNumber);
        LinearLayoutManager llm = (LinearLayoutManager) myView.pdfView.getLayoutManager();
        if (llm!=null) {
            llm.scrollToPosition(pageNumber-1);
        }
    }

    private void getScreenshot(int w, int h) {
        if (!mainActivityInterface.getPreferences().
                getMyPreferenceString(requireContext(),"songAutoScale","W").equals("N")
                && w!=0 && h!=0) {
            try {
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                if (myView != null) {
                    myView.songView.layout(0, 0, w, h);
                    myView.songView.draw(canvas);
                    mainActivityInterface.setScreenshot(bitmap);
                }
            } catch (Exception | OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    public MyZoomLayout getZoomLayout() {
        return myView.zoomLayout;
    }

    public void updateSizes(int width, int height) {
        if (width<0) {
            songViewWidth = screenWidth;
        } else {
            songViewWidth = width;
        }
        songViewHeight = height;
    }

}
