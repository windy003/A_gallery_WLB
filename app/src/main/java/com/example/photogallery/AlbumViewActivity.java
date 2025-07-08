package com.example.photogallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import java.io.InputStream;
import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {
    private Album album;
    private PhotoAdapter photoAdapter;
    private GridView gridView;
    private ImageView fullImageView;
    private TextView albumTitle;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_view);

        // 获取传递的相册ID
        long albumId = getIntent().getLongExtra("album_id", -1);
        if (albumId == -1) {
            finish();
            return;
        }

        album = AlbumManager.getInstance(this).getAlbumById(albumId);
        if (album == null) {
            finish();
            return;
        }

        initViews();
        setupAdapter();
        updateUI();
        setupBackButton();
    }

    private void initViews() {
        gridView = findViewById(R.id.albumGridView);
        fullImageView = findViewById(R.id.albumFullImageView);
        albumTitle = findViewById(R.id.albumTitle);
        emptyView = findViewById(R.id.emptyView);
        
        setTitle(album.getName());
        albumTitle.setText(album.getName());
    }

    private void setupAdapter() {
        List<Uri> imageUris = album.getImageUris();
        photoAdapter = new PhotoAdapter(this, imageUris);
        gridView.setAdapter(photoAdapter);
        
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri imageUri = album.getImageUris().get(position);
            openFullScreen(imageUri);
        });

        // 长按删除照片
        gridView.setOnItemLongClickListener((parent, view, position, id) -> {
            Uri imageUri = album.getImageUris().get(position);
            showRemoveImageDialog(imageUri);
            return true;
        });
    }

    private void updateUI() {
        if (album.getImageCount() == 0) {
            gridView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showRemoveImageDialog(Uri imageUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("移除照片");
        builder.setMessage("确定要从相册中移除这张照片吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            AlbumManager.getInstance(this).removeImageFromAlbum(album, imageUri);
            refreshAlbum();
            Toast.makeText(this, "照片已移除", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openFullScreen(Uri imageUri) {
        gridView.setVisibility(View.GONE);
        albumTitle.setVisibility(View.GONE);
        new Thread(() -> {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                if (is != null) {
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            fullImageView.setImageBitmap(bitmap);
                            fullImageView.setVisibility(View.VISIBLE);
                        }
                    });
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void refreshAlbum() {
        album = AlbumManager.getInstance(this).getAlbumById(album.getId());
        if (album != null) {
            List<Uri> imageUris = album.getImageUris();
            photoAdapter = new PhotoAdapter(this, imageUris);
            gridView.setAdapter(photoAdapter);
            albumTitle.setText(album.getName() + " (" + album.getImageCount() + " 张照片)");
            updateUI();
        }
    }

    private void setupBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fullImageView.getVisibility() == View.VISIBLE) {
                    fullImageView.setVisibility(View.GONE);
                    gridView.setVisibility(View.VISIBLE);
                    albumTitle.setVisibility(View.VISIBLE);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_album) {
            showClearAlbumDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空相册");
        builder.setMessage("确定要清空相册中的所有照片吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            AlbumManager.getInstance(this).clearAlbum(album);
            refreshAlbum();
            Toast.makeText(this, "相册已清空", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAlbum();
    }
} 