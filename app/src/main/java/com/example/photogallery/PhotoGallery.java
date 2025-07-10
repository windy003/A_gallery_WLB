package com.example.photogallery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoGallery extends AppCompatActivity {

    private PhotoAdapter photoAdapter;
    private final ArrayList<Uri> imageUris = new ArrayList<>();
    private GridView gridView;
    private ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_gallery);
        
        setTitle("所有照片");
        
        gridView = findViewById(R.id.gridView);
        fullImageView = findViewById(R.id.fullImageView);
        photoAdapter = new PhotoAdapter(this, imageUris);
        gridView.setAdapter(photoAdapter);
        
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri imageUri = imageUris.get(position);
            openFullScreen(imageUri);
        });

        // 添加长按监听器用于添加到相册
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Uri imageUri = imageUris.get(position);
            showAddToAlbumDialog(imageUri);
            return true;
        });

        checkPermissionAndLoadImages();
        setupBackButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_albums) {
            openAlbumList();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showAboutDialog() {
        String message = "相册应用\n\n" +
                "系统版本: " + PermissionHelper.getAndroidVersionInfo() + "\n" +
                "安卓11兼容: " + (PermissionHelper.isAndroid11OrAbove() ? "是" : "否") + "\n" +
                "权限状态: " + (PermissionHelper.hasReadMediaPermission(this) ? "已授权" : "未授权");
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void openAlbumList() {
        Intent intent = new Intent(this, AlbumListActivity.class);
        startActivity(intent);
    }

    private void showAddToAlbumDialog(Uri imageUri) {
        List<Album> albums = AlbumManager.getInstance(this).getAlbums();
        
        if (albums.isEmpty()) {
            Toast.makeText(this, "请先创建相册", Toast.LENGTH_SHORT).show();
            showCreateAlbumDialog(imageUri);
            return;
        }

        String[] albumNames = new String[albums.size() + 1];
        for (int i = 0; i < albums.size(); i++) {
            albumNames[i] = albums.get(i).getName();
        }
        albumNames[albums.size()] = "创建新相册";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择相册");
        builder.setItems(albumNames, (dialog, which) -> {
            if (which == albums.size()) {
                // 创建新相册
                showCreateAlbumDialog(imageUri);
            } else {
                // 添加到现有相册
                Album selectedAlbum = albums.get(which);
                AlbumManager.getInstance(this).addImageToAlbum(selectedAlbum, imageUri);
                Toast.makeText(this, "图片已添加到相册", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showCreateAlbumDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("创建相册");
        
        final EditText input = new EditText(this);
        input.setHint("请输入相册名称");
        builder.setView(input);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            String albumName = input.getText().toString().trim();
            if (!albumName.isEmpty()) {
                Album newAlbum = AlbumManager.getInstance(this).createAlbum(albumName);
                if (imageUri != null) {
                    AlbumManager.getInstance(this).addImageToAlbum(newAlbum, imageUri);
                }
                Toast.makeText(this, "相册创建成功", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openFullScreen(Uri imageUri) {
        gridView.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                final Bitmap bitmap = BitmapFactory.decodeStream(is);
                runOnUiThread(() -> {
                    fullImageView.setImageBitmap(bitmap);
                    fullImageView.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void checkPermissionAndLoadImages() {
        if (PermissionHelper.hasReadMediaPermission(this)) {
            loadImagesFromMediaStore();
        } else {
            String permission = PermissionHelper.getReadMediaPermission();
            requestPermissionLauncher.launch(permission);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "权限已授权，正在加载图片...", Toast.LENGTH_SHORT).show();
                    loadImagesFromMediaStore();
                } else {
                    String message = "需要存储权限才能访问照片。\n" +
                            "当前系统: " + PermissionHelper.getAndroidVersionInfo() + "\n" +
                            "请在设置中手动授权。";
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            });

    private void loadImagesFromMediaStore() {
        // 清空之前的图片列表
        imageUris.clear();
        
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE
        };
        
        // 添加查询条件，只查询图片类型
        String selection = MediaStore.Images.Media.MIME_TYPE + "=? OR " + 
                          MediaStore.Images.Media.MIME_TYPE + "=? OR " +
                          MediaStore.Images.Media.MIME_TYPE + "=? OR " +
                          MediaStore.Images.Media.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"};
        
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";
        
        try (Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int count = 0;
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    imageUris.add(imageUri);
                    count++;
                }
                photoAdapter.notifyDataSetChanged();
                
                // 显示加载结果
                String message = "已加载 " + count + " 张图片";
                if (PermissionHelper.isAndroid11OrAbove()) {
                    message += " (安卓11+兼容模式)";
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "无法访问媒体存储", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "加载图片时出错: " + e.getMessage();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void setupBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fullImageView.getVisibility() == View.VISIBLE) {
                    fullImageView.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
