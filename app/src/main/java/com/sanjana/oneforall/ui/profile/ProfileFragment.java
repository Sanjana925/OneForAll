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

import com.google.android.material.imageview.ShapeableImageView;
import com.sanjana.oneforall.R;
import com.sanjana.oneforall.MainActivity;

public class ProfileFragment extends Fragment {

    private EditText etUsername;
    private Button btnSave;
    private ShapeableImageView btnAvatar;

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

        etUsername = view.findViewById(R.id.etUsername);
        btnSave = view.findViewById(R.id.btnSaveUsername);
        btnAvatar = view.findViewById(R.id.btnAvatar);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, getContext().MODE_PRIVATE);

        // Load username
        etUsername.setText(prefs.getString(KEY_USERNAME, ""));

        // Load avatar
        String avatarUriStr = prefs.getString(KEY_AVATAR_URI, null);
        if (avatarUriStr != null) btnAvatar.setImageURI(Uri.parse(avatarUriStr));
        else btnAvatar.setImageResource(R.drawable.ic_avatar); // placeholder

        // Avatar picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        btnAvatar.setImageURI(uri);
                        prefs.edit().putString(KEY_AVATAR_URI, uri.toString()).apply();
                        Toast.makeText(getContext(), "Avatar updated", Toast.LENGTH_SHORT).show();
                        updateDrawerHeader();
                    }
                }
        );

        btnAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Save username
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

    // Update drawer header avatar + username
    private void updateDrawerHeader() {
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            main.updateDrawerProfile();
        }
    }
}
