package com.garethevans.church.opensongtablet.setmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetMenuSetBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songmenu.RandomSongBottomSheet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SetMenuBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetMenuSetBinding myView;
    private MainActivityInterface mainActivityInterface;

    private static final String TAG = "SetMenuBottomSheet";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetMenuSetBinding.inflate(inflater, container, false);

        // Initialise the 'close' floatingactionbutton
        myView.dialogHeading.setClose(this);

        // Check views allowed
        checkViewsAllowed();

        // Set up listeners
        setListeners();

        return myView.getRoot();
    }

    private void checkViewsAllowed() {
        // Check there are songs!
        boolean songs = mainActivityInterface.getCurrentSet().getSetFilenames().size()>0;

        if (!songs) {
            myView.variation.setVisibility(View.GONE);
            myView.shuffleSet.setVisibility(View.GONE);
            myView.randomSong.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        myView.createSet.setOnClickListener(v -> {
            mainActivityInterface.displayAreYouSure("newSet",getString(R.string.set_new),null,null,null,null);
            dismiss();
        });
        myView.shuffleSet.setOnClickListener(v -> {
            mainActivityInterface.getSetActions().shuffleSet(requireContext(),mainActivityInterface);
            mainActivityInterface.updateFragment("set_updateView",null,null);
            dismiss();
        });
        myView.addScripture.setOnClickListener(v -> {
            mainActivityInterface.navigateToFragment("opensongapp://settings/bible",0);
            dismiss();
        });
        myView.randomSong.setOnClickListener(v -> {
            RandomSongBottomSheet randomSongBottomSheet = new RandomSongBottomSheet("set");
            randomSongBottomSheet.show(requireActivity().getSupportFragmentManager(),"RandomBottomSheet");
            dismiss();
        });
        myView.manageSet.setOnClickListener(v -> {
            Log.d(TAG, "fragManager: "+requireActivity().getSupportFragmentManager());
            mainActivityInterface.navigateToFragment("opensongapp://settings/sets",-1);
            dismiss();
        });
        myView.variation.setOnClickListener(v -> {
            SetVariationBottomSheet setVariationBottomSheet = new SetVariationBottomSheet();
            setVariationBottomSheet.show(requireActivity().getSupportFragmentManager(),"setVariation");
            dismiss();
        });
    }

}