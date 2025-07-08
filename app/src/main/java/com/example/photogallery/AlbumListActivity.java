package com.example.photogallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class AlbumListActivity extends AppCompatActivity {
    private ListView albumListView;
    private AlbumAdapter albumAdapter;
    private FloatingActionButton fabCreateAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.album_list);
            setTitle("相册");
            
            initViews();
            setupAdapter();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "加载相册失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        albumListView = findViewById(R.id.albumListView);
        fabCreateAlbum = findViewById(R.id.fabCreateAlbum);
        
        if (albumListView == null || fabCreateAlbum == null) {
            throw new RuntimeException("布局文件中缺少必要的视图");
        }
    }

    private void setupAdapter() {
        try {
            List<Album> albums = AlbumManager.getInstance(this).getAlbums();
            albumAdapter = new AlbumAdapter(this, albums);
            albumListView.setAdapter(albumAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "加载相册数据失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        // 点击相册进入相册查看
        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Album album = (Album) albumAdapter.getItem(position);
                openAlbumView(album);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "打开相册失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 长按相册显示删除选项
        albumListView.setOnItemLongClickListener((parent, view, position, id) -> {
            try {
                Album album = (Album) albumAdapter.getItem(position);
                showDeleteAlbumDialog(album);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "操作失败", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // 创建新相册
        fabCreateAlbum.setOnClickListener(v -> showCreateAlbumDialog());
    }

    private void openAlbumView(Album album) {
        Intent intent = new Intent(this, AlbumViewActivity.class);
        intent.putExtra("album_id", album.getId());
        startActivity(intent);
    }

    private void showDeleteAlbumDialog(Album album) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认删除");
        builder.setMessage("确定要删除相册 \"" + album.getName() + "\" 吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                AlbumManager.getInstance(this).deleteAlbum(album);
                refreshAlbumList();
                Toast.makeText(this, "相册已删除", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showCreateAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("创建相册");
        
        final EditText input = new EditText(this);
        input.setHint("请输入相册名称");
        builder.setView(input);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                String albumName = input.getText().toString().trim();
                if (!albumName.isEmpty()) {
                    AlbumManager.getInstance(this).createAlbum(albumName);
                    refreshAlbumList();
                    Toast.makeText(this, "相册创建成功", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void refreshAlbumList() {
        try {
            List<Album> albums = AlbumManager.getInstance(this).getAlbums();
            if (albumAdapter != null) {
                albumAdapter.updateAlbums(albums);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "刷新失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAlbumList();
    }
} 