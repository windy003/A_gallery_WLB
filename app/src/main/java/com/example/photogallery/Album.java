package com.example.photogallery;

import android.net.Uri;
import java.util.ArrayList;
import java.util.List;

public class Album {
    private String name;
    private List<String> imageUris; // 使用String而不是Uri以便序列化
    private long id;

    public Album(long id, String name) {
        this.id = id;
        this.name = name;
        this.imageUris = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Uri> getImageUris() {
        List<Uri> uris = new ArrayList<>();
        for (String uriString : imageUris) {
            uris.add(Uri.parse(uriString));
        }
        return uris;
    }

    public void addImage(Uri imageUri) {
        String uriString = imageUri.toString();
        if (!imageUris.contains(uriString)) {
            imageUris.add(uriString);
        }
    }

    public void removeImage(Uri imageUri) {
        imageUris.remove(imageUri.toString());
    }

    public void clearImages() {
        imageUris.clear();
    }

    public int getImageCount() {
        return imageUris.size();
    }

    public Uri getCoverImage() {
        return imageUris.isEmpty() ? null : Uri.parse(imageUris.get(0));
    }
} 