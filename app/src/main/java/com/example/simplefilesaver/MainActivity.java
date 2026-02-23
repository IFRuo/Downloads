package com.example.simplefilesaver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 检查并请求存储权限（Android 10及以下需要）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            // 单文件
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                saveFileFromUri(uri);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            // 多文件
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) {
                for (Uri uri : uris) {
                    saveFileFromUri(uri);
                }
            }
        }
        finish(); // 处理完后自动关闭Activity
    }

    private void saveFileFromUri(Uri uri) {
        try {
            // 获取文件名（如果Uri无法提供则生成时间戳）
            String fileName = "file_" + System.currentTimeMillis();
            if (uri.getPath() != null) {
                String path = uri.getPath();
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash >= 0) {
                    fileName = path.substring(lastSlash + 1);
                }
            }

            // 目标目录：Downloads/SimpleFileSaver/
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File saveDir = new File(downloadsDir, "SimpleFileSaver");
            if (!saveDir.exists()) saveDir.mkdirs();

            File outputFile = new File(saveDir, fileName);

            // 复制文件
            try (InputStream is = getContentResolver().openInputStream(uri);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
                Toast.makeText(this, "已保存: " + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleIntent(getIntent());
            } else {
                Toast.makeText(this, "需要存储权限才能保存文件", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

