package com.finmate.UI.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finmate.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemeDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dialog_theme, container, false);
    }
}
