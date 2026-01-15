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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.sanjana.oneforall.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class BackupFragment extends Fragment {

    private Button btnBackup, btnRestore, btnShare;

    private final File backupFolder =
            new File(Environment.getExternalStorageDirectory(), "OneForAllBackup");
    private final File backupFile =
            new File(backupFolder, "oneforall_backup.db");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_backup, container, false);

        btnBackup = view.findViewById(R.id.btnBackup);
        btnRestore = view.findViewById(R.id.btnRestore);
        btnShare = view.findViewById(R.id.btnShare);

        if (!backupFolder.exists()) backupFolder.mkdirs();

        btnBackup.setOnClickListener(v -> backupDatabase());
        btnRestore.setOnClickListener(v -> restoreDatabase());
        btnShare.setOnClickListener(v -> shareBackup());

        return view;
    }

    private void backupDatabase() {
        try {
            File dbFile = requireContext().getDatabasePath("oneforall.db");

            try (FileChannel src = new FileInputStream(dbFile).getChannel();
                 FileChannel dst = new FileOutputStream(backupFile).getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }

            Toast.makeText(getContext(),
                    "Backup saved to " + backupFile.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Backup failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void restoreDatabase() {
        try {
            if (!backupFile.exists()) {
                Toast.makeText(getContext(), "No backup found!", Toast.LENGTH_SHORT).show();
                return;
            }

            File dbFile = requireContext().getDatabasePath("oneforall.db");

            try (FileChannel src = new FileInputStream(backupFile).getChannel();
                 FileChannel dst = new FileOutputStream(dbFile).getChannel()) {
                dst.transferFrom(src, 0, src.size());
            }

            Toast.makeText(getContext(),
                    "Database restored successfully!",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Restore failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void shareBackup() {
        try {
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
            Toast.makeText(getContext(),
                    "Share failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
