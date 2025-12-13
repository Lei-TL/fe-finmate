package com.finmate.ui.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.finmate.R;
import com.finmate.core.ui.ThemeHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ThemeDialog extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_theme_settings, container, false);

        LinearLayout btnLight = view.findViewById(R.id.btnLight);
        LinearLayout btnDark = view.findViewById(R.id.btnDark);
        LinearLayout btnSystem = view.findViewById(R.id.btnSystem);

        ImageView ivLightCheck = view.findViewById(R.id.ivLightCheck);
        ImageView ivDarkCheck = view.findViewById(R.id.ivDarkCheck);
        ImageView ivSystemCheck = view.findViewById(R.id.ivSystemCheck);

        String currentTheme = ThemeHelper.getCurrentTheme(requireContext());
        hideAllChecks(ivLightCheck, ivDarkCheck, ivSystemCheck);

        if (ThemeHelper.THEME_LIGHT.equals(currentTheme)) {
            ivLightCheck.setVisibility(View.VISIBLE);
        } else if (ThemeHelper.THEME_DARK.equals(currentTheme)) {
            ivDarkCheck.setVisibility(View.VISIBLE);
        } else {
            ivSystemCheck.setVisibility(View.VISIBLE);
        }

        btnLight.setOnClickListener(v -> {
            ThemeHelper.saveTheme(requireContext(), ThemeHelper.THEME_LIGHT);
            requireActivity().recreate();
            dismiss();
        });

        btnDark.setOnClickListener(v -> {
            ThemeHelper.saveTheme(requireContext(), ThemeHelper.THEME_DARK);
            requireActivity().recreate();
            dismiss();
        });

        btnSystem.setOnClickListener(v -> {
            ThemeHelper.saveTheme(requireContext(), ThemeHelper.THEME_SYSTEM);
            requireActivity().recreate();
            dismiss();
        });

        return view;
    }

    private void hideAllChecks(ImageView... checks) {
        for (ImageView check : checks) {
            check.setVisibility(View.GONE);
        }
    }
}
