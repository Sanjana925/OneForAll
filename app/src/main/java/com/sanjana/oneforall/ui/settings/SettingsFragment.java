package com.sanjana.oneforall.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.sanjana.oneforall.R;
import com.sanjana.oneforall.MainActivity;

public class SettingsFragment extends Fragment {

    private Switch switchNotifications, switchTheme;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchTheme = view.findViewById(R.id.switchTheme);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchTheme.setChecked(prefs.getBoolean("dark_theme_enabled", false));

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(requireContext(),
                    "Notifications " + (isChecked ? "ON" : "OFF"),
                    Toast.LENGTH_SHORT).show();
        });

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_theme_enabled", isChecked).apply();
            Toast.makeText(requireContext(),
                    "Theme changed to " + (isChecked ? "Dark" : "Light"),
                    Toast.LENGTH_SHORT).show();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).applyTheme(isChecked);
            }
        });

        return view;
    }
}
