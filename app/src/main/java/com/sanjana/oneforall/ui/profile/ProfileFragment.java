package com.sanjana.oneforall.ui.profile;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sanjana.oneforall.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private EditText etUsername;
    private Button btnSave;
    private CircleImageView btnAvatar;

    private static final String PREFS_NAME = "oneforall_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_AVATAR_URI = "avatar_uri";

    private SharedPreferences prefs;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // ---------------- FIND VIEWS ----------------
        etUsername = view.findViewById(R.id.etUsername);
        btnSave = view.findViewById(R.id.btnSaveUsername);
        btnAvatar = view.findViewById(R.id.btnAvatar);

        // ---------------- SHARED PREFERENCES ----------------
        prefs = requireContext().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);

        // Load username
        etUsername.setText(prefs.getString(KEY_USERNAME, ""));

        // Load avatar
        String avatarUriStr = prefs.getString(KEY_AVATAR_URI, null);
        if (avatarUriStr != null) btnAvatar.setImageURI(Uri.parse(avatarUriStr));

        // ---------------- PICK IMAGE ----------------
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        btnAvatar.setImageURI(uri); // CircleImageView crops it automatically
                        prefs.edit().putString(KEY_AVATAR_URI, uri.toString()).apply();
                        Toast.makeText(getContext(), "Avatar updated", Toast.LENGTH_SHORT).show();
                        updateDrawerHeader();
                    }
                }
        );

        btnAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // ---------------- SAVE USERNAME ----------------
        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(getContext(), "Enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_USERNAME, username).apply();
            Toast.makeText(getContext(), "Username saved!", Toast.LENGTH_SHORT).show();
            updateDrawerHeader();
        });

        return view;
    }

    // ---------------- UPDATE DRAWER HEADER ----------------
    private void updateDrawerHeader() {
        if (getActivity() instanceof com.sanjana.oneforall.MainActivity) {
            com.sanjana.oneforall.MainActivity main =
                    (com.sanjana.oneforall.MainActivity) getActivity();
            main.updateDrawerProfile();
        }
    }
}
