package com.sanjana.oneforall.ui.backup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sanjana.oneforall.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupFragment extends Fragment {

    private Button btnBackup, btnRestore, btnShare;

    // Launcher for folder/file creation
    private ActivityResultLauncher<Intent> createDocumentLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backup, container, false);

        btnBackup = view.findViewById(R.id.btnBackup);
        btnRestore = view.findViewById(R.id.btnRestore);
        btnShare = view.findViewById(R.id.btnShare);

        // Launcher to select file path for backup
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        if (result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) backupToUri(uri);
                        }
                    }
                });

        btnBackup.setOnClickListener(v -> createBackupFile());
        btnRestore.setOnClickListener(v -> restoreDatabase());
        btnShare.setOnClickListener(v -> shareBackup());

        return view;
    }

    // ---------------------- BACKUP ----------------------
    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "oneforall_backup.db");
        createDocumentLauncher.launch(intent);
    }

    private void backupToUri(Uri uri) {
        try {
            File dbFile = requireContext().getDatabasePath("oneforall.db");

            try (FileChannel src = new FileInputStream(dbFile).getChannel();
                 // Open writable file descriptor for the URI
                 FileChannel dst = new FileOutputStream(
                         requireContext().getContentResolver().openFileDescriptor(uri, "w").getFileDescriptor()
                 ).getChannel()) {

                dst.transferFrom(src, 0, src.size());
            }

            Toast.makeText(getContext(), "Backup saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------- RESTORE ----------------------
    private void restoreDatabase() {
        try {
            File dbFile = requireContext().getDatabasePath("oneforall.db");
            File backupFile = new File(Environment.getExternalStorageDirectory(),
                    "OneForAllBackup/oneforall_backup.db");

            if (!backupFile.exists()) {
                Toast.makeText(getContext(), "No backup found!", Toast.LENGTH_SHORT).show();
                return;
            }

            try (FileChannel src = new FileInputStream(backupFile).getChannel();
                 FileChannel dst = new FileOutputStream(dbFile).getChannel()) {

                dst.transferFrom(src, 0, src.size());
            }

            Toast.makeText(getContext(), "Database restored!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------------- SHARE ----------------------
    private void shareBackup() {
        try {
            File backupFile = new File(Environment.getExternalStorageDirectory(),
                    "OneForAllBackup/oneforall_backup.db");

            if (!backupFile.exists()) {
                Toast.makeText(getContext(), "No backup to share!", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri fileUri = Uri.fromFile(backupFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(shareIntent, "Share Backup via"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Share failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
