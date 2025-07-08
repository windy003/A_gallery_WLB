package com.example.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.util.List;

public class AlbumAdapter extends BaseAdapter {
    private Context context;
    private List<Album> albums;
    private LayoutInflater inflater;

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return albums.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.album_item, parent, false);
            holder = new ViewHolder();
            holder.albumCover = convertView.findViewById(R.id.albumCover);
            holder.albumName = convertView.findViewById(R.id.albumName);
            holder.albumCount = convertView.findViewById(R.id.albumCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Album album = albums.get(position);
        holder.albumName.setText(album.getName());
        holder.albumCount.setText(album.getImageCount() + " 张照片");

        // 设置相册封面
        Uri coverUri = album.getCoverImage();
        if (coverUri != null) {
            loadCoverImage(holder.albumCover, coverUri);
        } else {
            holder.albumCover.setImageResource(R.drawable.ic_launcher_foreground);
        }

        return convertView;
    }

    private void loadCoverImage(ImageView imageView, Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream is = context.getContentResolver().openInputStream(imageUri);
                if (is != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8; // 缩小图片以节省内存
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
                    if (bitmap != null) {
                        imageView.post(() -> imageView.setImageBitmap(bitmap));
                    } else {
                        imageView.post(() -> imageView.setImageResource(R.drawable.ic_launcher_foreground));
                    }
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.post(() -> imageView.setImageResource(R.drawable.ic_launcher_foreground));
            }
        }).start();
    }

    static class ViewHolder {
        ImageView albumCover;
        TextView albumName;
        TextView albumCount;
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums = newAlbums;
        notifyDataSetChanged();
    }
}