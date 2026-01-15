package com.sanjana.oneforall.ui.profile;

import android.content.Context;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileFragment extends Fragment {

    private EditText etUsername;
    private Button btnSave;
    private ShapeableImageView ivAvatar;

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
        ivAvatar = view.findViewById(R.id.btnAvatar);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        etUsername.setText(prefs.getString(KEY_USERNAME, ""));

        String avatarPath = prefs.getString(KEY_AVATAR_URI, null);
        if (avatarPath != null) {
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                ivAvatar.setImageURI(Uri.fromFile(avatarFile));
            }
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveAvatarToInternal(uri);
                    }
                }
        );

        ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

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

    private void saveAvatarToInternal(@NonNull Uri uri) {
        try {
            File file = new File(requireContext().getFilesDir(), "avatar.png");
            try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(file)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }

            prefs.edit().putString(KEY_AVATAR_URI, file.getAbsolutePath()).apply();
            ivAvatar.setImageURI(Uri.fromFile(file));
            Toast.makeText(getContext(), "Avatar updated", Toast.LENGTH_SHORT).show();
            updateDrawerHeader();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDrawerHeader() {
        if (getActivity() instanceof com.sanjana.oneforall.MainActivity) {
            com.sanjana.oneforall.MainActivity main = (com.sanjana.oneforall.MainActivity) getActivity();
            main.updateDrawerProfile();
        }
    }
}
